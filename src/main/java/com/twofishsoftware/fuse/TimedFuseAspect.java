/*
 * Copyright 2024 Two Fish Software, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.twofishsoftware.fuse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

@Aspect
@Component
public class TimedFuseAspect {

    @Autowired
    private TimedFuseProperties properties;

    @Autowired
    TimedFuseStateProvider stateProvider;

    @Around("@annotation(com.twofishsoftware.fuse.TimedFuse)")
    public Object igniteFuse(ProceedingJoinPoint joinPoint) {
        if (properties.getEnabled()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String shortMethodName = signature.toShortString();
            String longMethodName = signature.toLongString();
            TimedFuse timedFuse = method.getAnnotation(TimedFuse.class);

            TimedFuseState state = stateProvider.GetState(longMethodName);
            if (state == null) {
                stateProvider.CreateState(longMethodName, timedFuse.permittedFailures(), timedFuse.monitorDurationMs(), timedFuse.resetDurationMs());
            }

            if (!stateProvider.IsClosed(longMethodName)) {
                throw new TimedFuseException("The timed fuse around " + shortMethodName + " is opened.");
            }

            AtomicReference<Object> proceed = new AtomicReference<>();
            AtomicReference<Throwable> throwable = new AtomicReference<>();
            try {
                Thread thread = Thread.ofVirtual().start(() -> {
                    try {
                        proceed.set(joinPoint.proceed());
                    }
                    catch (Throwable t) {
                        throwable.set(t);
                    }
                });
                thread.join(timedFuse.timeoutMs());

                if (thread.isAlive()) {
                    thread.interrupt();
                    stateProvider.LogTimeout(longMethodName); // timeout
                    throw new TimedFuseException("The timed fuse around " + shortMethodName + " exceeded its execution timeout.");
                }
                else if (throwable.get() != null) {
                    stateProvider.LogException(longMethodName, throwable.get()); // exception
                    throw new TimedFuseException(throwable.get());
                }

            }
            catch (InterruptedException e) {
                stateProvider.LogException(longMethodName, throwable.get());
                throw new RuntimeException(e);
            }

            stateProvider.LogSuccess(longMethodName);
            return proceed;
            }
        else {
            try {
                return joinPoint.proceed();
            }
            catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}

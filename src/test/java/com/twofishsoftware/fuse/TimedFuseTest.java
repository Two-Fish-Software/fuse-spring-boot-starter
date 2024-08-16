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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest( classes = {
        TimedFuseAspect.class,
        TimedFuseProperties.class,
        TimedFuseStateProvider.class,
        TimedFuseDemoComponent.class,
        AnnotationAwareAspectJAutoProxyCreator.class
})
public class TimedFuseTest {
    @Autowired
    protected TimedFuseStateProvider stateProvider;

    @Autowired
    protected TimedFuseDemoComponent demoComponent;
    
    private final String PAUSE_NAME = "public void com.twofishsoftware.fuse.TimedFuseDemoComponent.pause(java.lang.Integer)";

    @Test
    public void shouldFinishInTime() {
        stateProvider.Reset();
        demoComponent.pause(100);
    }

    @Test
    public void shouldThrowTimedFuseException() {
        stateProvider.Reset();
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));
    }

    @Test
    public void shouldThrowRuntimeException() {
        stateProvider.Reset();
        assertThrows(RuntimeException.class, () -> demoComponent.except());
    }

    @Test
    public void fuseShouldOpenAfterRepeatedTimeouts() {
        stateProvider.Reset();
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertFalse(stateProvider.GetState(PAUSE_NAME).IsClosed());

        // This will throw because it's opened
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(10));

        assertFalse(stateProvider.GetState(PAUSE_NAME).IsClosed());
    }

    @Test
    public void fuseShouldCloseAfterResetDuration() {
        stateProvider.Reset();
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertFalse(stateProvider.GetState(PAUSE_NAME).IsClosed());

        // This will throw because it's opened
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(10));

        try {
            Thread.sleep(11 * 1000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());
    }

    @Test
    public void fuseShouldReOpenAfterTimeout() {
        stateProvider.Reset();
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertFalse(stateProvider.GetState(PAUSE_NAME).IsClosed());

        // This will throw because it's opened
        assertThrows(TimedFuseException.class, () -> demoComponent.pause(10));

        try {
            Thread.sleep(11 * 1000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertTrue(stateProvider.GetState(PAUSE_NAME).IsClosed());

        assertThrows(TimedFuseException.class, () -> demoComponent.pause(1000));

        assertFalse(stateProvider.GetState(PAUSE_NAME).IsClosed());
    }
}

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

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TimedFuseStateProvider {

    private final ConcurrentHashMap<String, TimedFuseState> states = new ConcurrentHashMap<>();

    public void reset() {
        states.clear();
    }

    public TimedFuseState getState(String name) {
        return states.get(name);
    }

    public void createState(String name, int permittedFailures, int monitorDurationMs, int resetDurationMs) {
        states.computeIfAbsent(name, k -> new TimedFuseState(permittedFailures, monitorDurationMs, resetDurationMs));
    }

    public boolean isClosed(String name) {
        TimedFuseState state = states.get(name);
        if (state == null) {
            return true;
        }
        return state.isClosed();
    }

    public void logSuccess(String name) {
        TimedFuseState state = states.get(name);
        if (state != null) {
            state.logSuccess();
        }
    }

    public void logTimeout(String name) {
        TimedFuseState state = states.get(name);
        if (state != null) {
            state.logFailure();
        }
    }

    public void logException(String name, Throwable throwable) {
        TimedFuseState state = states.get(name);
        if (state != null) {
            state.logFailure();
        }
    }
}

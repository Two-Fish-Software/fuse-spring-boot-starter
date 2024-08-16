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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TimedFuseStateProvider {

    private final ConcurrentHashMap<String, AtomicReference<TimedFuseState>> states = new ConcurrentHashMap<>();

    public void Reset() {
        states.clear();
    }

    public TimedFuseState GetState(String name) {
        if (!states.containsKey(name)) {
            return null;
        }
        return states.get(name).get();
    }

    public void CreateState(String name, Integer permittedFailures, Integer monitorDurationMs, Integer resetDurationMs) {
        TimedFuseState state = new TimedFuseState(permittedFailures, monitorDurationMs, resetDurationMs);
        states.put(name, new AtomicReference<>(state));
    }

    public boolean IsClosed(String name) {
        if (!states.containsKey(name)) {
            return false;
        }
        return states.get(name).get().IsClosed();
    }

    public void LogSuccess(String name) {
        if (states.containsKey(name)) {
            TimedFuseState state = states.get(name).get();
            state.LogSuccess();
            states.get(name).set(state);
        }
    }

    public void LogTimeout(String name) {
        if (states.containsKey(name)) {
            TimedFuseState state = states.get(name).get();
            state.LogFailure();
            states.get(name).set(state);
        }
    }

    public void LogException(String name, Throwable throwable) {
        // TODO: determine if I want it to handle exceptions or not
    }
}

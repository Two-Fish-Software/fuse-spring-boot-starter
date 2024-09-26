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

import org.springframework.stereotype.Component;

@Component
public class TimedFuseDemoComponent {

    @TimedFuse(timeoutMs = 500, permittedFailures = 3, monitorDurationMs = 10*1000, resetDurationMs = 10*1000)
    public void pause(Integer ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TimedFuse(timeoutMs = 500, permittedFailures = 3, monitorDurationMs = 10*1000, resetDurationMs = 10*1000)
    public void except() {
            throw new RuntimeException("Test Exception");
    }

    @TimedFuse(timeoutMs = 500, permittedFailures = 3, monitorDurationMs = 10*1000, resetDurationMs = 10*1000)
    public String str() {
        return "Test String";
    }
}

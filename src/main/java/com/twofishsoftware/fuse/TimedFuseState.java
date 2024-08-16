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

import java.time.Instant;

public class TimedFuseState {
    private final Integer permittedFailures;
    private final Integer monitorDuration;
    private final Integer resetDuration;

    private TimedFuseStatus status;
    private Instant nextReset;
    private Integer failures;

    public TimedFuseState(Integer permittedFailures, Integer monitorDuration, Integer resetDuration) {
        super();
        this.permittedFailures = permittedFailures;
        this.monitorDuration = monitorDuration;
        this.resetDuration = resetDuration;
        this.status = TimedFuseStatus.CLOSED;
        this.failures = 0;
    }

    public boolean IsClosed() {
        switch(status) {
            case OPEN -> {
                if (Instant.now().isAfter(nextReset)) {
                    status = TimedFuseStatus.CLOSING;
                    return true;
                }
                else {
                    return false;
                }
            }
            default -> {
                return true;
            }
        }
    }

    public void LogSuccess() {
        switch(status) {
            case CLOSING -> {
                status = TimedFuseStatus.CLOSED;
                failures = 0;
            }
            case OPENING -> {
                if (Instant.now().isAfter(nextReset)) {
                    status = TimedFuseStatus.CLOSED;
                    failures = 0;
                }
            }
            default -> {}
        }
    }

    public void LogFailure() {
        switch(status) {
            case CLOSING -> {
                status = TimedFuseStatus.OPEN;
                nextReset = Instant.now().plusMillis(resetDuration);
            }
            case CLOSED -> {
                failures = 1;
                if (failures >= permittedFailures) {
                    status = TimedFuseStatus.OPEN;
                    nextReset = Instant.now().plusMillis(resetDuration);
                }
                else {
                    status = TimedFuseStatus.OPENING;
                    nextReset = Instant.now().plusMillis(monitorDuration);
                }
            }
            case OPENING -> {
                if (Instant.now().isBefore(nextReset)) {
                    failures++;
                }
                else {
                    failures = 1;
                }
                if (failures >= permittedFailures) {
                    status = TimedFuseStatus.OPEN;
                    nextReset = Instant.now().plusMillis(resetDuration);
                }
            }
            case OPEN -> {
                nextReset = Instant.now().plusMillis(resetDuration);
                failures++;
            }
        }
    }
}

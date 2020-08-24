// Copyright 2020 The Operator-SDK Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.fabric8.controller.controller_runtime.pkg;

import java.time.Duration;

public class Result {

    /**
     * Gets Requeue.
     *
     * @return the get requeue flag
     */
    public boolean isRequeue() {
        return requeue;
    }

    /**
     * Sets requeue flag.
     *
     * @param requeue the requeue flag
     */
    public void setRequeue(boolean requeue) {
        this.requeue = requeue;
    }

    /**
     * Gets Duration.
     *
     * @return requeueAfter the deuration
     */
    public Duration getRequeueAfter() {
        return requeueAfter;
    }

    /**
     * Sets Duration.
     *
     * @param requeueAfter the requeueAfter duration
     */
    public void setRequeueAfter(Duration requeueAfter) {
        this.requeueAfter = requeueAfter;
    }

    private boolean requeue;
    private Duration requeueAfter;
}

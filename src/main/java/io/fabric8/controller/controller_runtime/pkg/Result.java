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

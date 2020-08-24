package io.fabric8.controller.controller_runtime;

public interface Controller extends Runnable {
    /** Shutdown the controller. */
    void shutdown();
}

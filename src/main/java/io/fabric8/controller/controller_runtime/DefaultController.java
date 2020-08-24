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

package io.fabric8.controller.controller_runtime;

import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.controller.controller_runtime.pkg.Reconciler;
import io.fabric8.controller.controller_runtime.pkg.Result;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;

public class DefaultController implements Controller {
    private static final Logger log = LoggerFactory.getLogger(DefaultController.class);

    private Reconciler reconciler;
    private Request request;
    private BlockingQueue<Request> workQueue =  new ArrayBlockingQueue<>(1024);
    private String name;
    private int workerCount;
    private ScheduledExecutorService workerThreadPool;

    /**
     * Instantiates a new Default controller.
     *
     * @param reconciler the reconciler
     * @param workQueue the work queue
     */
    public DefaultController(Reconciler reconciler,BlockingQueue<Request> workQueue){
        this.reconciler = reconciler;
        this.workQueue = workQueue;
    }

    public DefaultController(){

    }

    @Override
    public void run() {

        System.out.println("Running the controller");

        if(!preFlightCheck()){
            log.error("Controller {} failed pre-run check, exiting..", this.name);
            return;
        }

        // spawns worker threads for the controller.
        CountDownLatch latch = new CountDownLatch(workerCount);
        for (int i = 0; i < this.workerCount; i++) {
            final int workerIndex = i;
            workerThreadPool.scheduleWithFixedDelay(
                    () -> {
                        log.debug("Starting controller {} worker {}..", this.name, workerIndex);
                        try {
                            Result result= null;
                            Request request = null;
                            request = workQueue.take();
                            result = reconciler.reconcile(request);
                        } catch (Throwable t) {
                            log.error("Unexpected controller loop abortion", t);
                        }
                        latch.countDown();
                        log.debug("Exiting controller {} worker {}..", this.name, workerIndex);
                    },
                    0,
                    1   ,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * preFlightCheck checks if the controller is ready for working.
     *
     * @param
     * @return boolean value
     */
    private boolean preFlightCheck() {
        if (workerCount <= 0) {
            log.error("Fail to start controller {}: worker count must be positive.", this.name);
            return false;
        }
        if (workerThreadPool == null) {
            log.error("Fail to start controller {}: missing worker thread-pool.", this.name);
            return false;
        }
        return true;
    }

    @Override
    public void shutdown() {
        workerThreadPool.shutdown();
    }


    /**
     * Gets reconciler.
     *
     * @return the get reconciler
     */
    public Reconciler getReconciler() {
        return reconciler;
    }

    /**
     * Sets reconciler object.
     *
     * @param reconciler the reconciler object
     */
    public void setReconciler(Reconciler reconciler) {
        this.reconciler = reconciler;
    }

    /**
     * Gets workQueue.
     *
     * @return the get workQueue
     */
    public BlockingQueue<Request> getWorkQueue() {
        return workQueue;
    }

    /**
     * Sets workQueue.
     *
     * @param workQueue the workQueue
     */
    public void setWorkQueue(BlockingQueue<Request> workQueue) {
        this.workQueue = workQueue;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets workerCount.
     *
     * @return the get workerCount
     */
    public int getWorkerCount() {
        return workerCount;
    }

    /**
     * Sets workerCount.
     *
     * @param workerCount the workerCount
     */
    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    /**
     * Gets workerThreadPool.
     *
     * @return the get workerThreadPool
     */
    public ScheduledExecutorService getWorkerThreadPool() {
        return workerThreadPool;
    }

    /**
     * Sets workerThreadPool.
     *
     * @param workerThreadPool the workerThreadPool
     */
    public void setWorkerThreadPool(ScheduledExecutorService workerThreadPool) {
        this.workerThreadPool = workerThreadPool;
    }
}
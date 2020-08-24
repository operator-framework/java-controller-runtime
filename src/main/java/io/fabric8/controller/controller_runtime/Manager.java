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

import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Controller manager manages a set of controllers' lifecycle and also their informer
 * factory.
 */
public class Manager implements Controller{
    private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);
    private Controller[] controllers;
    private SharedInformerFactory informerFactory;

    private ExecutorService controllerThreadPool;

    /**
     * Instantiates a new Controller manager.
     *
     * @param factory the sharedinformerfactory initialized.
     * @param controllers the controllers to be managed.
     */
    public Manager(SharedInformerFactory factory, Controller[] controllers){
        this.controllers = controllers;
        this.informerFactory = factory;
    }

    @Override
    public void shutdown() {

        this.informerFactory.stopAllRegisteredInformers();

        for (Controller controller : this.controllers) {
            controller.shutdown();
        }

        if (controllerThreadPool != null) {
            this.controllerThreadPool.shutdown();
        }
    }

    @Override
    public void run() {

        informerFactory.startAllRegisteredInformers();

        CountDownLatch latch = new CountDownLatch(controllers.length);
        this.controllerThreadPool = Executors.newFixedThreadPool(controllers.length);
        for (Controller controller : this.controllers) {
            controllerThreadPool.submit(
                    () -> {
                        controller.run();
                        latch.countDown();
                    });
        }
        try {
            logger.debug("Controller-Manager {} bootstrapping..");
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Aborting controller-manager.", e);
        } finally {
            logger.info("Controller-Manager {} exited");
        }
    }
}

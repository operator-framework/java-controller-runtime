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
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Controller manager manages a set of controllers' lifecycle and also their informer
 * factory.
 */
public class Manager<T> implements Controller{
    private static final Logger logger = LoggerFactory.getLogger(DefaultController.class);
    private Controller[] controllers;
    private SharedInformerFactory informerFactory;

    private BlockingQueue<Request> workQueue;
    DefaultController defaultController;


    public SharedIndexInformer<T> getSharedIndexInformer() {
        return sharedIndexInformer;
    }

    public void setSharedIndexInformer(SharedIndexInformer<T> sharedIndexInformer) {
        this.sharedIndexInformer = sharedIndexInformer;
    }

    public SharedIndexInformer<T> sharedIndexInformer;

    private ExecutorService controllerThreadPool;

    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    public void setKubernetesClient(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    KubernetesClient kubernetesClient;

    /**
     * Instantiates a new Controller manager.
     *
     * @param factory the sharedinformerfactory initialized.
     * @param controllers the controllers to be managed.
     */
    public Manager(SharedInformerFactory factory, Controller[] controllers,BlockingQueue<Request> workQueue){
        this.controllers = controllers;
        this.informerFactory = factory;
        defaultController = new DefaultController(workQueue);
        this.workQueue = workQueue;
        kubernetesClient = new DefaultKubernetesClient();
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

        sharedIndexInformer.addEventHandler(new ResourceEventHandler<T>() {
            @Override
            public void onAdd(T t) {
                handlePodObject(t);
            }

            @Override
            public void onUpdate(T t, T t1) {
                System.out.println("on update in Controller Runtime");
            }

            @Override
            public void onDelete(T t, boolean b) {
                System.out.println("on delete in Controller Runtime");
                handlePodObject(t);
            }
        });

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

    /**
     * this function will handle the request and add to the queue.
     *
     * @param namespace the T object
     * @param name the T object
     */
    private void addToWorkQueue(String namespace, String name){
        workQueue = defaultController.getWorkQueue();
        workQueue.add(new Request(namespace, name));
        defaultController.setWorkQueue(workQueue);
    }

    /**
     * this function will handle the request and add to the queue.
     *
     * @param t the T object
     */
    private void handlePodObject(T t){
        OwnerReference ownerReference = getController(t);
        if(!ownerReference.getKind().equalsIgnoreCase("Podset")){
            return;
        }
        HasMetadata h = (HasMetadata) t;
        addToWorkQueue(h.getMetadata().getNamespace(), ownerReference.getName());
    }

    /**
     * this function will return then OwnerReference of an Pod object.
     *
     * @param t the T object
     * @return OwnerReference the object of type OwnerReference
     */
    private OwnerReference getController(T t){
        HasMetadata h = (HasMetadata) t;
        List<OwnerReference> ownerReferenceList = h.getMetadata().getOwnerReferences();
        for(OwnerReference ownerReference : ownerReferenceList){
            if(ownerReference.getController().equals(Boolean.TRUE)){
                return ownerReference;
            }
        }
        return null;
    }
}

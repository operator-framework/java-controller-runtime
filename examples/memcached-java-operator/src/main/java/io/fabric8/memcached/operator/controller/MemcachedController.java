package io.fabric8.memcached.operator.controller;

import io.fabric8.controller.controller_runtime.Controller;
import io.fabric8.controller.controller_runtime.Controllers;
import io.fabric8.controller.controller_runtime.DefaultController;
import io.fabric8.controller.controller_runtime.Manager;
import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.*;

public class MemcachedController<T> {
    private static final Logger logger = LoggerFactory.getLogger(MemcachedController.class);

    public KubernetesClient kubernetesClient;
    public SharedIndexInformer<Pod> podSharedIndexInformer;
    public SharedIndexInformer<Memcached> memcachedSharedIndexInformer;
    public SharedIndexInformer<T> tSharedIndexInformer;
    private BlockingQueue<Request> workQueue;
    private Lister<Memcached> memcachedLister;
    private Lister<Pod> podLister;
//    private Lister<T> tLister;
    private Controller[] controllers;
    SharedInformerFactory sharedInformerFactory;
    private int workerCount = 2;
    private String controllerName = "memcached-controller";
    DefaultController defaultController;

    /**
     * Instantiates a new Memcached controller.
     *
     * @param kubernetesClient the kubernetes client
     * @param podSharedIndexInformer the watch for pod
     * @param memcachedSharedIndexInformer the watch for memcached
     * @param sharedInformerFactory the watch object
     */
    public MemcachedController(KubernetesClient kubernetesClient, SharedIndexInformer<Pod> podSharedIndexInformer, SharedIndexInformer<Memcached> memcachedSharedIndexInformer,SharedInformerFactory sharedInformerFactory ){
        this.kubernetesClient = kubernetesClient;
        this.podSharedIndexInformer = podSharedIndexInformer;
        this.memcachedSharedIndexInformer = memcachedSharedIndexInformer;
        this.workQueue = new ArrayBlockingQueue<>(1024);
        this.memcachedLister = new Lister<>(memcachedSharedIndexInformer.getIndexer(),"default");
        this.podLister = new Lister<>(podSharedIndexInformer.getIndexer(),"default");
//        this.tLister = new Lister<>(tSharedIndexInformer.getIndexer(),"default");
        defaultController = new DefaultController(workQueue);
        this.sharedInformerFactory = sharedInformerFactory;
    }

    public MemcachedController() {

    }

    /**
     * It will create add/update/delete events for memcached and pods.
     */
    public void create(){


        memcachedSharedIndexInformer.addEventHandler(new ResourceEventHandler<Memcached>() {
            @Override
            public void onAdd(Memcached memcached) {

                  enQueueMemcached(memcached);
//                String key = Cache.metaNamespaceKeyFunc(memcached);
//                if(key!=null || !(key.isEmpty())) {
//                    HasMetadata h = (HasMetadata) memcached;
//                    workQueue = defaultController.getWorkQueue();
//                    workQueue.add(new Request(((HasMetadata) memcached).getMetadata().getNamespace(), ((HasMetadata) memcached).getMetadata().getName()));
//                    defaultController.setWorkQueue(workQueue);
//                    System.out.println("on add in workqueue size" + workQueue.size());
//                    System.out.println("on Add in Controller Runtime" + ((HasMetadata) memcached).getMetadata().getName() + "namespave" + ((HasMetadata) memcached).getMetadata().getNamespace());
//                }
            }

            @Override
            public void onUpdate(Memcached memcached, Memcached newMemcached) {
             //   enQueueMemcached(newMemcached);
            }

            @Override
            public void onDelete(Memcached memcached, boolean b) {
                //   enQueueMemcached(newMemcached);
            }
        });
//
//        podSharedIndexInformer.addEventHandler(new ResourceEventHandler<Pod>() {
//            @Override
//            public void onAdd(Pod pod) {
//                handlePodObject(pod);
//            }
//
//            @Override
//            public void onUpdate(Pod oldPod, Pod newPod) {
//
////                handlePodObject(newPod);
//            }
//
//            @Override
//            public void onDelete(Pod pod, boolean b) {
//                handlePodObject(pod);
//            }
//        });
    }

    /**
     * This function will initialize all the requred data and call appropriate methods from the
     * Java Controller runtime.
     */
    public void initializeDefaultController(){
        System.out.println("Initialize the Default Controller");
        MemcachedReconciler reconciler = new MemcachedReconciler(kubernetesClient,podLister,memcachedLister);
        defaultController.setWorkQueue(workQueue);
        defaultController.setName(this.controllerName);
        defaultController.setWorkerCount(this.workerCount);
        defaultController.setWorkerThreadPool(
                Executors.newScheduledThreadPool(
                        this.workerCount, Controllers.namedControllerThreadFactory(this.controllerName)));
        defaultController.setReconciler(reconciler);
        this.controllers = new Controller[]{defaultController};
    }

    public void run() throws InterruptedException {

        this.initializeDefaultController();
        Manager manager = new Manager(sharedInformerFactory,controllers,workQueue);
        manager.setPodSharedIndexInformer(podSharedIndexInformer);
//        manager.run();
    //    manager.setPodSharedIndexInformer(podSharedIndexInformer);
        //kubernetesClient = manager.getKubernetesClient();
       // getController().clone()
        manager.run();
    }

    /**
     * Enqueue the data into the workQueue, after add/update/delete events in the cluster
     *
     * @param memcached the controller name
     * @return
     */
    private void enQueueMemcached(Memcached memcached){
        String key = Cache.metaNamespaceKeyFunc(memcached);;
        if(key!=null || !(key.isEmpty())){
            workQueue =  defaultController.getWorkQueue();
            workQueue.add(new Request(memcached.getMetadata().getNamespace(), memcached.getMetadata().getName()));
            defaultController.setWorkQueue(workQueue);
        }
    }

    /**
     * this function will check the OwnerReference and then call's then enQueueMemcached for
     * enqueing the data into workQueue.
     *
     * @param pod the pod object
     * @return
     */
    private void handlePodObject(Pod pod){
        OwnerReference ownerReference = getController(pod);
        if(!ownerReference.getKind().equalsIgnoreCase("Podset")){
            return;
        }
        Memcached memcached =  memcachedLister.get(ownerReference.getName());
        if(memcached!=null)
            enQueueMemcached(memcached);
    }

    /**
     * this function will return then OwnerReference of an Pod object.
     *
     * @param pod the pod object
     * @return OwnerReference the object of type OwnerReference
     */
    private OwnerReference getController(Pod pod){
        List<OwnerReference> ownerReferenceList = pod.getMetadata().getOwnerReferences();
        for(OwnerReference ownerReference : ownerReferenceList){
            if(ownerReference.getController().equals(Boolean.TRUE)){
                return ownerReference;
            }
        }
        return null;
    }
}

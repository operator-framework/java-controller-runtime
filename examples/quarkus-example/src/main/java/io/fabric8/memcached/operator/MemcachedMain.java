package io.fabric8.memcached.operator;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.memcached.operator.controller.MemcachedController;
import io.fabric8.memcached.operator.crd.Memcached;
import io.fabric8.memcached.operator.crd.MemcachedList;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.event.Observes;

public class MemcachedMain {

//    @Inject
//    private KubernetesClient client;

    void onStartup(@Observes StartupEvent _ev) throws InterruptedException {

//        try(KubernetesClient client = new DefaultKubernetesClient()) {

//            List<Pod> podList = client.pods().list().getItems();
//            System.out.println("Example Operator Found with Qurakus = " + podList.size() + " Pods:");
//            for (Pod pod : podList) {
//                System.out.println(" * " + pod.getMetadata().getName());
//            }




//            System.out.println("I am here");
//            CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
//                    .withVersion("v1alpha1")
//                    .withScope("Namespaced")
//                    .withGroup("demo.k8s.io")
//                    .withPlural("podsets")
//                    .build();
//
//            SharedInformerFactory factory = client.informers();
//
//            SharedIndexInformer<Pod> podSharedInformer = factory.sharedIndexInformerFor(Pod.class, PodList.class, 1 * 60 * 1000);
//
//            SharedIndexInformer<PodSet> podSetSharedInformer =
//                    factory.sharedIndexInformerForCustomResource(customResourceDefinitionContext, PodSet.class, PodSetList.class, 1 * 60 * 1000);
//
//            PodSetController controller = new PodSetController(client,podSharedInformer,podSetSharedInformer);
//
//            controller.create();
//            factory.startAllRegisteredInformers();
//            System.out.println("I am then here");
//
//            controller.run();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        KubernetesClient kubernetesClient = new DefaultKubernetesClient();

        CustomResourceDefinitionContext customResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1alpha1")
                .withScope("Namespaces")
                .withGroup("demo.k8s.io")
                .withPlural("podsets")
                .build();

        SharedInformerFactory sharedInformerFactory = kubernetesClient.informers();

        SharedIndexInformer<Pod> podSharedIndexInformer =  sharedInformerFactory.sharedIndexInformerFor(Pod.class, PodList.class,1 * 60 * 1000);

        SharedIndexInformer<Memcached> memcachedSharedIndexInformer = sharedInformerFactory
                .sharedIndexInformerForCustomResource(customResourceDefinitionContext,Memcached.class, MemcachedList.class,1 * 60 * 1000);

        MemcachedController memcachedController =  new MemcachedController(kubernetesClient,podSharedIndexInformer,memcachedSharedIndexInformer,sharedInformerFactory);
        memcachedController.create();
        memcachedController.run();
    }
}

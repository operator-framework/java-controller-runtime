import static org.junit.Assert.*;

import io.fabric8.controller.controller_runtime.Controllers;
import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ControllerWatchTest {
//    private V1Pod testPod =
//            new V1Pod().metadata(new V1ObjectMeta().namespace("ns1").name("pod1")).spec(new V1PodSpec());

    BlockingQueue<Request> workQueue;


    public ControllerWatchTest(){
        workQueue = new ArrayBlockingQueue<>(1024);
    }

    @Test
    public void testOnAdd()  throws IOException, InterruptedException {




        System.out.println("test on add");
        try (final KubernetesClient client = new DefaultKubernetesClient()) {
            // Given
            CustomResourceDefinition animalCrd = client.customResourceDefinitions()
                    .load(getClass().getResourceAsStream("/animal.yml")).get();
            // When

            System.out.println(animalCrd);
            animalCrd = client.customResourceDefinitions().create(animalCrd);
            // Then
            assertNotNull(animalCrd);
        }
//        try (final KubernetesClient client = new DefaultKubernetesClient()) {
//            System.out.println("client");
//            String namespace = "default";
//
//            // Load CRD as object from YAML
//            CustomResourceDefinition animalCrd = client.customResourceDefinitions()
//                    .load(ControllerWatchTest.class.getResourceAsStream("animal.yml")).get();
//            // Apply CRD object onto your Kubernetes cluster
//            System.out.print("coming here");
//            System.out.println(animalCrd);
//            client.customResourceDefinitions().create(animalCrd);
//
//            CustomResourceDefinitionContext animalCrdContext = new CustomResourceDefinitionContext.Builder()
//                    .withName("animals.jungle.example.com")
//                    .withGroup("jungle.example.com")
//                    .withScope("Namespaced")
//                    .withVersion("v1")
//                    .withPlural("animals")
//                    .build();
//        }
//        // watch apply according to fabric8
//        DefaultControllerWatch<V1Pod> controllerWatch =
//                new DefaultControllerWatch(V1Pod.class, workQueue, Controllers.defaultReflectiveKeyFunc());
//
//        controllerWatch.getResourceEventHandler().onAdd(testPod);
//        assertEquals(1, workQueue.length());

//        controllerWatch.setOnAddFilterPredicate((V1Pod addedPod) -> false);
//        controllerWatch.getResourceEventHandler().onAdd(testPod);
//        assertEquals(1, workQueue.length());
    }

//    @Test
//    public void testOnUpdate() {
//        WorkQueue<Request> workQueue = new DefaultWorkQueue<>();
//        DefaultControllerWatch<V1Pod> controllerWatch =
//                new DefaultControllerWatch(V1Pod.class, workQueue, Controllers.defaultReflectiveKeyFunc());
//        controllerWatch.getResourceEventHandler().onUpdate(null, testPod);
//        assertEquals(1, workQueue.length());
//
//        controllerWatch.setOnUpdateFilterPredicate((V1Pod oldPod, V1Pod newPod) -> false);
//        controllerWatch.getResourceEventHandler().onUpdate(null, testPod);
//        assertEquals(1, workQueue.length());
//    }
//
//    @Test
//    public void testOnDelete() {
//        WorkQueue<Request> workQueue = new DefaultWorkQueue<>();
//        DefaultControllerWatch<V1Pod> controllerWatch =
//                new DefaultControllerWatch(V1Pod.class, workQueue, Controllers.defaultReflectiveKeyFunc());
//        controllerWatch.getResourceEventHandler().onDelete(testPod, false);
//        assertEquals(1, workQueue.length());
//
//        controllerWatch.setOnDeleteFilterPredicate((V1Pod newPod, Boolean stateUnknown) -> false);
//        controllerWatch.getResourceEventHandler().onDelete(testPod, false);
//        assertEquals(1, workQueue.length());
//    }
}


import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.memcached.operator.controller.MemcachedController;
import io.fabric8.memcached.operator.memcached_types.DoneablePodSet;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import io.fabric8.memcached.operator.memcached_types.MemcachedList;
import io.fabric8.memcached.operator.memcached_types.MemcachedSpec;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableRuleMigrationSupport
public class PodSetControllerTest {
    @Rule
    public KubernetesServer server = new KubernetesServer();

//    private static final CustomResourceDefinition podSetCustomResourceDefinition = new CustomResourceDefinitionBuilder()
//            .withNewMetadata().withName("podsets.demo.k8s.io").endMetadata()
//            .withNewSpec()
//            .withGroup("demo.k8s.io")
//            .withVersion("v1alpha1")
//            .withNewNames().withKind("PodSet").withPlural("podsets").endNames()
//            .withScope("Namespaced")
//            .endSpec()
//            .build();

//    private static final CustomResourceDefinitionContext podSetCustomResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
//            .withVersion("v1alpha1")
//            .withScope("Namespaced")
//            .withGroup("demo.k8s.io")
//            .withPlural("podsets")
//            .build();

    CustomResourceDefinitionContext podSetCustomResourceDefinitionContext = new CustomResourceDefinitionContext.Builder()
            .withVersion("v1alpha1")
            .withScope("Namespaces")
            .withGroup("demo.k8s.io")
            .withPlural("podsets")
            .build();

    private static final long RESYNC_PERIOD_MILLIS = 10 * 60 * 1000L;

    @Test
    @DisplayName("Should create pods for with respect to a specified PodSet")
    public void testReconcile() throws InterruptedException {
        // Given
        String testNamespace = "ns1";

        Memcached testPodSet = getPodSet("example-podset", testNamespace, "0800cff3-9d80-11ea-8973-0e13a02d8ebd");
        server.expect().post().withPath("/api/v1/namespaces/" + testNamespace + "/pods")
                .andReturn(HttpURLConnection.HTTP_CREATED, new PodBuilder().withNewMetadata().withName("pod1-clone").endMetadata().build())
                .times(testPodSet.getSpec().getSize());
        KubernetesClient client = server.getClient();

        SharedInformerFactory informerFactory = client.informers();
       // MixedOperation<Memcached, MemcachedList, DoneablePodSet, Resource<Memcached, DoneablePodSet>> podSetClient = client.customResources(podSetCustomResourceDefinition, Memcached.class, MemcachedList.class, DoneablePodSet.class);
        SharedIndexInformer<Pod> podSharedIndexInformer = informerFactory.sharedIndexInformerFor(Pod.class, PodList.class, RESYNC_PERIOD_MILLIS);
        SharedIndexInformer<Memcached> podSetSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(podSetCustomResourceDefinitionContext, Memcached.class, MemcachedList.class, RESYNC_PERIOD_MILLIS);

        // check this
       // PodSetController podSetController = new PodSetController(client, podSetClient, podSharedIndexInformer, podSetSharedIndexInformer, testNamespace);

        // When


       // podSetController.reconcile(testPodSet);

        MemcachedController memcachedController =  new MemcachedController(client,podSharedIndexInformer,podSharedIndexInformer,informerFactory);
        memcachedController.create();
        memcachedController.run();

        // Then
//        RecordedRequest recordedRequest = server.getLastRequest();
//        assertEquals("POST", recordedRequest.getMethod());
//        assertTrue(recordedRequest.getBody().readUtf8().contains(testPodSet.getMetadata().getName()));
    }

    private Memcached getPodSet(String name, String testNamespace, String uid) {
        Memcached podSet = new Memcached();
        MemcachedSpec podSetSpec = new MemcachedSpec();
        podSetSpec.setSize(5);

        podSet.setSpec(podSetSpec);
        podSet.setMetadata(new ObjectMetaBuilder().withName(name).withNamespace(testNamespace).withUid(uid).build());
        System.out.println(podSet.getMetadata().getName());
        System.out.println(podSet.getMetadata());
        return podSet;
    }
}

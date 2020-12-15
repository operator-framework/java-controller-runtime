import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Rule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;

import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableRuleMigrationSupport
public class PodMockTest {

    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Test
    @DisplayName("Should list pods in all namespaces")
    public void testPodGet() {
        // Given

        System.out.println("Started here");

        PodList expectedPodList = new PodListBuilder().withItems(
                new PodBuilder().withNewMetadata().withName("pod1").endMetadata()
                        .build(),
                new PodBuilder().withNewMetadata().withName("pod2").endMetadata()
                        .build()).build();

        server.expect().get().withPath("/api/v1/pods")
                .andReturn(HttpURLConnection.HTTP_OK, expectedPodList)
                .once();

        KubernetesClient client = server.getClient();

        // When
        PodList podList = client.pods().inAnyNamespace().list();

        // Then
        assertNotNull(podList);
        System.out.println(podList);
        assertEquals(2, podList.getItems().size());


        System.out.println("ENDED here");
    }

//    @Test
//    @DisplayName("Should be able to list pods in default namespace")
//    public void testPodGetInDefaultNamespace() {
//        // Given
//            server.expect().get().withPath("/api/v1/namespaces/default/pods")
//                .andReturn(HttpURLConnection.HTTP_OK, new PodListBuilder().build())
//                .once();
//
//        KubernetesClient client = server.getClient();
//
//        // When
//        PodList podList = client.pods().inNamespace("default").list();
//
//        // Then
//        System.out.println(podList.getItems().isEmpty());
//        System.out.println(podList.getItems().size());
//        assertTrue(podList.getItems().isEmpty());
//    }

    @Test
    @DisplayName("Should create a new resource in Kubernetes Cluster")
    void testResourceCreate() {
        server.expect().post().withPath("/api/v1/namespaces/test/pods").andReturn(HttpURLConnection.HTTP_CREATED, new PodBuilder()
                .withNewMetadata().withResourceVersion("12345").and().build()).once();

        KubernetesClient client = server.getClient();
        HasMetadata result = client.resource(new PodBuilder().withNewMetadata().withName("pod123").and().withNewSpec().and().build()).createOrReplace();
        assertNotNull(result);
        System.out.println(result.getMetadata().getResourceVersion());
        assertEquals("12345", result.getMetadata().getResourceVersion());
    }



}

package io.fabric8.memcached.operator.controller;

import io.fabric8.controller.controller_runtime.pkg.Reconciler;
import io.fabric8.controller.controller_runtime.pkg.Request;
import io.fabric8.controller.controller_runtime.pkg.Result;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemcachedReconciler implements Reconciler {

    private Lister<Memcached> memcachedLister;
    private Lister<Pod> podLister;
    public KubernetesClient kubernetesClient;

    /**
     * Instantiates a new Memcached Reconciler.
     *
     * @param kubernetesClient the kubernetes client
     * @param podLister the pod lister
     * @param memcachedLister the memcached lister
     */
    public MemcachedReconciler(KubernetesClient kubernetesClient, Lister<Pod> podLister, Lister<Memcached> memcachedLister){
        this.kubernetesClient = kubernetesClient;
        this.podLister = podLister;
        this.memcachedLister = memcachedLister;
    }

    public MemcachedReconciler(){

    }

    @Override
    public Result reconcile(Request request) {

        System.out.println("Reconcilation started"+request.getNamespace()+" name   "+request.getName());
        Memcached memcached =  this.memcachedLister.namespace(request.getNamespace()).get(request.getName());
        List<String> pods = podCountByLabel("app",memcached.getMetadata().getName());

        int existingPods = pods.size();
        int desiredPods = memcached.getSpec().getSize();
        if(existingPods < desiredPods){
            createPod(desiredPods-existingPods,memcached);
        }
        else if(desiredPods < existingPods){
            int diff = existingPods - desiredPods;
            for(int i=0;i<diff;i++) {
                String podName = pods.remove(0);
                kubernetesClient.pods().inNamespace(memcached.getMetadata().getNamespace()).withName(podName).delete();
            }
        }
        return null;
    }

    /**
     * This function will list out the pod names and return he entire list
     *
     * @param label the label
     * @param memcachedName the memcached name
     * @return podNames the list of pod names
     */
    private List<String> podCountByLabel(String label, String memcachedName){
        List<String> podNames = new ArrayList<>();
        List<Pod> pods = podLister.list();

        for(Pod pod : pods) {
            if (pod.getMetadata().getLabels().entrySet().contains(new AbstractMap.SimpleEntry<>(label, memcachedName))) {
                podNames.add(pod.getMetadata().getName());
            }
        }
        return podNames;
    }

    /**
     * It will create the pod according to the number of pods specified in the input.
     *
     * @param noOfPods the number of pods
     * @param memcached the memcached object
     */
    private void createPod(int noOfPods, Memcached memcached){
        for(int i = 0;i<noOfPods;i++){
            Pod pod = createNewPod(memcached);
            kubernetesClient.pods().inNamespace(memcached.getMetadata().getNamespace()).create(pod);
        }
    }

    /**
     * It will create the pod.
     *
     * @param memcached the memcached object
     * @return Pod the Pod object
     */
    private Pod createNewPod(Memcached memcached){
        return new PodBuilder()
                .withNewMetadata()
                .withGenerateName(memcached.getMetadata().getName() + "-pod")
                .withLabels(Collections.singletonMap("app",memcached.getMetadata().getName()))
                .addNewOwnerReference().withController(true).withKind("PodSet").withApiVersion("demo.k8s.io/v1alpha1").withName(memcached.getMetadata().getName()).withNewUid(memcached.getMetadata().getUid()).endOwnerReference()
                .endMetadata()
                .withNewSpec()
                .addNewContainer().withName("busybox").withImage("busybox").withCommand("sleep","3600").endContainer()
                .endSpec()
                .build();
    }
}

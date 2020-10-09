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

package io.fabric8.memcached.operator;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import io.fabric8.memcached.operator.controller.MemcachedController;
import io.fabric8.memcached.operator.memcached_types.Memcached;
import io.fabric8.memcached.operator.memcached_types.MemcachedList;

public class MemcachedMain {

    /** Main method where operator execution starts **/
    public static void main(String args[]) throws InterruptedException {
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

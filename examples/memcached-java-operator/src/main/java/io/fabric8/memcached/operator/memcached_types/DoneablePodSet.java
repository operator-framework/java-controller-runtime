package io.fabric8.memcached.operator.memcached_types;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;


public class DoneablePodSet extends CustomResourceDoneable<Memcached> {
    public DoneablePodSet(Memcached resource, Function function) { super(resource, function); }
}

package io.fabric8.memcached.operator.memcached_types;

import io.fabric8.kubernetes.client.CustomResource;

public class Memcached extends CustomResource {

    /**
     * Gets spec.
     *
     * @return the get spec
     */
    public MemcachedSpec getSpec() {
        return spec;
    }

    /**
     * Sets spec.
     *
     * @param spec the spec
     */
    public void setSpec(MemcachedSpec spec) {
        this.spec = spec;
    }

    /**
     * Gets status.
     *
     * @return the get status
     */
    public MemcachedStatus getStatus() {
        return Status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(MemcachedStatus status) {
        Status = status;
    }

    private MemcachedSpec spec;
    private MemcachedStatus Status;
}

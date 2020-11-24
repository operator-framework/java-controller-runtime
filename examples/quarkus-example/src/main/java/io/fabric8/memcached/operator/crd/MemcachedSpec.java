package io.fabric8.memcached.operator.crd;

public class MemcachedSpec {

    private int size;

    /**
     * Gets Size.
     *
     * @return the get Size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets Size.
     *
     * @param size the spec
     */
    public void setSize(int size) {
        this.size = size;
    }
}

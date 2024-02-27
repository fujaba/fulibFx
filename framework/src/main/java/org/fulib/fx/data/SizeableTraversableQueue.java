package org.fulib.fx.data;

public interface SizeableTraversableQueue<T> extends TraversableQueue<T> {

    /**
     * Sets the maximum size of the queue.
     * If the current size is larger than the new size, the oldest elements will be removed.
     *
     * @param size the new size
     */
    void setSize(int size);

    /**
     * Returns the maximum size of the queue.
     *
     * @return The maximum size of the queue.
     */
    int size();


}

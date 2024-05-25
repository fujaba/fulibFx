package org.fulib.fx.data;

import java.util.List;

/**
 * Interface for a queue that can be traversed by going forwards or backwards.
 *
 * @param <T> The type of items saved in the queue
 */
public interface TraversableQueue<T> {

    void insert(T value);

    T peek();

    T back();

    T forward();

    T current();

    List<T> entries();

    int length();

}

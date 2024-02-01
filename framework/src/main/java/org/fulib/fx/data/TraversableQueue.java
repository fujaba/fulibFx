package org.fulib.fx.data;

import java.util.List;

public interface TraversableQueue<T> {

    void insert(T value);

    T peek();

    T back();

    T forward();

    T current();

    List<T> entries();

    int size();

}

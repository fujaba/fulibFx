package org.fulib.fx.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.fulib.fx.util.FrameworkUtil.error;

public class EvictingQueue<T> implements SizeableTraversableQueue<T> {

    private int size;

    private final ArrayList<T> list;

    private int currentIndex = 0;

    public EvictingQueue(int size) {
        this.size = size;
        this.list = new ArrayList<>();
    }

    @Override
    public void insert(T value) {

        // If we're not at the end of the list, remove all elements after the current index
        if (currentIndex < list.size() - 1) {
            list.subList(currentIndex + 1, list.size()).clear();
        }

        // Add the element at the last position
        list.add(value);
        currentIndex = list.size() - 1;

        // If the list is too big, remove the first (oldest) element
        if (list.size() > size) {
            list.remove(0);
            currentIndex--;
        }
    }

    @Override
    public T peek() {
        return list.get(list.size() - 1);
    }

    @Override
    public T back() {
        if (currentIndex > 0) {
            return list.get(--currentIndex);
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public T forward() {
        if (currentIndex < list.size() - 1) {
            return list.get(++currentIndex);
        }
        throw new IndexOutOfBoundsException(error(5000));
    }

    @Override
    public T current() {
        if (list.isEmpty()) {
            throw new IndexOutOfBoundsException(error(5001));
        }
        return list.get(currentIndex);
    }

    @Override
    public List<T> entries() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EvictingQueue<?> that = (EvictingQueue<?>) o;
        return Objects.equals(list, that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, currentIndex, size);
    }

    @Override
    public int length() {
        return list.size();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void setSize(int size) {
        if (size < 1) throw new IllegalArgumentException("Size must be at least 1");
        if (size == this.size) return;
        if (currentIndex < list.size() - size)
            throw new IllegalArgumentException("Cannot update size while the current index is outside the new bounds");

        this.size = size;
        if (list.size() > size) {
            list.subList(0, list.size() - size).clear();
            // Update the current index to the correct position
            currentIndex -= list.size() - size;
        }
    }
}

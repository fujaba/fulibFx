package io.github.sekassel.jfxframework.data;

import java.util.*;

public class EvictingQueue<T> implements TraversableQueue<T> {

    private final int size;

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
        throw new IndexOutOfBoundsException("No previous element saved");
    }

    @Override
    public T forward() {
        if (currentIndex < list.size() - 1) {
            return list.get(++currentIndex);
        }
        throw new IndexOutOfBoundsException("No next element saved");
    }

    @Override
    public T current() {
        if (list.isEmpty()) {
            throw new IndexOutOfBoundsException("Queue is empty");
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
}

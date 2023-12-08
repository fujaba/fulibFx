package io.github.sekassel.jfxframework.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TraversableQueueTest {

    @Test
    public void insert() {
        TraversableQueue<Integer> queue = new EvictingQueue<>(5);

        assertTrue(queue.entries().isEmpty());

        assertThrows(IndexOutOfBoundsException.class, queue::current);

        queue.insert(1);
        assertEquals(1, queue.current());
        assertEquals(1, queue.peek());

        queue.insert(2);
        assertEquals(2, queue.current());
        assertEquals(2, queue.peek());

        queue.insert(3);
        queue.insert(4);
        queue.insert(5);

        assertEquals(List.of(1, 2, 3, 4, 5), queue.entries());

        queue.insert(6);

        assertEquals(6, queue.current());
        assertEquals(6, queue.peek());

        assertEquals(List.of(2, 3, 4, 5, 6), queue.entries());

    }

    @Test
    public void traverse() {
        TraversableQueue<Integer> queue = new EvictingQueue<>(3);
        queue.insert(1);
        queue.insert(2);
        queue.insert(3);

        assertEquals(3, queue.current());

        assertEquals(2, queue.back());
        assertEquals(2, queue.current());
        assertEquals(3, queue.peek());

        assertEquals(1, queue.back());
        assertEquals(1, queue.current());

        assertThrows(IndexOutOfBoundsException.class, queue::back);

        assertEquals(2, queue.forward());
        assertEquals(3, queue.forward());

        assertThrows(IndexOutOfBoundsException.class, queue::forward);

    }

    @Test
    public void insertMiddle() {
        TraversableQueue<Integer> queue = new EvictingQueue<>(5);

        queue.insert(1);
        queue.insert(2);
        queue.insert(3);
        queue.insert(4);
        queue.insert(5);

        assertEquals(4, queue.back());
        assertEquals(3, queue.back());
        assertEquals(2, queue.back());

        queue.insert(6);

        assertEquals(6, queue.current());
        assertEquals(6, queue.peek());

        assertEquals(List.of(1, 2, 6), queue.entries());

    }

}

package org.fulib.fx;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUtil {

    private TestUtil() {
        // Prevent instantiation
    }

    /**
     * Asserts that the expected list and the actual list have the same content.
     *
     * @param expected The expected list
     * @param actual   The actual list
     */
    public static void assertSameContent(List<?> expected, List<?> actual) {
        assertEquals(expected.size(), actual.size());
        expected.forEach(e -> {
            if (!actual.contains(e)) {
                throw new AssertionError("Expected " + e + " in " + actual);
            }
        });
    }

    /**
     * Asserts that the actual list contains all elements of the expected list.
     *
     * @param expected The expected list
     * @param actual   The actual list
     */
    public static void containsAll(List<?> expected, List<?> actual) {
        expected.forEach(e -> {
            if (!actual.contains(e)) {
                throw new AssertionError("Expected " + e + " in " + actual);
            }
        });
    }

}

package org.fulib.fx.data;

import org.fulib.fx.util.reflection.Reflection;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReflectionTest {

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            int.class, Integer.class,
            char.class, Character.class,
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    @Test
    public void testWrapping() {
        for (var entry : PRIMITIVE_TO_WRAPPER.entrySet()) {
            var primitive = entry.getKey();
            var wrapper = entry.getValue();
            assertEquals(wrapper, Reflection.wrap(primitive));
            assertEquals(primitive, Reflection.unwrap(wrapper));
        }
    }

    @Test
    public void testNonPrimitiveWrapping() {
        assertEquals(String.class, Reflection.wrap(String.class));
        assertEquals(String.class, Reflection.unwrap(String.class));
    }

}

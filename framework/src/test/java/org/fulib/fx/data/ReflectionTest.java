package org.fulib.fx.data;

import org.fulib.fx.util.reflection.Reflection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
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

    static class Example {
        private int number;
        private String text;

        public void method() {
        }
    }

    @Test
    public void listFieldsAndMethods() {
        assertEquals(List.of("number", "text"), Reflection.getAllFields(Example.class).stream().map(Field::getName).toList());
        assertEquals(List.of("method"), Reflection.getAllMethods(Example.class, false).stream().map(Method::getName).toList());
        assertEquals(List.of("method", "finalize", "wait0", "equals", "toString", "hashCode", "getClass", "clone", "notify", "notifyAll", "wait", "wait", "wait"), Reflection.getAllMethods(Example.class, true).stream().map(Method::getName).toList());
    }

}

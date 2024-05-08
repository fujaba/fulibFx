package org.fulib.fx.data;

import org.fulib.fx.TestUtil;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.ReflectionUtil;
import org.fulib.fx.util.reflection.Reflection;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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

        public void method3() {
        }

        @OnInit
        public void onInit() {
        }

        @OnRender
        public void onRender() {
        }

        @OnDestroy
        public void onDestroy() {
        }

        @OnKey
        public void onKey() {
        }
    }

    static class Example2 extends Example {

        @Override
        public void method() {
        }

        public void method2() {
        }
    }

    static class Example3 extends Example2 {

        @Override
        public void method3() {
        }
    }


    @Test
    public void listFieldsAndMethods() {
        TestUtil.containsAll(List.of("number", "text"), Reflection.getAllFields(Example.class).stream().map(Field::getName).toList());
        TestUtil.containsAll(List.of("method", "onDestroy", "onRender", "onInit", "method3", "onKey"), Reflection.getAllMethods(Example.class, false).stream().map(Method::getName).toList());
        TestUtil.containsAll(List.of("method", "finalize", "equals", "toString", "hashCode", "getClass", "clone", "notify", "notifyAll", "wait", "wait", "wait"), Reflection.getAllMethods(Example.class, true).stream().map(Method::getName).toList());
    }

    @Test
    public void override() throws NoSuchMethodException {
        Method method1 = Example2.class.getMethod("method");
        Method method2 = Example2.class.getMethod("method2");
        Method method3 = Example3.class.getMethod("method3");

        assertEquals(Example.class.getMethod("method"), ReflectionUtil.getOverriding(method1));
        assertEquals(Example.class.getMethod("method3"), ReflectionUtil.getOverriding(method3));
        assertNull(ReflectionUtil.getOverriding(method2));
    }

    @Test
    public void eventMethods() throws NoSuchMethodException {
        Method init = Example.class.getMethod("onInit");
        Method render = Example.class.getMethod("onRender");
        Method destroy = Example.class.getMethod("onDestroy");
        Method key = Example.class.getMethod("onKey");
        Method other = Example.class.getMethod("method");

        assertTrue(ControllerUtil.isEventMethod(init));
        assertTrue(ControllerUtil.isEventMethod(render));
        assertTrue(ControllerUtil.isEventMethod(destroy));
        assertTrue(ControllerUtil.isEventMethod(key));
        assertFalse(ControllerUtil.isEventMethod(other));

    }

}

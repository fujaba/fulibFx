package org.fulib.fx.util.reflection;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Utility class containing different helper methods for reflection.
 */
public class Reflection {

    private Reflection() {
        // Prevent instantiation
    }

    /**
     * Returns all fields of the given class that are annotated with the given annotation.
     *
     * @param clazz      The class to get the fields from
     * @param annotation The annotation to filter the fields by
     * @return A list of fields that are annotated with the given annotation
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation)).toList();
    }

    /**
     * Calls the given method for all fields of the given class that are annotated with the given annotation.
     *
     * @param instance The instance to call the methods on
     * @param fields   The fields to call the methods for
     * @param method   The method to call
     */
    public static void callMethodsForFieldInstances(Object instance, Collection<Field> fields, Consumer<Object> method) {
        for (Field field : fields) {
            try {
                boolean accessible = field.canAccess(instance);
                field.setAccessible(true);
                Object component = field.get(instance);
                field.setAccessible(accessible);
                method.accept(component);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't run method for field '%s' in class '%s'.".formatted(field.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Returns all fields of the given class that are of the given type.
     *
     * @param clazz The class to get the fields from
     * @param type  The type to filter the fields by
     * @return A list of fields that are of the given type
     */
    public static List<Field> getFieldsOfType(Class<?> clazz, Class<?> type) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getType().equals(type)).toList();
    }

    /**
     * Returns all methods of the given class that are annotated with the given annotation.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A list of methods that are annotated with the given annotation
     */
    public static List<Method> getMethodsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(annotation)).toList();
    }

    /**
     * Checks if the value can be assigned to the given type.
     *
     * @param type  The type to check
     * @param value The value to check
     * @return True if the value can be assigned to the type, false otherwise
     */
    public static boolean canBeAssigned(Class<?> type, Object value) {
        if (value == null) {
            return !type.isPrimitive();
        }

        Class<?> valueType = value.getClass();
        return getWrapperType(type).isAssignableFrom(valueType);
    }


    /**
     * Returns all fields of the given class and its superclasses.
     *
     * @param clazz The class to get the fields from
     * @return A set of all fields of the given class and its superclasses
     */
    public static @NotNull Set<Field> getAllFields(@NotNull Class<?> clazz) {

        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));

        // Recursively add fields from superclass until it reaches Object class
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            fields.addAll(getAllFields(superClass));
        }

        return fields;
    }

    public static Class<?> getWrapperType(Class<?> type) {
        return type.isPrimitive() ? wrap(type) : type;
    }

    // https://stackoverflow.com/questions/1704634/simple-way-to-get-wrapper-class-type-in-java/62219759#62219759
    @SuppressWarnings("unchecked")
    public static <T> Class<T> wrap(Class<T> unwrapped) {
        return (Class<T>) MethodType.methodType(unwrapped).wrap().returnType();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> unwrap(Class<T> wrapped) {
        return (Class<T>) MethodType.methodType(wrapped).unwrap().returnType();
    }

}
package org.fulib.fx.util.reflection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.fulib.fx.util.FrameworkUtil.error;

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
     * @return A stream of fields that are annotated with the given annotation
     */
    public static Stream<Field> getFieldsWithAnnotation(@NotNull Class<?> clazz, @NotNull Class<? extends @NotNull Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(annotation));
    }

    /**
     * Returns all fields of the given class (or a subclass) that are annotated with the given annotation.
     *
     * @param clazz      The class to get the fields from
     * @param annotation The annotation to filter the fields by
     * @return A stream of fields that are annotated with the given annotation
     */
    public static Stream<Field> getAllFieldsWithAnnotation(@NotNull Class<?> clazz, @NotNull Class<? extends @NotNull Annotation> annotation) {
        return getAllFields(clazz).stream().filter(field -> field.isAnnotationPresent(annotation));
    }

    /**
     * Calls the given method for all fields of the given class that are annotated with the given annotation.
     *
     * @param instance The instance to call the methods on
     * @param fields   The fields to call the methods for
     * @param method   The method to call
     */
    public static void callMethodsForFieldInstances(@NotNull Object instance, @NotNull Collection<@NotNull Field> fields, @NotNull Consumer<@NotNull Object> method) {
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object component = field.get(instance);
                method.accept(component);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(error(9000).formatted(field.getName(), instance.getClass().getName()), e);
            }
        }
    }

    /**
     * Returns all fields of the given class that are of the given type.
     *
     * @param clazz The class to get the fields from
     * @param type  The type to filter the fields by
     * @return A stream of fields that are of the given type
     */
    public static Stream<Field> getFieldsOfType(@NotNull Class<?> clazz, @NotNull Class<?> type) {
        return Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.getType().equals(type));
    }

    /**
     * Returns all fields of the given class (or a super class) that are of the given type.
     *
     * @param clazz The class to get the fields from
     * @param type  The type to filter the fields by
     * @return A stream of fields that are of the given type
     */
    public static Stream<Field> getAllFieldsOfType(@NotNull Class<?> clazz, @NotNull Class<?> type) {
        return getAllFields(clazz).stream().filter(field -> field.getType().equals(type));
    }

    /**
     * Returns all methods of the given class that are annotated with the given annotation.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation
     */
    public static Stream<Method> getMethodsWithAnnotation(@NotNull Class<?> clazz, @NotNull Class<? extends @NotNull Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredMethods()).filter(method -> method.isAnnotationPresent(annotation));
    }

    /**
     * Returns all methods of the given class (or a super class) that are annotated with the given annotation.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation
     */
    public static Stream<Method> getAllMethodsWithAnnotation(@NotNull Class<?> clazz, @NotNull Class<? extends @NotNull Annotation> annotation) {
        return getAllMethods(clazz, false).stream().filter(method -> method.isAnnotationPresent(annotation));
    }

    /**
     * Checks if the value can be assigned to the given type.
     *
     * @param type  The type to check
     * @param value The value to check
     * @return True if the value can be assigned to the type, false otherwise
     */
    public static boolean canBeAssigned(@NotNull Class<?> type, @Nullable Object value) {
        if (value == null) {
            return !type.isPrimitive();
        }

        Class<?> valueType = value.getClass();
        return getWrapperType(type).isAssignableFrom(valueType);
    }

    public static Class<?> getWrapperType(@NotNull Class<?> type) {
        return type.isPrimitive() ? wrap(type) : type;
    }

    // https://stackoverflow.com/questions/1704634/simple-way-to-get-wrapper-class-type-in-java/62219759#62219759
    @SuppressWarnings("unchecked")
    public static <T> Class<T> wrap(@NotNull Class<T> unwrapped) {
        return (Class<T>) MethodType.methodType(unwrapped).wrap().returnType();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> unwrap(@NotNull Class<T> wrapped) {
        return (Class<T>) MethodType.methodType(wrapped).unwrap().returnType();
    }

    /**
     * Returns all fields of a class including private and inherited fields.
     *
     * @param clazz The class to get the fields from
     * @return A list of all fields of the given class
     */
    public static List<Field> getAllFields(@NotNull Class<?> clazz) {
        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        while (clazz.getSuperclass() != null) {
            clazz = clazz.getSuperclass();
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields;
    }

    /**
     * Returns all methods of a class including private and inherited methods.
     *
     * @param clazz The class to get the methods from
     * @return A list of all methods of the given class
     */
    public static List<Method> getAllMethods(@NotNull Class<?> clazz, boolean includeObjectMethods) {
        List<Method> methods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
        while (clazz.getSuperclass() != null && (includeObjectMethods || clazz.getSuperclass() != Object.class)) {
            clazz = clazz.getSuperclass();
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        }
        return methods;
    }

}
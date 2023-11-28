package io.github.sekassel.jfxframework.util.reflection;

import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.jfxframework.controller.annotation.Params;
import io.github.sekassel.jfxframework.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
     * Calls all methods annotated with a certain annotation in the provided controller. The method will be called with the given parameters if they're annotated with @Param or @Params.
     *
     * @param annotation The annotation to look for
     * @param parameters The parameters to pass to the methods
     */
    public static void callMethodsWithAnnotation(@NotNull Object instance, @NotNull Class<? extends Annotation> annotation, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        for (Method method : Reflection.getMethodsWithAnnotation(instance.getClass(), annotation)) {
            try {
                method.invoke(instance, getApplicableParameters(method, parameters));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Couldn't run method '" + method.getName() + "' annotated with '" + annotation.getName() + "' in '" + instance.getClass().getName() + "'", e);
            }
        }
    }

    /**
     * Returns an array with all parameters that are applicable to the given method in the correct order.
     * <p>
     * If the method has a parameter annotated with @Param, the value of the parameter with the same key as the annotation will be used.
     * <p>
     * If the method has a parameter annotated with @Params, the whole parameters map will be used.
     *
     * @param method     The method to check
     * @param parameters The values of the parameters
     * @return An array with all applicable parameters
     */
    private static @Nullable Object @NotNull [] getApplicableParameters(@NotNull Method method, @NotNull Map<String, Object> parameters) {
        return Arrays.stream(method.getParameters()).map(parameter -> {
            Param param = parameter.getAnnotation(Param.class);
            Params params = parameter.getAnnotation(Params.class);

            if (param != null && params != null)
                throw new RuntimeException("Parameter '" + parameter.getName() + "' in method '" + method.getDeclaringClass().getName() + "#" + method.getName() + "' is annotated with both @Param and @Params");

            // Check if the parameter is annotated with @Param and if the parameter is of the correct type
            if (param != null) {
                if (parameters.containsKey(param.name()) && !parameter.getType().isAssignableFrom(parameters.get(param.name()).getClass())) {
                    throw new RuntimeException("Parameter named '" + param.name() + "' in method '" + method.getDeclaringClass().getName() + "#" + method.getName() + "' is of type " + parameter.getType().getName() + " but the provided value is of type " + parameters.get(param.name()).getClass().getName());
                }
                return parameters.get(param.name());
            }

            // Check if the parameter is annotated with @Params and if the parameter is of the type Map<String, Object>
            if (params != null) {
                if (!Util.isMapWithTypes(parameter, String.class, Object.class)) {
                    throw new RuntimeException("Parameter annotated with @Params in method '" + method.getClass().getName() + "#" + method.getName() + "' is not of type " + Map.class.getName());
                }
                return parameters;
            }

            return null;
        }).toArray();
    }

}

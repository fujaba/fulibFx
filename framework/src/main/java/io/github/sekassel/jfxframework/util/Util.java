package io.github.sekassel.jfxframework.util;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.Route;
import io.github.sekassel.jfxframework.controller.exception.InvalidRouteFieldException;
import javafx.scene.Node;
import javafx.scene.control.Button;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.reflect.*;
import java.util.*;

public class Util {

    private Util() {
        // Prevent instantiation
    }

    /**
     * Transforms a class name to a fxml file name or id using the default naming scheme. Used if no path is specified in the {@link Controller#view()} annotation.
     * <p>
     * Example: ExampleController --> example
     * <p>
     * Note that this method could result in funky names if the class name doesn't follow the naming scheme.
     *
     * @param className The name of the class (should be {@link Class#getName()} or {@link Class#getSimpleName()}
     * @return The transformed name
     */
    public static String transform(String className) {
        String[] classes = className.split("\\.");
        return classes[classes.length - 1].replace("Controller", "").toLowerCase();
    }

    /**
     * Checks if the given field is a valid route field.
     * A valid route field is a field that is annotated with {@link Route} and is of type {@link Provider} where the generic type is a class annotated with {@link Controller}.
     *
     * @param field The field to check
     * @throws InvalidRouteFieldException If the field is not a valid route field
     */
    public static void requireControllerProvider(Field field) {
        Class<?> providedClass = getProvidedClass(field);
        if (providedClass == null || !providedClass.isAnnotationPresent(Controller.class))
            throw new InvalidRouteFieldException(field);
    }

    /**
     * Returns the class provided by the given provider field.
     *
     * @param providerField The provider field
     * @return The class provided by the provider field or null if the field is not a valid provider field
     */
    public static @Nullable Class<?> getProvidedClass(Field providerField) {
        if (providerField.getType() == Provider.class) {
            Type genericType = providerField.getGenericType();

            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                if (typeArguments.length == 1 && typeArguments[0] instanceof Class<?> genericClass) {
                    return genericClass;
                }
            }
        }
        return null;
    }


    /**
     * Checks if the given parameter is a valid map field with the given key and value types.
     *
     * @param mapParameter The parameter to check
     * @param key          The key type
     * @param value        The value type
     * @return True if the parameter is a valid map field with the given key and value types
     */
    public static boolean isMapWithTypes(Parameter mapParameter, Class<?> key, Class<?> value) {
        if (Map.class.isAssignableFrom(mapParameter.getType())) {
            Type genericType = mapParameter.getParameterizedType();

            if (genericType instanceof ParameterizedType parameterizedType) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();

                if (typeArguments.length == 2 && typeArguments[0] instanceof Class<?> genericKey && typeArguments[1] instanceof Class<?> genericValue) {
                    return genericKey == key && genericValue == value;
                }
            }
        }
        return false;
    }

    /**
     * Returns all fields of the given class and its superclasses.
     *
     * @param clazz The class to get the fields from
     * @return A set of all fields of the given class and its superclasses
     */
    public static Set<Field> getAllFields(Class<?> clazz) {

        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));

        // Recursively add fields from superclass until it reaches Object class
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            fields.addAll(getAllFields(superClass));
        }

        return fields;
    }


}

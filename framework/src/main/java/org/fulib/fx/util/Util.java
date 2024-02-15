package org.fulib.fx.util;

import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.controller.exception.InvalidRouteFieldException;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Utility class containing different helper methods for the framework or the user.
 */
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
    public static @NotNull String transform(@NotNull String className) {
        String[] classes = className.split("\\.");
        return classes[classes.length - 1].replace("Controller", "").toLowerCase();
    }

    /**
     * Checks if the given field is a valid route field.
     * A valid route field is a field that is annotated with {@link Route} and is of type {@link Provider} where the generic type is a class annotated with {@link Controller} or {@link Component}.
     *
     * @param field The field to check
     * @throws InvalidRouteFieldException If the field is not a valid route field
     */
    public static void requireControllerProvider(@NotNull Field field) {
        Class<?> providedClass = getProvidedClass(field);
        if (providedClass == null || (!providedClass.isAnnotationPresent(Controller.class) && !providedClass.isAnnotationPresent(Component.class)))
            throw new InvalidRouteFieldException(field);
    }

    /**
     * Returns the class provided by the given provider field.
     *
     * @param providerField The provider field
     * @return The class provided by the provider field or null if the field is not a valid provider field
     */
    public static @Nullable Class<?> getProvidedClass(@NotNull Field providerField) {
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
     * Checks if the given parameter is a map with the given key and value types.
     * <p>
     * This will not work for maps not directly specifying the generic types, such as MyMap extends HashMap<Key, Value>.
     *
     * @param parameter The parameter to check
     * @param key       The key type
     * @param value     The value type
     * @return True if the parameter is a valid map field with the given key and value types
     */
    public static boolean isMapWithTypes(@NotNull Parameter parameter, @NotNull Class<?> key, @NotNull Class<?> value) {
        if (Map.class.isAssignableFrom(parameter.getType())) {
            Type genericType = parameter.getParameterizedType();
            return isMapWithTypes(genericType, key, value);
        }
        return false;
    }

    /**
     * Checks if the given field is a map with the given key and value types.
     * <p>
     * This will not work for maps not directly specifying the generic types, such as MyMap extends HashMap<Key, Value>.
     *
     * @param field The field to check
     * @param key   The key type
     * @param value The value type
     * @return True if the parameter is a valid map field with the given key and value types
     */
    public static boolean isMapWithTypes(@NotNull Field field, @NotNull Class<?> key, @NotNull Class<?> value) {
        if (Map.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            return isMapWithTypes(genericType, key, value);
        }
        return false;
    }

    private static boolean isMapWithTypes(@NotNull Type type, @NotNull Class<?> key, @NotNull Class<?> value) {
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (typeArguments.length == 2 && typeArguments[0] instanceof Class<?> genericKey && typeArguments[1] instanceof Class<?> genericValue) {
                return genericKey == key && genericValue == value;
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
    public static @NotNull Set<Field> getAllFields(@NotNull Class<?> clazz) {

        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));

        // Recursively add fields from superclass until it reaches Object class
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            fields.addAll(getAllFields(superClass));
        }

        return fields;
    }

    /**
     * Returns the children list of the given parent. This method is used to access the children list of a Parent class
     * even though the method is not public in the Parent class. This should be used with caution and is mainly used
     * for duplicating nodes.
     *
     * @param clazz  The class to get the children list from
     * @param parent The parent to get the children list from
     * @return The children list of the given parent
     */
    @SuppressWarnings("unchecked")
    public static ObservableList<Node> getChildrenList(Class<?> clazz, Parent parent) {
        try {
            Method getChildren = clazz.getDeclaredMethod("getChildren");
            getChildren.setAccessible(true);
            Object childrenList = getChildren.invoke(parent);
            return (ObservableList<Node>) childrenList;
        } catch (Exception e) {
            if (clazz.getSuperclass() == Object.class)
                throw new RuntimeException("Couldn't access getChildren() method in class or superclass", e);
            return getChildrenList(clazz.getSuperclass(), parent);
        }
    }

    /**
     * Returns the key for the given value in the given map.
     *
     * @param map   The map to search in
     * @param value The value to search for
     * @param <T>   The type of the key
     * @param <E>   The type of the value
     * @return The key for the given value in the given map or null if the value is not in the map
     */
    public static <T, E> @Nullable T keyForValue(@NotNull Map<@NotNull T, @NotNull E> map, @NotNull E value) {
        Map.Entry<T, E> keyEntry = map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).findFirst().orElse(null);
        return keyEntry == null ? null : keyEntry.getKey();
    }

    /**
     * Inserts the given key and value into the given map if the key is not already present or is null.
     * If the key is already present (not null), the value will not be inserted.
     * <p>
     * If the key is already present, the value for the key will be returned.
     *
     * @param map   The map to insert the key and value into
     * @param key   The key to insert
     * @param value The value to insert
     * @param <T>   The type of the key
     * @param <E>   The type of the value
     * @return The value for the key if the key is already present, the new value otherwise
     */
    public static <T, E> @NotNull E putIfNull(@NotNull Map<@NotNull T, @NotNull E> map, @NotNull T key, @NotNull E value) {
        if (map.containsKey(key) && map.get(key) != null) return map.get(key);
        map.put(key, value);
        return value;
    }

    /**
     * Checks if an instance is a component (controller extending a Parent).
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param instance The instance to check
     * @return True if the instance is a component (controller extending a Parent)
     */
    public static boolean isComponent(@Nullable Object instance) {
        return instance != null && instance.getClass().isAnnotationPresent(Component.class) && Parent.class.isAssignableFrom(instance.getClass());
    }

    /**
     * Checks if an instance is a controller or a component.
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param instance The instance to check
     * @return True if the instance is a controller
     */
    public static boolean isController(@Nullable Object instance) {
        if (instance == null) return false;

        if (instance.getClass().isAnnotationPresent(Controller.class) && instance.getClass().isAnnotationPresent(Component.class))
            return false;
        return instance.getClass().isAnnotationPresent(Controller.class) || isComponent(instance);
    }

    /**
     * Checks if the given field is a field that can provide a component.
     *
     * @param field The field to check
     * @return True if the field is a field that can provide a component
     */
    public static boolean canProvideSubComponent(Field field) {
        if (field.getType().isAnnotationPresent(Component.class) && Parent.class.isAssignableFrom(field.getType()))
            return true; // Field is a component

        Class<?> providedClass = getProvidedClass(field);

        return providedClass != null && providedClass.isAnnotationPresent(Component.class) && Parent.class.isAssignableFrom(providedClass); // Field is a provider of a component
    }

    /**
     * Returns the content of the given file as a string.
     *
     * @param file The file to read
     * @return The content of the given file as a string or an empty string if the file couldn't be read
     */
    public static @NotNull String getContent(@NotNull File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Returns the file representation of the given resource in the resources folder of the given class.
     *
     * @param clazz    The class to get the resource from
     * @param resource The resource to read
     * @return The file of the given resource
     */
    public static @NotNull File getResourceAsLocalFile(Class<?> clazz, String resource) {
        String classPath = clazz.getPackageName().replace(".", "/");
        Path path = FulibFxApp.resourcesPath();
        return path.resolve(classPath).resolve(resource).toFile();
    }

    /**
     * Checks if the framework is running in development mode. This is the case if the INDEV environment variable is set to true.
     * <p>
     * Since people are dumb and might not set the variable correctly, it also checks if the intellij launcher is used.
     *
     * @return True if the framework is running in development mode
     */
    public static boolean runningInDev() {
        return System.getenv().getOrDefault(Constants.INDEV_ENVIRONMENT_VARIABLE, "false").equalsIgnoreCase("true");
    }

    public static Object getInstanceOfProviderField(Field provider, Object instance) {
        try {
            provider.setAccessible(true);
            Provider<?> providerInstance = (Provider<?>) provider.get(instance);
            if (providerInstance == null)
                throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
            return providerInstance.get();
        } catch (NullPointerException e) {
            throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "'.", e);
        }
    }

}

package org.fulib.fx.util;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.controller.exception.InvalidRouteFieldException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class containing different helper methods for the framework or the user.
 */
public class Util {

    // Environment variable for telling the framework that it's running in development mode
    private static final String INDEV_ENVIRONMENT_VARIABLE = "INDEV";

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
        return System.getenv().getOrDefault(INDEV_ENVIRONMENT_VARIABLE, "false").equalsIgnoreCase("true");
    }

    /**
     * Returns an instance provided by the given provider field.
     *
     * @param provider The provider field
     * @param instance The instance to get the provider from
     * @return The instance provided by the provider field
     */
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

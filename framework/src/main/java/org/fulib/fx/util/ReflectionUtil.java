package org.fulib.fx.util;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.util.reflection.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.stream.Stream;

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * Utility class containing different helper methods for the framework or the user.
 * Parts of this class are hacky and should be used with caution.
 */
public class ReflectionUtil {

    private ReflectionUtil() {
        // Prevent instantiation
    }

    // Reflection for mouse handler
    private static Field mouseHandlerField;
    private static Constructor<?> mouseHandlerCtor;

    static {
        try {
            mouseHandlerField = Scene.class.getDeclaredField("mouseHandler");
            mouseHandlerCtor = Class.forName(Scene.class.getName() + "$MouseHandler").getDeclaredConstructor(Scene.class);
            mouseHandlerField.setAccessible(true);
            mouseHandlerCtor.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            FulibFxApp.LOGGER.severe("Could not initialize mouse handler reflection. This may cause problems with mouse drag events.");
        }
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
            if (clazz.getSuperclass() == Object.class) {
                throw new RuntimeException(error(9003).formatted(parent.getClass().getName()), e);
            }
            return getChildrenList(clazz.getSuperclass(), parent);
        }
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
            if (providerInstance == null) {
                throw new RuntimeException(error(9002).formatted(provider.getName(), provider.getDeclaringClass().getName()));
            }
            return providerInstance.get();
        } catch (NullPointerException e) {
            throw new RuntimeException(error(9002).formatted(provider.getName(), provider.getDeclaringClass().getName()), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(error(9001).formatted(provider.getName(), instance.getClass().getName()), e);
        }
    }

    /**
     * Resets the mouse handler of the given stage. This method is used to reset the mouse handler of a scene to avoid
     * problems with mouse drag events. This should be used with caution and is mainly used for refreshing the scene.
     *
     * @param stage The stage to reset the mouse handler for
     */
    public static void resetMouseHandler(Stage stage) {
        // NB: This hack avoids problems with mouse drag events.
        // In particular, the scene's MouseHandler would keep a list of its previous nodes,
        // which do not have a reference back to the scene and subsequently cause an NPE.
        try {
            final Scene scene = stage.getScene();
            final Object mouseHandler = mouseHandlerCtor.newInstance(scene);
            mouseHandlerField.set(scene, mouseHandler);
        } catch (ReflectiveOperationException e) {
            FulibFxApp.LOGGER.warning("Could not reset mouse handler. This may cause problems with mouse drag events after refreshing the scene.");
        }
    }

    /**
     * Returns all non-private fields of the given class that are annotated with the given annotation.
     * If a field is private, a RuntimeException is thrown.
     * <p>
     * Utility method for not having to specify an error message every time.
     *
     * @param clazz      The class to get the fields from
     * @param annotation The annotation to filter the fields by
     * @return A stream of fields that are annotated with the given annotation and are not private
     */
    public static Stream<Field> getAllNonPrivateFieldsOrThrow(@NotNull Class<?> clazz, @NotNull Class<? extends @NotNull Annotation> annotation) {
        return Reflection.getAllFieldsWithAnnotation(clazz, annotation).peek(field -> {
            if (Modifier.isPrivate(field.getModifiers())) {
                throw new RuntimeException(error(1012).formatted(Field.class.getSimpleName(), field.getName(), field.getDeclaringClass().getName(), annotation.getSimpleName()));
            }
        });
    }

    /**
     * Returns all non-private methods of the given class that are annotated with the given annotation.
     * If a method is private, a RuntimeException is thrown.
     * <p>
     * Utility method for not having to specify an error message every time.
     *
     * @param clazz      The class to get the methods from
     * @param annotation The annotation to filter the methods by
     * @return A stream of methods that are annotated with the given annotation and are not private
     */
    public static Stream<Method> getAllNonPrivateMethodsOrThrow(@NotNull Class<?> clazz, @NotNull Class<? extends @NotNull Annotation> annotation) {
        return Reflection.getAllMethodsWithAnnotation(clazz, annotation).peek(method -> {
            if (Modifier.isPrivate(method.getModifiers())) {
                throw new RuntimeException(error(1012).formatted(Method.class.getSimpleName(), method.getName(), method.getDeclaringClass().getName(), annotation.getSimpleName()));
            }
        });
    }

    /**
     * Checks if a method is overriding a method in one of the classes superclasses and returns the overridden method.
     *
     * @param method The method to check
     * @return The overridden method, or null if there is none
     */
    public static @Nullable Method getOverriding(@NotNull Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        Class<?> superclass = declaringClass.getSuperclass();

        while (superclass != null) {
            try {
                return superclass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            } catch (NoSuchMethodException e) {
                superclass = superclass.getSuperclass();
            }
        }
        return null;
    }


}

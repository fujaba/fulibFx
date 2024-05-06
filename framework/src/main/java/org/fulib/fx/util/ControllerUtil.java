package org.fulib.fx.util;

import javafx.scene.Node;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.OnDestroy;
import org.fulib.fx.annotation.event.OnInit;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.controller.exception.InvalidRouteFieldException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static org.fulib.fx.util.FrameworkUtil.error;
import static org.fulib.fx.util.ReflectionUtil.getProvidedClass;

public class ControllerUtil {

    private ControllerUtil() {
        // Prevent instantiation
    }

    private static final Set<Class<? extends Annotation>> EVENT_ANNOTATIONS = Set.of(
            OnInit.class,
            OnRender.class,
            OnDestroy.class,
            OnKey.class
    );

    /**
     * Checks if an instance is a component (controller extending a Node).
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param instance The instance to check
     * @return True if the instance is a component (controller extending a Node)
     */
    public static boolean isComponent(@Nullable Object instance) {
        return instance != null && isComponent(instance.getClass());
    }

    /**
     * Checks if a class is a component (controller extending a Node).
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param clazz The clazz to check
     * @return True if the clazz is a component (controller extending a Node)
     */
    public static boolean isComponent(@Nullable Class<?> clazz) {
        return clazz != null && clazz.isAnnotationPresent(Component.class) && !clazz.isAnnotationPresent(Controller.class) && Node.class.isAssignableFrom(clazz);
    }

    /**
     * Checks if an instance is a controller or a component.
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param instance The instance to check
     * @return True if the instance is a controller or component
     */
    public static boolean isControllerOrComponent(@Nullable Object instance) {
        return isController(instance) || isComponent(instance);
    }

    /**
     * Checks if an instance is a controller.
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param instance The instance to check
     * @return True if the instance is a controller
     */
    public static boolean isController(@Nullable Object instance) {
        return instance != null && isController(instance.getClass());
    }

    /**
     * Checks if a class is a controller.
     * <p>
     * This method is used internally by the framework and shouldn't be required for developers.
     *
     * @param clazz The class to check
     * @return True if the class is a controller
     */
    public static boolean isController(@Nullable Class<?> clazz) {
        return clazz != null && clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(Component.class);
    }

    /**
     * Checks if the given field is a field that can provide a component.
     *
     * @param field The field to check
     * @return True if the field is a field that can provide a component
     */
    public static boolean canProvideSubComponent(Field field) {
        return isComponent(field.getType()) || isComponent(getProvidedClass(field));
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
        return classes[classes.length - 1].replace("Controller", "").replace("Component", "");
    }

    /**
     * Checks if the given field is a valid route field.
     * A valid route field is a field that is annotated with {@link Route} and is of type {@link Provider} where the generic type is a class annotated with {@link Controller} or {@link Component}.
     *
     * @param field The field to check
     * @throws InvalidRouteFieldException If the field is not a valid route field
     */
    public static void requireControllerProvider(@NotNull Field field) {
        if (isControllerOrComponent(getProvidedClass(field))) {
            throw new InvalidRouteFieldException(field);
        }
    }

    /**
     * Checks whether a method is annotated with an event annotation such as {@link OnKey} or {@link OnRender}.
     *
     * @param method The method to check
     * @return Whether the method is annotated with an event annotation
     */
    public static boolean isEventMethod(@NotNull Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (EVENT_ANNOTATIONS.contains(annotation.annotationType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility method for checking if a method overrides another event method.
     * <p>
     * If a method overrides another method and the overridden method is an event method, calling the superclass method
     * results in the subclass method being called twice due to how java handles method overrides.
     *
     * @param method     The method to check
     * @param annotation Whether the method overrides another event method
     */
    public static void checkOverrides(Method method, Class<? extends Annotation> annotation) {
        Method overridden = ReflectionUtil.getOverriding(method);
        if (overridden != null && ControllerUtil.isEventMethod(overridden)) {
            throw new RuntimeException(error(1013).formatted(method.getName(), annotation.getSimpleName(), method.getDeclaringClass().getName(), overridden.getDeclaringClass().getName()));
        }
    }
}

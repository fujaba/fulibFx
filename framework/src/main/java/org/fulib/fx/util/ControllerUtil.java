package org.fulib.fx.util;

import javafx.scene.Parent;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

import static org.fulib.fx.util.Util.getProvidedClass;

public class ControllerUtil {

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
}

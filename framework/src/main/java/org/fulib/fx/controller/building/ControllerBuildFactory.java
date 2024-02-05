package org.fulib.fx.controller.building;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.util.Util;
import org.fulib.fx.util.reflection.Reflection;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.util.*;

/**
 * A custom building-factory for instantiating controllers. If an element in an FXML file is of a class annotated with @Controller and a field providing an instance of the same class exists, the provided instance will be used as the controller for the element.
 */
public class ControllerBuildFactory implements BuilderFactory {

    private final Object instance;

    // Cache for subcontroller instances, mapped by class -> instance/provider
    private final Map<Class<?>, List<Object>> subControllerInstances;
    private final Map<Class<?>, Provider<?>> subControllerProviders;

    public ControllerBuildFactory(@NotNull Object instance) {
        this.instance = instance;
        this.subControllerInstances = new HashMap<>();
        this.subControllerProviders = new HashMap<>();

        initSubControllers();
    }

    /**
     * Searches the controller class for fields annotated with @SubController and stores the instances.
     */
    private void initSubControllers() {
        Reflection.getFieldsWithAnnotation(instance.getClass(), SubComponent.class).forEach(field -> {

            // If the field is a provider, store it in the provider map
            if (field.getType() == Provider.class) {
                field.setAccessible(true);
                try {
                    Class<?> type = Util.getProvidedClass(field);
                    if (type == null) {
                        throw new RuntimeException("Couldn't determine the type of the provider '%s' in '%s'.".formatted(field.getName(), field.getClass().getName()));
                    }
                    if (subControllerProviders.containsKey(type)) {
                        throw new RuntimeException("Multiple sub-controller annotations with the same type '" + type.getName() + "' found in class '" + instance.getClass().getName() + "'.");
                    }
                    subControllerProviders.put(type, (Provider<?>) field.get(instance));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Couldn't access the provider '%s' in '%s'.".formatted(field.getName(), field.getClass().getName()), e);
                }
                return;
            }

            // If the field is not a provider, store the instance in the instance map

            if (!subControllerInstances.containsKey(field.getType())) {
                subControllerInstances.put(field.getType(), new ArrayList<>());
            }

            try {
                field.setAccessible(true);
                subControllerInstances.get(field.getType()).add(field.get(instance));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't access field '%s' annotated as a sub-controller in '%s'.".formatted(field.getName(), field.getClass().getName()), e);
            }
        });
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type.isAnnotationPresent(Component.class)) {
            return new ControllerProxyBuilder<>(this, type);
        } else {
            return null; // Let javafx handle the instantiation
        }
    }

    /**
     * Searches the controller class for a field annotated with @SubController that provides an instance of the given type.
     * If no matching field is found, the framework will look for a provider field annotated with @SubController.
     * If no matching provider is found, the framework will throw an exception.
     *
     * @param type The type of the subcontroller
     * @return The instance of the subcontroller
     */
    public Object getProvidedInstance(Class<?> type) {
        if (subControllerInstances.containsKey(type)) {
            if (!subControllerInstances.get(type).isEmpty()) {
                // If there are multiple instances of the same type, use the first one and remove it from the list
                Object instance = subControllerInstances.get(type).get(0);
                subControllerInstances.get(type).remove(0);
                return instance;
            } else
                throw new RuntimeException("No usable instance of the sub-controller with type '" + type.getName() + "' found.");
        } else if (subControllerProviders.containsKey(type)) {
            return subControllerProviders.get(type).get();
        } else {
            throw new RuntimeException("No instance of the sub-controller with type '" + type.getName() + "' found.");
        }
    }
}
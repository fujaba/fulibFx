package io.github.sekassel.jfxframework.controller.building;

import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.controller.SubComponent;
import io.github.sekassel.jfxframework.util.Util;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * A custom building-factory for instantiating controllers. If an element in an FXML file is of a class annotated with @Controller and a field providing an instance of the same class exists, the provided instance will be used as the controller for the element.
 */
public class ControllerBuildFactory implements BuilderFactory {

    private final Object instance;

    // Cache for subcontroller instances, mapped by class -> instance/provider
    private final Map<Class<?>, Object> subControllerInstances;
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

            if (subControllerInstances.containsKey(field.getType())) {
                throw new RuntimeException("Multiple sub-controller annotations with the same type '" + field.getType().getName() + "' found in class '" + instance.getClass().getName() + "'.");
            }

            try {
                field.setAccessible(true);
                subControllerInstances.put(field.getType(), field.get(instance));
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
     * The type of the instance doesn't need to be exactly the same as the given type, as long as the classes are compatible.
     *
     * @param type The type of the subcontroller
     * @return The instance of the subcontroller
     */
    public Object getProvidedInstance(Class<?> type) {
        if (subControllerInstances.containsKey(type)) {
            return subControllerInstances.get(type);
        } else if (subControllerProviders.containsKey(type)) {
            return subControllerProviders.get(type).get();
        } else {
            throw new RuntimeException("No instance of the subcontroller with type '" + type.getName() + "' found.");
        }
    }
}
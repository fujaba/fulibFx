package io.github.sekassel.jfxframework.controller.building;

import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.SubController;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A custom building-factory for instantiating controllers. If an element in an FXML file is of a class annotated with @Controller and a field providing an instance of the same class exists, the provided instance will be used as the controller for the element.
 */
public class ControllerBuildFactory implements BuilderFactory {

    private final Object instance;

    // Cache for subcontroller instances, mapped by class -> id -> instance
    private final Map<String, Object> subControllerInstances;

    public ControllerBuildFactory(@NotNull Object instance) {
        this.instance = instance;
        this.subControllerInstances = new HashMap<>();

        initSubControllers();
    }

    /**
     * Searches the controller class for fields annotated with @SubController and stores the instances.
     */
    private void initSubControllers() {
        Reflection.getFieldsWithAnnotation(instance.getClass(), SubController.class).forEach(field -> {

            SubController annotation = field.getAnnotation(SubController.class);
            String id = annotation.value();
            if (id.isEmpty()) {
                id = "";
            }

            if (subControllerInstances.containsKey(id)) {
                throw new RuntimeException("Multiple subcontrollers with the same id '" + id + "' found in class '" + instance.getClass().getName() + "'.");
            }

            try {
                field.setAccessible(true);
                subControllerInstances.put(id, field.get(instance));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't access the instance of the subcontroller field with id '" + id + "'.", e);
            }
        });
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type.isAnnotationPresent(Controller.class)) {
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
     * @param id   The id of the subcontroller
     * @return The instance of the subcontroller
     */
    public Object getProvidedInstance(Class<?> type, String id) {
        if (subControllerInstances.containsKey(id)) {
            Object instance = subControllerInstances.get(id);

            // The instance doesn't need to be of the exact type, as long as the classes are compatible
            if (type.isAssignableFrom(instance.getClass())) {
                return instance;
            } else {
                throw new RuntimeException("The provided instance of the subcontroller with id '" + id + "' is not of the correct type.");
            }
        } else {
            throw new RuntimeException("No instance of the subcontroller with id '" + id + "' found.");
        }
    }
}
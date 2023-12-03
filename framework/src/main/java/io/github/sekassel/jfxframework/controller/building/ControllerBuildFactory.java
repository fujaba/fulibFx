package io.github.sekassel.jfxframework.controller.building;

import io.github.sekassel.jfxframework.controller.ControllerManager;
import io.github.sekassel.jfxframework.controller.Router;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A custom building-factory for instantiating controllers. If an element in an FXML file is of a class annotated with @Controller and a field providing an instance of the same class exists, the provided instance will be used as the controller for the element.
 */
public class ControllerBuildFactory implements BuilderFactory {

    private final ControllerManager controllerManager;
    private final Router router;

    private final Map<String, Object> parameters;

    public ControllerBuildFactory(@NotNull ControllerManager controllerManager, @NotNull Router router, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        this.controllerManager = controllerManager;
        this.router = router;
        this.parameters = parameters;
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type.isAnnotationPresent(Controller.class)) {
            return new ControllerProxyBuilder<>(this, type, parameters);
        } else {
            return null; // Let javafx handle the instantiation
        }
    }

    public Object getProvidedInstance(Class<?> type) {
        return router.getProvidedInstance(type);
    }

    public ControllerManager controllerManager() {
        return controllerManager;
    }
}

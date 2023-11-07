package io.github.sekassel.jfxframework.controller.building;

import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.Router;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A custom building-factory for instantiating controllers. If an element in an FXML file is of a class annotated with @Controller and a field providing an instance of the same class exists, the provided instance will be used as the controller for the element.
 */
public class ControllerBuildFactory implements BuilderFactory {

    private final Router router;
    private final URL fxmlPath;
    private final Map<String, Object> parameters;

    private final Set<Object> instances;

    public ControllerBuildFactory(@NotNull Router router, @NotNull URL fxmlPath, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        this.router = router;
        this.fxmlPath = fxmlPath;
        this.parameters = parameters;
        this.instances = new HashSet<>();
    }

    @Override
    public Builder<?> getBuilder(Class<?> type) {
        if (type.isAnnotationPresent(Controller.class)) {
            return new ControllerProxyBuilder<>(this, type);
        } else {
            return null;
        }
    }

    public Object getProvidedInstance(Class<?> type) {
        Object instance = router.getProvidedInstance(type);

        // Run the controller's onInit methods. onRender methods will be run by the FxFramework.
        try {
            Reflection.callMethodsWithAnnotation(instance, ControllerEvent.onInit.class, parameters);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize subcontroller '" + type.getSimpleName() + "' in '" + fxmlPath.getPath() + "'.", e);
        }

        instances.add(instance);

        return instance;
    }

    public Set<Object> getInstantiatedControllers() {
        return instances;
    }
}

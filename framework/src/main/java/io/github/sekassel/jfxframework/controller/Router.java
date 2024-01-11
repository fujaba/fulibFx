package io.github.sekassel.jfxframework.controller;

import dagger.Lazy;
import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.Providing;
import io.github.sekassel.jfxframework.annotation.Route;
import io.github.sekassel.jfxframework.controller.exception.ControllerDuplicatedRouteException;
import io.github.sekassel.jfxframework.controller.exception.ControllerInvalidRouteException;
import io.github.sekassel.jfxframework.data.*;
import io.github.sekassel.jfxframework.util.Util;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Router {

    private final Map<Class<?>, Field> providingFields;
    private final TraversableTree<Field> routes;
    private final TraversableQueue<Tuple<TraversableNodeTree.Node<Field>, Map<String, Object>>> history;

    @Inject
    Lazy<ControllerManager> manager;

    private Object routerObject;

    @Inject
    public Router() {
        this.providingFields = new ConcurrentHashMap<>();
        this.routes = new TraversableNodeTree<>();
        this.history = new EvictingQueue<>(10);
    }

    /**
     * Registers all routes in the given class.
     *
     * @param routes The class to register the routes from
     */
    public void registerRoutes(@NotNull Object routes) {
        if (this.routerObject != null)
            throw new IllegalStateException("%s has already been registered as the router class.".formatted(this.routerObject.getClass().getName()));

        this.routerObject = routes;

        Reflection.getFieldsWithAnnotation(routes.getClass(), Route.class).forEach(this::registerRoute);
        Reflection.getFieldsWithAnnotation(routes.getClass(), Providing.class).forEach(this::registerProviding);
    }

    /**
     * Registers a field as a provider for loading subcontrollers in FXML files.
     *
     * @param field The field to register
     */
    private void registerProviding(Field field) {
        if (!field.isAnnotationPresent(Providing.class))
            throw new RuntimeException("Field '" + field.getName() + "' in class '" + field.getDeclaringClass().getName() + "' is not annotated with @Providing");
        if (this.providingFields.containsKey(field.getType())) {
            FxFramework.logger().warning("Field '" + field.getName() + "' in '" + field.getDeclaringClass().getName() + "' is annotated with @Providing but there is already a field providing an instance of '" + field.getType().getName() + "'. The old field will be used instead.");
            return;
        }

        Util.requireControllerProvider(field);

        this.providingFields.put(Util.getProvidedClass(field), field);
    }

    /**
     * Registers a field as a route.
     * <p>
     * The field has to be marked with {@link Route}.
     *
     * @param field The controller to register
     */
    private void registerRoute(@NotNull Field field) {
        if (!field.isAnnotationPresent(Route.class))
            throw new RuntimeException("Field " + field.getName() + " is not annotated with @Route");

        // Check if the field is of type Provider<T> where T is annotated with @Controller
        Util.requireControllerProvider(field);

        Route annotation = field.getAnnotation(Route.class);
        String route = annotation.value().equals("$name") ? "/" + field.getName() : annotation.value();

        // Make sure the route starts with a slash to prevent issues with the traversal
        route = route.startsWith("/") ? route : "/" + route;

        if (this.routes.containsPath(route))
            throw new ControllerDuplicatedRouteException(route, field.getType(), this.routes.get(route).getType());

        this.routes.insert(route, field);
    }

    /**
     * Initializes and renders the controller with the given route.
     *
     * @param route      The route of the controller
     * @param parameters The parameters to pass to the controller
     * @throws ControllerInvalidRouteException If the controller couldn't be found
     */
    public @NotNull Tuple<Object, Parent> renderRoute(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        // Check if the route exists and has a valid controller
        if (!this.routes.containsPath(route)) throw new ControllerInvalidRouteException(route);

        // Get the provider and the controller class
        Field provider = this.routes.traverse(route);
        TraversableNodeTree.Node<Field> node = ((TraversableNodeTree<Field>) this.routes).currentNode();

        // Since we visited this route with the given parameters, we can add it to the history
        this.history.insert(Tuple.of(node, parameters));
        Class<?> controllerClass = Util.getProvidedClass(Objects.requireNonNull(provider));

        // Check if the provider is providing a valid controller/component
        if (controllerClass == null)
            throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not a valid provider field.");

        if (!controllerClass.isAnnotationPresent(Controller.class) && !controllerClass.isAnnotationPresent(Component.class))
            throw new RuntimeException("Class " + controllerClass.getName() + " is not annotated with @Controller or @Component");

        // Get the instance of the controller
        Object controllerInstance = getInstanceOfProviderField(provider);
        Parent renderedParent = this.manager.get().initAndRender(controllerInstance, parameters);

        return Tuple.of(controllerInstance, renderedParent);
    }

    private Object getInstanceOfProviderField(Field provider) {
        try {
            provider.setAccessible(true);
            Provider<?> providerInstance = (Provider<?>) provider.get(this.routerObject);
            if (providerInstance == null)
                throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
            return providerInstance.get();
        } catch (NullPointerException e) {
            throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "'.", e);
        }
    }

    /**
     * Returns the instance of the given type provided by a field annotated with {@link Providing} in the router class.
     *
     * @param type The type of the instance
     * @return The instance
     * @throws RuntimeException If no field providing an instance of the given type has been registered
     */
    public @NotNull <T> T getProvidedInstance(@NotNull Class<T> type) {
        if (!this.providingFields.containsKey(type))
            throw new RuntimeException("No field providing an instance of '" + type.getName() + "' has been registered using @Providing.");

        Field field = this.providingFields.get(type);
        try {
            return ((Provider<T>) field.get(this.routerObject)).get();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Field '" + field.getName() + "' in '" + field.getDeclaringClass().getName() + "' could not be accessed.", e);
        }
    }

    /**
     * Goes back to the previous controller in the history.
     *
     * @return The rendered controller
     */
    public Parent back() {
        try {
            Tuple<TraversableNodeTree.Node<Field>, Map<String, Object>> tuple = this.history.back();
            ((TraversableNodeTree<Field>) routes).setCurrentNode(tuple.first());
            return this.manager.get().initAndRender(getInstanceOfProviderField(tuple.first().value()), tuple.second());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Goes forward to the next controller in the history.
     *
     * @return The rendered controller
     */
    public Parent forward() {
        try {
            Tuple<TraversableNodeTree.Node<Field>, Map<String, Object>> tuple = this.history.forward();
            ((TraversableNodeTree<Field>) routes).setCurrentNode(tuple.first());
            return this.manager.get().initAndRender(getInstanceOfProviderField(tuple.first().value()), tuple.second());
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * Returns the current field and its parameters.
     * This is used internally for reloading the current controller.
     *
     * @return The current field and its parameters
     */
    public Tuple<Field, Map<String, Object>> current() {
        return Tuple.of(this.history.current().first().value(), this.history.current().second());
    }
}
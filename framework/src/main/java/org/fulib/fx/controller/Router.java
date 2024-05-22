package org.fulib.fx.controller;

import dagger.Lazy;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Pair;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.Route;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.controller.exception.ControllerDuplicatedRouteException;
import org.fulib.fx.controller.exception.ControllerInvalidRouteException;
import org.fulib.fx.data.*;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.FrameworkUtil;
import org.fulib.fx.util.ReflectionUtil;
import org.fulib.fx.util.reflection.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

import static org.fulib.fx.util.FrameworkUtil.error;
import static org.fulib.fx.util.FrameworkUtil.note;

@Singleton
public class Router {

    private final TraversableTree<Field> routes;
    private final SizeableTraversableQueue<Pair<Either<TraversableNodeTree.Node<Field>, Object>, Map<String, Object>>> history;

    @Inject
    Lazy<ControllerManager> manager;

    private Object routerObject;

    @Inject
    public Router() {
        this.routes = new TraversableNodeTree<>();
        this.history = new EvictingQueue<>(5);
    }

    /**
     * Registers all routes in the given class.
     *
     * @param routes The class to register the routes from
     */
    public void registerRoutes(@NotNull Object routes) {
        if (this.routerObject != null) {
            throw new IllegalStateException(error(3000).formatted(this.routerObject.getClass().getName()));
        }

        this.routerObject = routes;

        Reflection.getFieldsWithAnnotation(routes.getClass(), Route.class).forEach(this::registerRoute);
    }


    /**
     * Registers a field as a route.
     * <p>
     * The field has to be marked with {@link Route}.
     *
     * @param field The controller to register
     */
    private void registerRoute(@NotNull Field field) {
        if (!field.isAnnotationPresent(Route.class)) {
            throw new RuntimeException(error(3001).formatted(field.getName()));
        }

        // Check if the field is of type Provider<T> where T is annotated with @Controller
        ControllerUtil.requireControllerProvider(field);

        Route annotation = field.getAnnotation(Route.class);
        String route = annotation.value().equals("$name") ? "/" + field.getName() : annotation.value();

        // Make sure the route starts with a slash to prevent issues with the traversal
        route = route.startsWith("/") ? route : "/" + route;

        if (this.routes.containsPath(route)) {
            throw new ControllerDuplicatedRouteException(route, field.getType(), this.routes.get(route).getType());
        }
        this.routes.insert(route, field);
    }

    /**
     * Initializes and renders the controller/component with the given route.
     * This only works for controllers and components having a parent as their root node.
     *
     * @param route      The route of the controller
     * @param parameters The parameters to pass to the controller
     * @return A pair containing the controller instance and the rendered parent (will be the same if the controller is a component)
     * @throws ControllerInvalidRouteException If the route couldn't be found
     */
    public @NotNull Pair<Object, Parent> renderRoute(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        // Check if the route exists and has a valid controller
        if (!this.routes.containsPath(route)) {
            String message = error(3005).formatted(route);
            if (this.routes.containsPath("/" + route)) {
                message += " " + note(3005).formatted("/" + route);
            }
            throw new ControllerInvalidRouteException(message);
        }

        // Get the provider and the controller class
        Field provider = this.routes.traverse(route);
        TraversableNodeTree.Node<Field> node = ((TraversableNodeTree<Field>) this.routes).currentNode();

        // Since we visited this route with the given parameters, we can add it to the history
        this.addToHistory(new Pair<>(Either.left(node), parameters));
        Class<?> controllerClass = ReflectionUtil.getProvidedClass(Objects.requireNonNull(provider));

        // Check if the provider is providing a valid controller/component
        if (controllerClass == null) {
            throw new RuntimeException(error(3004).formatted(provider.getName(), routerObject.getClass().getName()));
        }
        if (!controllerClass.isAnnotationPresent(Controller.class) && !controllerClass.isAnnotationPresent(Component.class)) {
            throw new RuntimeException(error(1001).formatted(controllerClass.getName()));
        }
        // Get the instance of the controller
        Object controllerInstance = ReflectionUtil.getInstanceOfProviderField(provider, this.routerObject);
        Node renderedNode = this.manager.get().initAndRender(controllerInstance, parameters);

        if (renderedNode instanceof Parent parent) {
            return new Pair<>(controllerInstance, parent);
        } else {
            throw new RuntimeException(error(1011).formatted(controllerClass.getName()));
        }
    }

    /**
     * Returns the controller with the given route without initializing and rendering it.
     * The route will be seen as absolute, meaning it will be treated as a full path.
     *
     * @param route The route of the controller
     * @return The controller instance
     */
    public Object getController(String route) {
        Field provider = this.routes.get(route.startsWith("/") ? route : "/" + route);
        return ReflectionUtil.getInstanceOfProviderField(provider, this.routerObject);
    }

    public void addToHistory(Pair<Either<TraversableNodeTree.Node<Field>, Object>, Map<String, Object>> pair) {
        this.history.insert(pair);
    }

    /**
     * Goes back to the previous controller in the history.
     *
     * @return The rendered controller
     */
    public Pair<Object, Node> back() {
        try {
            var pair = this.history.back();
            return navigate(pair);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Goes forward to the next controller in the history.
     *
     * @return The rendered controller
     */
    public Pair<Object, Node> forward() {
        try {
            var pair = this.history.forward();
            return navigate(pair);
        } catch (Exception e) {
            return null;
        }
    }

    private Pair<Object, Node> navigate(Pair<Either<TraversableNodeTree.Node<Field>, Object>, Map<String, Object>> pair) {
        var either = pair.getKey();
        either.getLeft().ifPresent(node -> ((TraversableNodeTree<Field>) routes).setCurrentNode(node)); // If the history contains a route, set it as the current node

        Object controller = either.isLeft() ?
                ReflectionUtil.getInstanceOfProviderField(either.getLeft().orElseThrow().value(), this.routerObject) : // Get the controller instance from the provider
                either.getRight().orElseThrow(); // Get the controller instance from the history

        this.manager.get().cleanup(); // Cleanup the current controller

        // Returns the controller instance and the rendered node
        return new Pair<>(controller, this.manager.get().initAndRender(
                controller,
                pair.getValue() // The parameters
        ));
    }

    /**
     * Returns the current field and its parameters.
     * This is used internally for reloading the current controller.
     *
     * @return The current controller object and its parameters
     */
    public Pair<Object, Map<String, Object>> current() {
        Either<TraversableNodeTree.Node<Field>, Object> either = this.history.current().getKey();
        return new Pair<>(
                either.isLeft() ?
                        either.getLeft().map(node -> {
                            try {
                                Objects.requireNonNull(node.value()).setAccessible(true);
                                return ((Provider<?>) Objects.requireNonNull(node.value()).get(routerObject)).get();
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }).orElseThrow() :
                        either.getRight().orElseThrow(),
                this.history.current().getValue()
        );
    }

    /**
     * Updates the history size.
     * Setting the size to 1 will disable the history except for the current controller which will be stored for reloading.
     *
     * @param size The size of the history
     */
    public void setHistorySize(int size) {
        this.history.setSize(size);
    }
}

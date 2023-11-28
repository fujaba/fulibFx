package io.github.sekassel.jfxframework.controller;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Providing;
import io.github.sekassel.jfxframework.controller.annotation.Route;
import io.github.sekassel.jfxframework.controller.building.ControllerBuildFactory;
import io.github.sekassel.jfxframework.controller.exception.ControllerDuplicatedRouteException;
import io.github.sekassel.jfxframework.controller.exception.ControllerInvalidRouteException;
import io.github.sekassel.jfxframework.data.TraversableNodeTree;
import io.github.sekassel.jfxframework.data.TraversableTree;
import io.github.sekassel.jfxframework.util.Util;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Provider;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sekassel.jfxframework.util.Constants.FXML_PATH;

public class Router {

    private final Map<Class<?>, Field> providingFields;

    private final TraversableTree<Field> routes;

    private Object source;
    private Class<? extends FxFramework> baseClass = FxFramework.class;

    public Router() {
        this.providingFields = new ConcurrentHashMap<>();
        this.routes = new TraversableNodeTree<>();
    }

    /**
     * Registers all routes in the given class.
     *
     * @param routes The class to register the routes from
     */
    public void registerRoutes(@NotNull Object routes) {
        if (this.source != null)
            throw new IllegalStateException("%s has already been registered as the router class.".formatted(this.source.getClass().getName()));

        this.source = routes;

        Reflection.getFieldsWithAnnotation(routes.getClass(), Route.class).forEach(this::registerRoute);
        Reflection.getFieldsWithAnnotation(routes.getClass(), Providing.class).forEach(this::registerProviding);
    }

    /**
     * Registers a field as a provider for loading subcontrollers in FXML files.
     * @param field The field to register
     */
    private void registerProviding(Field field) {
        if (!field.isAnnotationPresent(Providing.class))
            throw new RuntimeException("Field '" + field.getName() + "' in class '"+ field.getDeclaringClass().getName() + "' is not annotated with @Providing");
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
        String route = annotation.route().equals("$name") ? field.getName() : annotation.route();

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
    public @NotNull Parent renderRoute(@NotNull String route, @NotNull Map<String, Object> parameters) {
        // Check if the route exists and has a valid controller
        if (!this.routes.containsPath(route)) throw new ControllerInvalidRouteException(route);

        Field provider = this.routes.traverse(route);
        Class<?> controller = Util.getProvidedClass(provider);

        if (controller == null)
            throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not a valid provider field.");

        if (!controller.isAnnotationPresent(Controller.class))
            throw new RuntimeException("Class " + controller.getName() + " is not annotated with @Controller");

        // Get the instance of the controller
        Object instance;
        try {
            instance = ((Provider<?>) provider.get(source)).get();
        } catch (NullPointerException e) {
            throw new RuntimeException("Field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "' is not initialized.");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field '" + provider.getName() + "' in '" + provider.getDeclaringClass().getName() + "'.", e);
        }

        return initAndRender(instance, parameters);
    }

    public Parent initAndRender(Object instance, Map<String, Object> parameters) {

        // Call the onInit method
        Reflection.callMethodsWithAnnotation(instance, ControllerEvent.onInit.class, parameters);

        // Render the controller
        Parent parent;
        String view = instance.getClass().getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Parent, render it
        if (Parent.class.isAssignableFrom(instance.getClass()) && view.isEmpty()) {
            parent = (Parent) instance;
        }

        // If the controller specifies a method as view, call it
        else if (view.startsWith("#")) {
            String methodName = view.substring(1);
            try {
                Method method = instance.getClass().getDeclaredMethod(methodName);
                if (!Parent.class.isAssignableFrom(method.getReturnType()))
                    throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' does not return a Parent.");
                parent = (Parent) method.invoke(instance);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' does not exist.");
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' could not be called.", e);
            }
        }
        // If the controller specifies a fxml file, load it
        else {
            String fxmlPath = view.isEmpty() ? FXML_PATH + Util.transform(instance.getClass().getSimpleName()) + ".fxml" : view;
            parent = load(fxmlPath, instance, parameters, Parent.class.isAssignableFrom(instance.getClass()));
        }

        // Call the onRender method
        Reflection.callMethodsWithAnnotation(instance, ControllerEvent.onRender.class, parameters);

        return parent;
    }

    /**
     * Loads a fxml file.
     *
     * @param fileName The name of the fxml resource file (with path and file extension)
     * @param factory  The controller factory to use
     * @return A parent representing the fxml file
     */
    protected @NotNull Parent load(@NotNull String fileName, @NotNull Object factory, @NotNull Map<@NotNull String, @Nullable Object> parameters, boolean setRoot) {
        URL url = baseClass.getResource(fileName);
        if (url == null) throw new RuntimeException("Could not find resource '" + fileName + "'");

        ControllerBuildFactory builderFactory = new ControllerBuildFactory(this, parameters);

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(c -> factory);
        loader.setBuilderFactory(builderFactory);

        if (setRoot) {
            loader.setRoot(factory);
        }

        try {
            return loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Sets the base class of the application (the class that extends {@link FxFramework}).
     * This is used to load the fxml files with relative paths.
     *
     * @param clazz The base class
     */
    public void setMainClass(Class<? extends FxFramework> clazz) {
        this.baseClass = clazz;
    }

    public Object getProvidedInstance(Class<?> type) {
        if (!this.providingFields.containsKey(type))
            throw new RuntimeException("No field providing an instance of '" + type.getName() + "' has been registered using @Providing.");

        Field field = this.providingFields.get(type);
        try {
            return ((Provider<?>) field.get(this.source)).get();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Field '" + field.getName() + "' in '" + field.getDeclaringClass().getName() + "' could not be accessed.", e);
        }
    }
}
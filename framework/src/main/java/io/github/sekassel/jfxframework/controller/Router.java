package io.github.sekassel.jfxframework.controller;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.jfxframework.controller.annotation.Params;
import io.github.sekassel.jfxframework.controller.annotation.Route;
import io.github.sekassel.jfxframework.controller.exception.ControllerDuplicatedRouteException;
import io.github.sekassel.jfxframework.controller.exception.ControllerInvalidRouteException;
import io.github.sekassel.jfxframework.controller.exception.ControllerLoadingException;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import static io.github.sekassel.jfxframework.util.Constants.FXML_PATH;

public class Router {

    private final TraversableTree<Field> routes;

    private Object source;
    private Class<? extends FxFramework> baseClass = FxFramework.class;

    public Router() {
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
    public @NotNull Parent render(@NotNull String route, @NotNull Map<String, Object> parameters) {
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
            throw new ControllerLoadingException(route, e);
        }

        // Call the onInit method
        callMethodsWithAnnotation(route, instance, ControllerEvent.onInit.class, parameters);

        // Render the controller
        Parent parent;
        String view = controller.getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Parent, render it
        if (Parent.class.isAssignableFrom(instance.getClass())) {
            parent = (Parent) instance;
            if (!view.isEmpty()) throw new RuntimeException("Controller '" + controller.getName() + "' is a javafx parent and cannot have a view specified.");
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
            parent = view.isEmpty() ? load(FXML_PATH + Util.transform(controller.getSimpleName()) + ".fxml", instance) : load(view, instance);
        }

        // Call the onRender method
        callMethodsWithAnnotation(route, instance, ControllerEvent.onRender.class, parameters);

        return parent;
    }

    /**
     * Calls all methods annotated with a certain annotation in the provided controller.
     *
     * @param route      The route of the controller
     * @param annotation The annotation to look for
     * @param parameters The parameters to pass to the methods
     */
    private void callMethodsWithAnnotation(@NotNull String route, @NotNull Object instance, @NotNull Class<? extends Annotation> annotation, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), annotation).forEach(method -> {
            try {
                method.invoke(instance, applicableParameters(method, parameters));
            } catch (Exception e) {
                throw new ControllerLoadingException(route, e);
            }
        });
    }

    /**
     * Returns an array with all parameters that are applicable to the given method in the correct order.
     *
     * @param method     The method to check
     * @param parameters The values of the parameters
     * @return An array with all applicable parameters
     */
    private @Nullable Object @NotNull [] applicableParameters(@NotNull Method method, @NotNull Map<String, Object> parameters) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> {

                    // Check if the parameter is annotated with @Param and if the parameter is of the correct type
                    Param param = parameter.getAnnotation(Param.class);
                    if (param != null) {
                        if (parameters.containsKey(param.name()) && !parameter.getType().isAssignableFrom(parameters.get(param.name()).getClass())) {
                            throw new RuntimeException("Parameter named '" + param.name() + "' in method '" + method.getDeclaringClass().getName() + "#" + method.getName() + "' is of type " + parameter.getType().getName() + " but the provided value is of type " + parameters.get(param.name()).getClass().getName());
                        }
                        return parameters.get(param.name());
                    }

                    // Check if the parameter is annotated with @Params and if the parameter is of the type Map<String, Object>
                    Params params = parameter.getAnnotation(Params.class);
                    if (params != null) {
                        if (!Util.isMapWithTypes(parameter, String.class, Object.class)) {
                            throw new RuntimeException("Parameter annotated with @Params in method '" + method.getClass().getName() + "#" + method.getName() + "' is not of type " + Map.class.getName());
                        }
                        return parameters;
                    }

                    return null;
                })
                .toArray();
    }

    /**
     * Loads a fxml file.
     *
     * @param fileName The name of the fxml resource file (with path and file extension)
     * @param factory  The controller factory to use
     * @return A parent representing the fxml file
     */
    protected @NotNull Parent load(@NotNull String fileName, @NotNull Object factory) {
        URL url = baseClass.getResource(fileName);
        if (url == null) throw new RuntimeException("Could not find resource '" + fileName + "'");

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(c -> factory);
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

}
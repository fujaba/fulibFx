package io.github.sekassel.jfxframework.controller;

import io.github.sekassel.jfxframework.controller.exception.ControllerInvalidRouteException;
import io.github.sekassel.jfxframework.controller.exception.ControllerLoadingException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ControllerManager {

    private static final String FXML_PATH = "view/";

    private final Class<?> baseClass;

    /**
     * A map of all routes to their controller classes.
     */
    private final Map<String, Class<?>> classes;


    /**
     * A map of all routes to their controller instance.
     * <p>
     * The key is the route of the controller.
     */
    // TODO: fix singletons
    private final Map<String, Object> instances;

    public ControllerManager(Class<?> baseClass) {
        this.baseClass = baseClass;
        this.classes = new LinkedHashMap<>();
        this.instances = new LinkedHashMap<>();
    }

    /**
     * Registers all controllers in a given package.
     *
     * @param packages The packages to scan
     */
    public void register(@NotNull String @NotNull ... packages) {
        Arrays.asList(packages).forEach(pack -> {
            Reflections reflections = new Reflections(pack);
            reflections.getTypesAnnotatedWith(Controller.class).forEach(this::register);
        });
    }

    /**
     * Registers a controller.
     * <p>
     * The controller must be annotated with {@link Controller}.
     *
     * @param controller The controller to register
     */
    private void register(@NotNull Class<?> controller) {
        this.classes.put(controller.getAnnotation(Controller.class).route(), controller);
    }

    /**
     * Initializes the controller with the given route.
     *
     * @param route      The route of the controller
     * @param parameters The parameters to pass to the controller
     * @throws ControllerInvalidRouteException If the controller couldn't be found
     */
    public void init(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        if (!this.classes.containsKey(route)) throw new ControllerInvalidRouteException(route);

        Class<?> controller = this.classes.get(route);

        try {
            instances.put(route, controller.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new ControllerLoadingException(route, e);
        }

        callAnnotation(route, controller, ControllerEvent.onInit.class, parameters);
    }

    /**
     * Renders the controller with the given route.
     *
     * @param route      The route of the controller
     * @param parameters The parameters to pass to the controller
     * @throws ControllerInvalidRouteException If the controller couldn't be found
     */
    public @NotNull Parent render(@NotNull String route, @NotNull Map<String, Object> parameters) {
        if (!this.instances.containsKey(route)) throw new ControllerInvalidRouteException(route);
        Class<?> controller = this.classes.get(route);

        String path = controller.getAnnotation(Controller.class).path();

        Parent parent = path.isEmpty() ? load(FXML_PATH + transform(controller.getSimpleName()), this.instances.get(route)) : load(path, this.instances.get(route));

        callAnnotation(route, controller, ControllerEvent.onRender.class, parameters);

        return parent;
    }

    /**
     * Calls all methods annotated with a certain annotation in the provided controller.
     *
     * @param route      The route of the controller
     * @param controller The controller class
     * @param annotation The annotation to look for
     * @param parameters The parameters to pass to the methods
     */
    private void callAnnotation(@NotNull String route, @NotNull Class<?> controller, @NotNull Class<? extends Annotation> annotation, @NotNull Map<@NotNull String, @Nullable Object> parameters) {
        Reflections reflections = new Reflections(controller, Scanners.MethodsAnnotated);
        reflections.getMethodsAnnotatedWith(annotation).forEach(method -> {
            try {
                method.invoke(instances.get(route), applicableParameters(method, parameters));
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
                    Param param = parameter.getAnnotation(Param.class);
                    if (param == null) return null;
                    return parameters.get(param.name());
                })
                .toArray();
    }

    /**
     * Loads a fxml file.
     *
     * @param fileName The name of the file (with path and file extension)
     * @param factory  The controller factory to use
     * @return A parent representing the fxml file
     */
    protected @NotNull Parent load(@NotNull String fileName, @NotNull Object factory) {
        FXMLLoader loader = new FXMLLoader(baseClass.getResource(fileName));
        loader.setControllerFactory(c -> factory);
        try {
            return loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Transforms a class name to a fxml file name.
     * Example: ExampleController --> example.fxml
     *
     * @param className The name of the class (should be class.getSimpleName())
     * @return The name of the fxml file
     */
    protected String transform(String className) {
        return className.replace("Controller", "").toLowerCase() + ".fxml";
    }

}

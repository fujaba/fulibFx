package io.github.sekassel.jfxframework.controller;

import dagger.Lazy;
import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.event.onDestroy;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.controller.SubController;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.controller.building.ControllerBuildFactory;
import io.github.sekassel.jfxframework.data.Tuple;
import io.github.sekassel.jfxframework.util.Util;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static io.github.sekassel.jfxframework.util.Constants.FXML_PATH;

/**
 * Manages the initialization, rendering and destroying of controllers.
 * <p>
 * This class is used internally by the framework and should not be used directly.
 */
@Singleton
public class ControllerManager {

    // Set of controllers that have been initialized and are currently displayed
    private final Map<Object, Collection<WeakReference<Object>>> currentlyDisplayedControllers = new HashMap<>();

    @Inject
    Lazy<Router> router;

    // The base class of the framework, used to load resources (relative to the base class)
    private Class<? extends FxFramework> baseClass;

    @Inject
    public ControllerManager() {
    }

    /**
     * Initializes and renders the given controller. Calls the onInit and onRender methods. See {@link #init(Object, Map)} and {@link #render(Object, Map)}.
     * <p>
     * The controller instance will be added to the set of initialized controllers and will be destroyed when a new main controller is set.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller
     */
    public Parent initAndRender(Object instance, Map<String, Object> parameters) {

        // Call the onInit method
        init(instance, parameters);

        // Render the controller
        return render(instance, parameters);
    }

    /**
     * Initializes the given controller/component.
     * Calls the onInit method(s) and recursively initializes all sub-controllers.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     */
    public void init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {

        // Check if the instance is a controller
        if (!Util.isController(instance))
            throw new IllegalArgumentException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Check if this exact instance has already been initialized
        if (this.currentlyDisplayedControllers.containsKey(instance))
            throw new IllegalArgumentException("Controller '%s' has already been initialized.".formatted(instance.getClass().getName()));

        // Inject parameters into the controller fields
        Reflection.fillParametersIntoFields(instance, parameters);

        // Call the onInit method(s)
        Reflection.callMethodsWithAnnotation(instance, onInit.class, parameters);

        // Search for sub-controllers
        List<Field> subControllerField = Reflection.getFieldsWithAnnotation(instance.getClass(), SubController.class)
                .stream()
                .filter(field -> {
                    if (!field.getType().isAnnotationPresent(Component.class)) {
                        FxFramework.logger().warning("Field '%s' in class '%s' is annotated with @SubController but is not a Component.".formatted(field.getName(), instance.getClass().getName()));
                        return false;
                    }
                    return true;
                }).toList();

        // Initialize all sub-controllers and add them to the map of initialized controllers
        List<WeakReference<Object>> subControllers = new ArrayList<>();
        this.currentlyDisplayedControllers.put(instance, subControllers);
        Reflection.callMethodsForFieldInstances(instance, subControllerField, (subController) -> {
            subControllers.add(new WeakReference<>(subController));
            init(subController, parameters);
        });
    }

    /**
     * Renders the given controller/component instance. Renders all sub-controllers recursively and then calls the onRender method(s).
     * <p>
     * If the controller specifies a fxml file in its {@link Controller#view()},
     * it will be loaded and the controller will be set as the controller of the fxml file.
     * <p>
     * If the component extends from a JavaFX Parent, the component itself will be rendered and returned.
     * This can be combined with the {@link Controller#view()} to set the controller as the root of the fxml file.
     * <p>
     * If the controller specifies a method as {@link Controller#view()}, the method will be called and the returned Parent will be returned.
     * In order to specify a method, the view must start with a '#'. The method must be in the controller class and must return a (subclass of) Parent.
     * Example: {@code @Controller(view = "#getView")} will call the method {@code Parent getView()} in the controller.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller/component
     */
    public Parent render(Object instance, Map<String, Object> parameters) {

        // Check if the instance is a controller/component
        boolean component = instance.getClass().isAnnotationPresent(Component.class) && Util.isComponent(instance);

        if (!component && !instance.getClass().isAnnotationPresent(Controller.class))
            throw new IllegalArgumentException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Check if the instance has been initialized
        if (!currentlyDisplayedControllers.containsKey(instance)) {
            throw new IllegalArgumentException("Controller '%s' has not been initialized.".formatted(instance.getClass().getName()));
        }

        // Render all sub-controllers
        currentlyDisplayedControllers.get(instance).forEach(weakReference -> {
            Object subController = weakReference.get();
            if (subController != null) {
                // We can safely ignore the return value here, as the sub-controllers will all be components and therefore do not have an 'external' view
                render(subController, parameters);
            }
        });

        // Get the view of the controller
        Parent parent;
        String view = component ?
                instance.getClass().getAnnotation(Component.class).view() :
                instance.getClass().getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Parent, render it
        // This can be combined with the view annotation to set the controller as the root of the fxml file
        if (component) {
            parent = view.isEmpty() ? (Parent) instance : loadFXML(view, instance, parameters, true);
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

        // If the controller specifies a fxml file, load it. This will also load sub-controllers specified in the FXML file
        else {
            String fxmlPath = view.isEmpty() ? FXML_PATH + Util.transform(instance.getClass().getSimpleName()) + ".fxml" : view;
            parent = loadFXML(fxmlPath, instance, parameters, false);
        }

        // Call the onRender method
        Reflection.callMethodsWithAnnotation(instance, onRender.class, parameters);

        return parent;
    }

    /**
     * Destroys the given controller by calling all methods annotated with {@link onDestroy}.
     * If the controller has an undestroyed Subscriber field, the destroy method of the subscriber will be called.
     *
     * @param instance The controller instance to destroy
     */
    public void destroy(@NotNull Object instance) {
        if (!Util.isController(instance))
            throw new IllegalArgumentException("Class '%s' is not a controller.".formatted(instance.getClass().getName()));

        // Call destroy methods
        Reflection.callMethodsWithAnnotation(instance, onDestroy.class, Map.of());

        // In development mode, check for undestroyed subscribers
        if (Util.runningInDev()) {
            Reflection.getFieldsOfType(instance.getClass(), Subscriber.class) // Get all Subscriber fields
                    .stream()
                    .map(field -> {
                        try {
                            field.setAccessible(true);
                            return Tuple.of(field, (Subscriber) field.get(instance)); // Get the Subscriber instance, if it exists
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(tuple -> tuple.first() != null)
                    .filter(tuple -> !tuple.second().disposed()) // Filter out disposed subscribers
                    .forEach(tuple ->
                            FxFramework.logger().warning("Found undestroyed subscriber '%s' in class '%s'.".formatted(tuple.first().getName(), instance.getClass().getName()))
                    );
        }
    }

    /**
     * Destroys all controllers that have been initialized and are currently displayed.
     */
    public void cleanup() {
        currentlyDisplayedControllers.forEach((controller, subControllers) -> {
            subControllers.forEach(weakReference -> {
                Object subController = weakReference.get();
                if (subController != null) {
                    destroy(subController);
                }
            });
            destroy(controller);
        });
        currentlyDisplayedControllers.clear();
    }

    /**
     * Loads a fxml file using a custom controller factory.
     * This method is used internally by the framework and should not be used directly.
     * <p>
     * If the fxml file contains an element with a controller class annotated with {@link Controller},
     * an instance provided by the router will be used as the controller for the element.
     *
     * @param fileName The name of the fxml resource file (with path and file extension)
     * @param instance The controller instance to use
     * @return A parent representing the fxml file
     */
    public @NotNull Parent loadFXML(@NotNull String fileName, @NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters, boolean setRoot) {

        URL url = baseClass.getResource(fileName);
        if (url == null) {
            throw new RuntimeException("Could not find resource '" + fileName + "'");
        }

        File file = Util.getResourceAsLocalFile(baseClass, fileName);

        // If the file exists, use it instead of the resource (development mode, allows for hot reloading)
        if (file.exists()) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException("File '" + file.getAbsolutePath() + "' exists, but could not be converted to URL.", e);
            }
        }

        ControllerBuildFactory builderFactory = new ControllerBuildFactory(instance);

        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(c -> instance);
        loader.setBuilderFactory(builderFactory);

        if (setRoot) {
            loader.setRoot(instance);
        }

        try {
            return loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Sets the base class of the framework.
     *
     * @param clazz The base class
     */
    public void setMainClass(Class<? extends FxFramework> clazz) {
        this.baseClass = clazz;
    }

}
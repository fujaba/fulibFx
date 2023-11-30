package io.github.sekassel.jfxframework.controller;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.building.ControllerBuildFactory;
import io.github.sekassel.jfxframework.util.Util;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.sekassel.jfxframework.util.Constants.FXML_PATH;

/**
 * Manages the initialization, rendering and destroying of controllers.
 * <p>
 * This class is used internally by the framework and should not be used directly.
 */
public class ControllerManager {

    // Set of controllers that have been initialized and are currently displayed
    private final Set<Object> controllers = new HashSet<>();
    // The base class of the framework, used to load resources (relative to the base class)
    private Class<? extends FxFramework> baseClass;

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
        // Add the controller to the set of initialized controllers
        controllers.add(instance);

        // Call the onInit method
        init(instance, parameters);

        // Render the controller
        return render(instance, parameters);
    }

    public void init(Object instance, Map<String, Object> parameters) {
        Reflection.callMethodsWithAnnotation(instance, ControllerEvent.onInit.class, parameters);
    }

    /**
     * Renders the given controller. Calls the onRender method.
     * <p>
     * If the controller specifies a fxml file in its {@link Controller#view()},
     * it will be loaded and the controller will be set as the controller of the fxml file.
     * <p>
     * If the controller extends from a JavaFX Parent, the controller itself will be returned.
     * This can be combined with the {@link Controller#view()} to set the controller as the root of the fxml file.
     * <p>
     * If the controller specifies a method as {@link Controller#view()}, the method will be called and the returned Parent will be returned.
     * In order to specify a method, the view must start with a '#'. The method must be in the controller class and must return a (subclass of) Parent.
     * Example: {@code @Controller(view = "#getView")} will call the method {@code Parent getView()} in the controller.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller
     */
    public Parent render(Object instance, Map<String, Object> parameters) {
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
            parent = loadFXML(fxmlPath, instance, parameters, Parent.class.isAssignableFrom(instance.getClass()));
        }

        // Call the onRender method
        Reflection.callMethodsWithAnnotation(instance, ControllerEvent.onRender.class, parameters);

        return parent;
    }

    /**
     * Destroys the given controller by calling all methods annotated with {@link ControllerEvent.onDestroy}.
     * If the controller implements {@link Subscriber}, the {@link Subscriber#destroy()} method will be called.
     *
     * @param instance The controller instance to destroy
     */
    public void destroy(@NotNull Object instance) {
        Reflection.callMethodsWithAnnotation(instance, ControllerEvent.onDestroy.class, Map.of());

        if (instance instanceof Subscriber) {
            ((Subscriber) instance).destroy();
        }
    }

    /**
     * Destroys all controllers that have been initialized and are currently displayed.
     */
    public void cleanup() {
        controllers.forEach(this::destroy);
        controllers.clear();
    }

    /**
     * Loads a fxml file using a custom controller factory.
     * This method is used internally by the framework and should not be used directly.
     * <p>
     * If the fxml file contains an element with a controller class annotated with {@link Controller},
     * an instance provided by the router will be used as the controller for the element.
     *
     * @param fileName The name of the fxml resource file (with path and file extension)
     * @param factory  The controller factory to use
     * @return A parent representing the fxml file
     */
    public @NotNull Parent loadFXML(@NotNull String fileName, @NotNull Object factory, @NotNull Map<@NotNull String, @Nullable Object> parameters, boolean setRoot) {
        URL url = baseClass.getResource(fileName);
        System.out.println(baseClass);
        if (url == null) throw new RuntimeException("Could not find resource '" + fileName + "'");

        ControllerBuildFactory builderFactory = new ControllerBuildFactory(FxFramework.router(), parameters);

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

    public void setMainClass(Class<? extends FxFramework> clazz) {
        this.baseClass = clazz;
    }
}

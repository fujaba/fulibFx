package io.github.sekassel.jfxframework.controller;

import dagger.Lazy;
import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.annotation.controller.SubComponent;
import io.github.sekassel.jfxframework.annotation.event.onDestroy;
import io.github.sekassel.jfxframework.annotation.event.onInit;
import io.github.sekassel.jfxframework.annotation.event.onRender;
import io.github.sekassel.jfxframework.controller.building.ControllerBuildFactory;
import io.github.sekassel.jfxframework.controller.exception.IllegalControllerException;
import io.github.sekassel.jfxframework.data.Tuple;
import io.github.sekassel.jfxframework.util.Util;
import io.github.sekassel.jfxframework.util.disposable.RefreshableCompositeDisposable;
import io.github.sekassel.jfxframework.util.reflection.Reflection;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Manages the initialization, rendering and destroying of controllers.
 * <p>
 * This class is used internally by the framework and should not be used directly.
 */
@Singleton
public class ControllerManager {

    // Map of controllers that have been initialized
    private final RefreshableCompositeDisposable cleanup = new RefreshableCompositeDisposable();

    @Inject
    Lazy<Router> router;

    @Inject
    public ControllerManager() {
    }

    /**
     * Initializes and renders the given controller. Calls the onInit and onRender methods. See {@link #init(Object, Map)} and {@link #render(Object, Map)}.
     * <p>
     * The controller/component instance(s) will be added to the set of initialized controllers and will be destroyed when a new main controller is set.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller
     */
    public Parent initAndRender(Object instance, Map<String, Object> parameters) {

        // Initialize the controller
        init(instance, parameters, true);

        // Render the controller
        return render(instance, parameters);
    }


    /**
     * Initializes the given controller/component. Calls the onInit method(s) and recursively initializes all subcomponents.
     * <p>
     * <b>Order:</b> Controller -> Subcomponents -> Subcomponents of subcomponents -> ...
     *
     * @param instance                   The controller/component instance
     * @param parameters                 The parameters to pass to the controller/component
     * @param disposeOnNewMainController Whether the controller/component should be destroyed when a new main controller is set
     * @return A disposable that can be used to destroy the controller/component and all its subcomponents manually
     */
    public Disposable init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters, boolean disposeOnNewMainController) {
        Disposable disposable = Disposable.fromRunnable(() -> destroy(instance));

        init(instance, parameters);

        if (disposeOnNewMainController) {
            this.cleanup.add(disposable);
        }

        return disposable;
    }

    /**
     * Initializes the given controller/component.
     * Calls the onInit method(s) and recursively initializes all sub-controllers.
     * <p>
     * All initialized controllers will be added to the list of initialized controllers.
     * If a controller/component is added to the list, all its subcomponents will follow right after it.
     *
     * @param instance   The controller/component instance
     * @param parameters The parameters to pass to the controller
     */
    private void init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {

        // Check if the instance is a controller
        if (!Util.isController(instance))
            throw new IllegalControllerException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Inject parameters into the controller fields
        Reflection.fillParametersIntoFields(instance, parameters);

        // Call the onInit method(s)
        Reflection.callMethodsWithAnnotation(instance, onInit.class, parameters);

        // Initialize all sub-controllers
        Reflection.callMethodsForFieldInstances(instance, getSubComponentFields(instance), (subController) -> init(subController, parameters));

    }

    /**
     * Renders the given controller/component instance. Renders all sub-controllers recursively and then calls the onRender method(s) before returning the rendered controller.
     * <p>
     * <b>Important:</b> This method assumes that the controller has already been initialized.
     * The controller will <u>not automatically be destroyed</u> when using only this method.
     * Use {@link #init(Object, Map, boolean)} before, to initialize and automatically destroy the controller or destroy it yourself afterward.
     * <p>
     * If the controller specifies a fxml file in its {@link Controller#view()},
     * it will be loaded and the controller will be set as the controller of the fxml file.
     * <p>
     * If the controller is a component (extends from a JavaFX Parent), the component itself will be rendered and returned.
     * This can be combined with the {@link Component#view()} to set the controller as the root of the fxml file.
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

        // Render all sub-controllers
        Reflection.callMethodsForFieldInstances(instance, getSubComponentFields(instance), (subController) -> render(subController, parameters));

        // Get the view of the controller
        Parent parent;
        String view = component ?
                instance.getClass().getAnnotation(Component.class).view() :
                instance.getClass().getAnnotation(Controller.class).view();

        // If the controller extends from a javafx Parent, render it
        // This can be combined with the view annotation to set the controller as the root of the fxml file
        if (component) {
            parent = view.isEmpty() ? (Parent) instance : loadFXML(view, instance, true);
        }

        // If the controller specifies a method as view, call it
        else if (view.startsWith("#")) {
            String methodName = view.substring(1);
            try {
                Method method = instance.getClass().getDeclaredMethod(methodName);
                if (!Parent.class.isAssignableFrom(method.getReturnType()))
                    throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' does not return a Parent.");
                method.setAccessible(true);
                parent = (Parent) method.invoke(instance);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' does not exist.");
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException("Method '" + methodName + "()' in class '" + instance.getClass().getName() + "' could not be called.", e);
            }
        }

        // If the controller specifies a fxml file, load it. This will also load sub-controllers specified in the FXML file
        else {
            String fxmlPath = view.isEmpty() ? Util.transform(instance.getClass().getSimpleName()) + ".fxml" : view;
            parent = loadFXML(fxmlPath, instance, false);
        }

        // Call the onRender method
        Reflection.callMethodsWithAnnotation(instance, onRender.class, parameters);

        return parent;
    }

    /**
     * Destroys the given controller/component by calling all methods annotated with {@link onDestroy}.
     * <p>
     * <b>Important:</b> Do not use this method on a controller's view but on the controller itself.
     * <p>
     * If the controller has subcomponents, they will be destroyed first recursively in reverse order.
     * <p>
     * If the controller has an undestroyed Subscriber field, a warning will be logged in development mode.
     *
     * @param instance The controller/component instance to destroy
     */
    public void destroy(@NotNull Object instance) {
        if (!Util.isController(instance))
            throw new IllegalArgumentException("Class '%s' is not a controller or component.".formatted(instance.getClass().getName()));

        // Destroying should be done in exactly the reverse order of initialization
        List<Field> subComponentFields = new ArrayList<>(getSubComponentFields(instance));
        Collections.reverse(subComponentFields);

        // Destroy all sub-controllers
        Reflection.callMethodsForFieldInstances(instance, subComponentFields, this::destroy);

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
        cleanup.dispose();
        cleanup.refresh();
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
    public @NotNull Parent loadFXML(@NotNull String fileName, @NotNull Object instance, boolean setRoot) {

        URL url = instance.getClass().getResource(fileName);
        if (url == null) {
            String urlPath = instance.getClass().getPackageName().replace(".", "/") + "/" +  fileName;
            throw new RuntimeException("Could not find resource '" + urlPath + "'");
        }

        File file = Util.getResourceAsLocalFile(instance.getClass(), fileName);

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
            throw new RuntimeException("Couldn't load the FXML file for controller '%s'".formatted(instance.getClass()), exception);
        }
    }

    /**
     * Returns a list of all fields in the given instance that are annotated with {@link SubComponent}.
     *
     * @param instance The instance to get the fields from
     * @return A list of all fields in the given instance that are annotated with {@link SubComponent}
     */
    @Unmodifiable
    private List<Field> getSubComponentFields(Object instance) {
        return Reflection.getFieldsWithAnnotation(instance.getClass(), SubComponent.class)
                .stream()
                .filter(field -> {
                    if (!field.getType().isAnnotationPresent(Component.class)) {
                        FxFramework.logger().warning("Field '%s' in class '%s' is annotated with @SubComponent but is not a subcomponent.".formatted(field.getName(), instance.getClass().getName()));
                        return false;
                    }
                    return true;
                }).toList();
    }

}
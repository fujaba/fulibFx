package org.fulib.fx.controller;

import dagger.Lazy;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;
import org.fulib.fx.FulibFxApp;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Resource;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onKey;
import org.fulib.fx.controller.building.ControllerBuildFactory;
import org.fulib.fx.controller.exception.IllegalControllerException;
import org.fulib.fx.controller.internal.FxSidecar;
import org.fulib.fx.controller.internal.ReflectionSidecar;
import org.fulib.fx.data.disposable.RefreshableCompositeDisposable;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.FileUtil;
import org.fulib.fx.util.FrameworkUtil;
import org.fulib.fx.util.KeyEventHolder;
import org.fulib.fx.util.reflection.Reflection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.fulib.fx.util.FrameworkUtil.error;

/**
 * Manages the initialization, rendering and destroying of controllers.
 * <p>
 * This class is used internally by the framework and should not be used directly.
 */
@Singleton
@ApiStatus.Internal
public class ControllerManager {

    // Map of controllers that have been initialized
    private final RefreshableCompositeDisposable cleanup = new RefreshableCompositeDisposable();

    private static ResourceBundle defaultResourceBundle;
    private final Map<Class<?>, FxSidecar<?>> sidecars = new IdentityHashMap<>();

    private final Map<Object, Collection<KeyEventHolder>> keyEventHandlers = new HashMap<>();

    @Inject
    Lazy<FulibFxApp> app;

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
    public Node initAndRender(Object instance, Map<String, Object> parameters) {

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
     * Calls the onInit method(s) and recursively initializes all subcomponents.
     * <p>
     * All initialized controllers will be added to the list of initialized controllers.
     * If a controller/component is added to the list, all its subcomponents will follow right after it.
     *
     * @param instance   The controller/component instance
     * @param parameters The parameters to pass to the controller
     */
    public void init(@NotNull Object instance, @NotNull Map<@NotNull String, @Nullable Object> parameters) {

        // Check if the instance is a controller
        if (!ControllerUtil.isController(instance))
            throw new IllegalControllerException(error(1001).formatted(instance.getClass().getName()));

        getSidecar(instance).init(instance, parameters);
    }

    private <T> @NotNull FxSidecar<T> getSidecar(@NotNull T instance) {
        final Class<?> instanceClass = instance.getClass();
        if (sidecars.containsKey(instanceClass)) {
            return (FxSidecar<T>) sidecars.get(instanceClass);
        } else {
            final FxSidecar<T> sidecar = createSidecar((Class<T>) instanceClass);
            sidecars.put(instanceClass, sidecar);
            return sidecar;
        }
    }

    private <T> @NotNull FxSidecar<T> createSidecar(Class<T> componentClass) {
        final Class<?> sidecarClass = Class.forName(componentClass.getModule(), componentClass.getName() + "_Fx");
        if (sidecarClass == null) {
            return new ReflectionSidecar<>(this);
        }
        try {
            return (FxSidecar<T>) sidecarClass.getDeclaredConstructor(ControllerManager.class).newInstance(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders the given controller/component instance. Renders all subcomponents recursively and then calls the onRender method(s) before returning the rendered controller.
     * <p>
     * <b>Important:</b> This method assumes that the controller has already been initialized.
     * The controller will <u>not automatically be destroyed</u> when using only this method.
     * Use {@link #init(Object, Map, boolean)} before, to initialize and automatically destroy the controller or destroy it yourself afterward.
     * <p>
     * If the controller specifies a fxml file in its {@link Controller#view()},
     * it will be loaded and the controller will be set as the controller of the fxml file.
     * <p>
     * If the controller is a component (extends from a JavaFX Node), the component itself will be rendered and returned.
     * This can be combined with the {@link Component#view()} to set the controller as the root of the fxml file.
     * <p>
     * If the controller specifies a method as {@link Controller#view()}, the method will be called and the returned Parent will be used as the view.
     * In order to specify a method, the view must start with a '#'. The method must be in the controller class and must return a (subclass of) Parent.
     * Example: {@code @Controller(view = "#getView")} will call the method {@code Parent getView()} in the controller.
     *
     * @param instance   The controller instance
     * @param parameters The parameters to pass to the controller
     * @return The rendered controller/component
     */
    public Node render(Object instance, Map<String, Object> parameters) {

        // Check if the instance is a controller/component
        boolean component = instance.getClass().isAnnotationPresent(Component.class) && ControllerUtil.isComponent(instance);

        if (!component && !instance.getClass().isAnnotationPresent(Controller.class))
            throw new IllegalArgumentException(error(1001).formatted(instance.getClass().getName()));

        final Node node = getSidecar(instance).render(instance, parameters);

        // TODO Register key events via Sidecar
        registerKeyEvents(instance);

        return node;
    }

    /**
     * Registers all key events for the given controller instance.
     *
     * @param instance The controller instance
     */
    private void registerKeyEvents(Object instance) {
        Reflection.getMethodsWithAnnotation(instance.getClass(), onKey.class).forEach(method -> {

            onKey annotation = method.getAnnotation(onKey.class);
            EventType<KeyEvent> type = annotation.type().asEventType();
            EventHandler<KeyEvent> handler = createKeyEventHandler(method, instance, annotation);

            keyEventHandlers.computeIfAbsent(instance, k -> new HashSet<>()).add(new KeyEventHolder(annotation.target(), type, handler));

            switch (annotation.target()) {
                case SCENE -> app.get().stage().getScene().addEventFilter(type, handler);
                case STAGE -> app.get().stage().addEventFilter(type, handler);
            }
        });
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
        if (!ControllerUtil.isController(instance))
            throw new IllegalArgumentException(error(1001).formatted(instance.getClass().getName()));

        getSidecar(instance).destroy(instance);

        // TODO Unregister key events via Sidecar
        cleanUpListeners(instance);

        // In development mode, check for undestroyed subscribers
        if (FrameworkUtil.runningInDev()) {
            Reflection.getFieldsOfType(instance.getClass(), Subscriber.class) // Get all Subscriber fields
                    .map(field -> {
                        try {
                            field.setAccessible(true);
                            return new Pair<>(field, (Subscriber) field.get(instance)); // Get the Subscriber instance, if it exists
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(pair -> pair.getKey() != null)
                    .filter(pair -> !pair.getValue().isDisposed()) // Filter out disposed subscribers
                    .forEach(pair ->
                            FulibFxApp.LOGGER.warning("Found undestroyed subscriber '%s' in class '%s'.".formatted(pair.getKey().getName(), instance.getClass().getName()))
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
    @ApiStatus.Internal
    public @NotNull Node loadFXML(@NotNull String fileName, @NotNull Object instance, boolean setRoot) {

        URL url = instance.getClass().getResource(fileName);
        if (url == null) {
            String urlPath = instance.getClass().getPackageName().replace(".", "/") + "/" + fileName;
            throw new RuntimeException(error(2000).formatted(urlPath));
        }

        File file = FileUtil.getResourceAsLocalFile(FulibFxApp.resourcesPath(), instance.getClass(), fileName);

        // If the file exists, use it instead of the resource (development mode, allows for hot reloading)
        if (file.exists()) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(error(2001).formatted(file.getAbsolutePath()), e);
            }
        }

        // Set the controller factory and builder factory
        ControllerBuildFactory builderFactory = new ControllerBuildFactory(instance);
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(c -> instance);
        loader.setBuilderFactory(builderFactory);

        // If the controller has a resource bundle, use it
        ResourceBundle resourceBundle = getResourceBundle(instance);
        if (resourceBundle != null) {
            loader.setResources(resourceBundle);
        }

        // Set the root of the FXML file when a component specifies a view
        if (setRoot) {
            loader.setRoot(instance);
        }

        // Load the FXML file
        try {
            return loader.load();
        } catch (IOException exception) {
            throw new RuntimeException(error(2002).formatted(instance.getClass()), exception);
        }
    }

    /**
     * Returns the resource bundle of the given instance if it has one.
     * If no resource bundle is set, the default resource bundle will be used.
     * If no default resource bundle is set, null will be returned.
     *
     * @param instance The instance to get the resource bundle from
     * @return The resource bundle of the given instance if it has one or the default resource bundle
     * @throws RuntimeException If the instance has more than one field annotated with {@link Resource}
     */
    private @Nullable ResourceBundle getResourceBundle(@NotNull Object instance) {
        return getSidecar(instance).getResources(instance);
    }

    public @Nullable ResourceBundle getDefaultResourceBundle() {
        return defaultResourceBundle;
    }

    /**
     * Sets the default resource bundle for all controllers that don't have a resource bundle set.
     *
     * @param resourceBundle The default resource bundle
     */
    public void setDefaultResourceBundle(ResourceBundle resourceBundle) {
        defaultResourceBundle = resourceBundle;
    }

    /**
     * Creates an event handler for the given method and instance that will be called when the specified key event occurs.
     *
     * @param method     The method to call
     * @param instance   The instance to call the method on
     * @param annotation The annotation with the key event information
     * @return An event handler for the given method and instance
     */
    private EventHandler<KeyEvent> createKeyEventHandler(Method method, Object instance, onKey annotation) {
        boolean hasEventParameter = method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(KeyEvent.class);

        if (!hasEventParameter && method.getParameterCount() != 0) {
            throw new RuntimeException(error(1010).formatted(method.getName(), instance.getClass().getName()));
        }

        method.setAccessible(true);

        return event -> {
            if (keyEventMatchesAnnotation(event, annotation)) {
                try {
                    if (hasEventParameter) {
                        method.invoke(instance, event);
                    } else {
                        method.invoke(instance);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(error(1005).formatted(method.getName(), annotation.getClass().getSimpleName(), method.getClass()), e);
                }
            }
        };
    }

    private boolean keyEventMatchesAnnotation(KeyEvent event, onKey annotation) {
        return (annotation.code() == KeyCode.UNDEFINED || event.getCode() == annotation.code()) &&
                (annotation.character().isEmpty() || event.getCharacter().equals(annotation.character())) &&
                (annotation.text().isEmpty() || event.getText().equals(annotation.text())) &&
                (event.isShiftDown() || !annotation.shift()) &&
                (event.isControlDown() || !annotation.control()) &&
                (event.isAltDown() || !annotation.alt()) &&
                (event.isMetaDown() || !annotation.meta());
    }

    /**
     * Clears all key handlers registered for the given instance.
     *
     * @param instance The instance to clear the key handlers for
     */
    private void cleanUpListeners(Object instance) {
        final Collection<KeyEventHolder> handlers = keyEventHandlers.remove(instance);
        if (handlers == null) {
            return;
        }
        for (KeyEventHolder holder : handlers) {
            switch (holder.target()) {
                case SCENE -> app.get().stage().getScene().removeEventFilter(holder.type(), holder.handler());
                case STAGE -> app.get().stage().removeEventFilter(holder.type(), holder.handler());
            }
        }
    }

    /**
     * Returns the title of the given controller instance if it has one.
     * If the title is a key, the title will be looked up in the resource bundle of the controller.
     *
     * @param instance The controller instance
     * @return The title of the controller
     */
    public Optional<String> getTitle(@NotNull Object instance) {
        return Optional.ofNullable(getSidecar(instance).getTitle(instance));
    }
}

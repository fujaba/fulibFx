package org.fulib.fx;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.disposables.DisposableContainer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.controller.AutoRefresher;
import org.fulib.fx.dagger.DaggerFrameworkComponent;
import org.fulib.fx.dagger.FrameworkComponent;
import org.fulib.fx.util.ControllerUtil;
import org.fulib.fx.util.FrameworkUtil;
import org.fulib.fx.util.ReflectionUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public abstract class FulibFxApp extends Application {

    public static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);
    public static final Logger LOGGER = Logger.getLogger(FulibFxApp.class.getName());

    private static Path resourcesPath = Path.of("src/main/resources");

    // The component holding the required dependencies like router, controller manager, etc.
    private FrameworkComponent component;

    // The stage of the application
    private Stage stage;

    // The instance of the current main controller (last controller displayed with show())
    private Object currentMainController;

    /**
     * Returns the path to the 'resources' directory.
     *
     * @return The path to the resources directory
     */
    public static @NotNull Path resourcesPath() {
        return resourcesPath;
    }

    /**
     * Sets the path to the 'resources' directory. If your 'resources' directory differs from "src/main/resources", you can set it here.
     * <p>
     * If your project is contained in another directory (e.g. gradle submodule), you can set the path to the 'resources' directory here.
     *
     * @param path The path to the resources directory (e.g. "example/src/main/resources")
     */
    public static void setResourcesPath(@NotNull Path path) {
        resourcesPath = path;
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * If a disposable is provided, the disposable will be modified to include the cleanup of the rendered component.
     * The provided disposable can be used to destroy the component with all its children manually.
     * <p>
     * If no disposable is provided, the component has to be destroyed manually, for example by calling {@link #destroy(Parent)}.
     *
     * @param component The component instance
     * @param <T>       The type of the component
     * @param onDestroy A disposable which will be modified to include the disposable of the component
     * @return The rendered component
     */
    public @NotNull <T extends Parent> T initAndRender(@NotNull T component, DisposableContainer onDestroy) {
        return initAndRender(component, Map.of(), onDestroy);
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * The component has to be destroyed manually, for example by calling {@link #destroy(Parent)}.
     *
     * @param component The component instance
     * @param <T>       The type of the component
     * @return The rendered component
     */
    public @NotNull <T extends Parent> T initAndRender(@NotNull T component) {
        return initAndRender(component, Map.of(), null);
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * The component has to be destroyed manually, for example by calling {@link #destroy(Parent)}.
     *
     * @param component The component instance
     * @param params    The arguments passed to the component
     * @param <T>       The type of the component
     * @return The rendered component
     */
    public @NotNull <T extends Parent> T initAndRender(@NotNull T component, Map<String, Object> params) {
        return initAndRender(component, params, null);
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * If a disposable is provided, the disposable will be modified to include the cleanup of the rendered component.
     * The provided disposable can be used to destroy the component with all its children manually.
     * <p>
     * If no disposable is provided, the component has to be destroyed manually, for example by calling {@link #destroy(Parent)}.
     *
     * @param component The component instance
     * @param params    The arguments passed to the component
     * @param onDestroy A disposable which will be modified to include the disposable of the component
     * @return The rendered component
     */
    public @NotNull <T extends Parent> T initAndRender(@NotNull T component, Map<String, Object> params, DisposableContainer onDestroy) {
        if (!ControllerUtil.isComponent(component))
            throw new IllegalArgumentException(FrameworkUtil.error(1001).formatted(component.getClass().getName()));

        Disposable disposable = this.component.controllerManager().init(component, params, false);
        if (onDestroy != null) {
            onDestroy.add(disposable);
        }

        @SuppressWarnings("unchecked") // We know that the component will return itself as the view
        T rendered = (T) this.frameworkComponent().controllerManager().render(component, params);
        return rendered;
    }

    /**
     * Destroys a rendered controller extending Parent.
     *
     * @param rendered The rendered controller instance
     * @param <T>      The type of the controller
     * @return The destroyed controller instance
     * @throws IllegalArgumentException If the given instance is not a controller extending Parent
     */
    public @NotNull <T extends Parent> T destroy(@NotNull T rendered) {
        this.frameworkComponent().controllerManager().destroy(rendered);
        return rendered;
    }

    @MustBeInvokedByOverriders
    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        this.component = DaggerFrameworkComponent.builder().framework(this).build();

        Scene scene = new Scene(new Pane()); // Show default scene

        this.stage.setScene(scene);
        this.stage.show();
    }

    @Override
    public void stop() {
        this.currentMainController = null;
        cleanup();
        autoRefresher().close();
        System.exit(0);
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param route The route of the controller to render
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull String route) {
        return show(route, Map.of());
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param controller The controller to render
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull Object controller) {
        return show(controller, Map.of());
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param controller The controller to render
     * @param params     The arguments passed to the controller
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull Object controller, @NotNull Map<String, Object> params) {
        cleanup();
        Parent renderedParent = this.frameworkComponent().controllerManager().initAndRender(controller, params);
        this.currentMainController = controller;
        onShow(Optional.empty(), controller, renderedParent, params);
        display(renderedParent);
        return renderedParent;
    }

    /**
     * Initializes, renders and displays a controller.
     *
     * @param route  The route of the controller to render
     * @param params The arguments passed to the controller
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> params) {
        cleanup();
        Pair<Object, Parent> rendered = this.component.router().renderRoute(route, params);
        this.currentMainController = rendered.getKey();
        display(rendered.getValue());
        onShow(Optional.of(route), rendered.getKey(), rendered.getValue(), params);
        return rendered.getValue();
    }

    /**
     * Displays the given parent. Will be called when showing a main controller or when reloading the currently displayed controller.
     * This method can be overridden to add custom behavior like multiple controllers on top of each other.
     *
     * @param parent The parent to display
     */
    protected void display(@NotNull Parent parent) {
        stage.getScene().setRoot(parent);
    }

    // Internal helper method
    protected void cleanup() {
        this.component.controllerManager().cleanup();
    }

    /**
     * Called when the application shows a new controller.
     * <p>
     * This method is called after the controller is initialized and rendered.
     *
     * @param route      The route of the controller (empty if the controller has been shown directly)
     * @param controller The controller instance
     * @param rendered   The rendered parent of the controller
     * @param params     The arguments passed to the controller
     */
    protected void onShow(Optional<String> route, Object controller, Parent rendered, Map<String, Object> params) {
        // Override this method
    }

    /**
     * Returns the stage of the application.
     *
     * @return The stage of the application
     */
    public Stage stage() {
        return this.stage;
    }

    /**
     * Returns the currently used component of the application.
     * <p>
     * <b>Warning:</b> This method should only be used for internal purposes.
     *
     * @return The component
     */
    @ApiStatus.Internal
    public FrameworkComponent frameworkComponent() {
        return this.component;
    }

    /**
     * Registers all routes in the given class.
     *
     * @param routes The class to register the routes from
     */
    public void registerRoutes(Object routes) {
        this.component.router().registerRoutes(routes);
    }

    /**
     * Sets the default resource bundle to use for FXML files if no resource bundle is provided in the controller/component.
     */
    public void setDefaultResourceBundle(ResourceBundle resourceBundle) {
        this.component.controllerManager().setDefaultResourceBundle(resourceBundle);
    }

    /**
     * Returns auto refresher of the application.
     */
    public AutoRefresher autoRefresher() {
        return this.component.autoRefresher();
    }

    /**
     * Returns to the previous controller in the history if possible.
     */
    public void back() {
        cleanup();
        Parent parent = this.component.router().back();
        if (parent != null)
            display(parent);
    }

    /**
     * Forwards to the next controller in the history if possible.
     */
    public void forward() {
        cleanup();
        Parent parent = this.component.router().forward();
        if (parent != null)
            display(parent);
    }

    /**
     * Refreshes the current controller.
     * <p>
     * The controller will be cleaned up and then re-initialized and re-rendered.
     * This method will re-use the current route and parameters and not update the history.
     */
    public void refresh() {
        cleanup();
        Map<String, Object> params = this.component.router().current().getValue(); // Use the same parameters as before
        this.component.controllerManager().init(currentMainController, params, true); // Re-initialize the controller
        Parent parent = this.component.controllerManager().render(currentMainController, params); // Re-render the controller
        ReflectionUtil.resetMouseHandler(stage());
        display(parent);
    }

    /**
     * Returns the instance of the current main controller.
     * This should not be used to change the current main controller.
     *
     * @return The instance of the currently displayed controller
     */
    public Object currentMainController() {
        return currentMainController;
    }

}

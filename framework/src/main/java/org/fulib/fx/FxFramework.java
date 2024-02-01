package org.fulib.fx;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.controller.AutoRefresher;
import org.fulib.fx.dagger.FrameworkComponent;
import org.fulib.fx.data.Rendered;
import org.fulib.fx.data.Tuple;
import org.fulib.fx.dagger.DaggerFrameworkComponent;
import org.fulib.fx.util.Util;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

public abstract class FxFramework extends Application {

    private static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);
    private static final Logger LOGGER = Logger.getLogger(FxFramework.class.getName());
    private static FxFramework instance;

    private static Path resourcesPath = Path.of("src/main/resources");

    // The component holding the required dependencies like router, controller manager, etc.
    private FrameworkComponent component;

    // The stage of the application
    private Stage stage;

    // The instance of the current main controller (last controller displayed with show())
    private Object currentMainController;

    public FxFramework() {
        if (instance != null)
            logger().warning("Multiple instances of FxFramework are not supported.");
        instance = this;
    }

    /**
     * Returns the framework's logger.
     *
     * @return The logger
     */
    public static Logger logger() {
        return LOGGER;
    }

    /**
     * Returns the scheduler of the framework.
     *
     * @return The scheduler
     */
    public static Scheduler scheduler() {
        return FX_SCHEDULER;
    }

    /**
     * Returns the current instance of the framework.
     * <p>
     * This method is used for internal purposes only where the framework instance is not available via dependency injection.
     * It is not guaranteed that this method will work in all cases.
     *
     * @return The current instance of the framework
     */
    public static FxFramework framework() {
        return instance;
    }

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
     * If destroyWithCurrent is false, the method will NOT add the controller to the set of initialized controllers and the
     * controller will not be destroyed when a new main controller is set.
     * <p>
     * If destroyWithCurrent is true, the controller will be added to the set of initialized controllers and will be destroyed when
     * a new main controller is set.
     *
     * @param controller         The controller instance
     * @param destroyWithCurrent Whether the controller shall be destroyed when a new main controller is set
     * @return The rendered controller
     */
    public @NotNull <T extends Parent> Rendered<T> initAndRender(@NotNull T controller, boolean destroyWithCurrent) {
        return initAndRender(controller, Map.of(), destroyWithCurrent);
    }

    /**
     * Initializes and renders a component instance (a controller with the {@link Component} annotation).
     * <p>
     * If destroyWithCurrent is false, the method will NOT add the controller to the set of initialized controllers and the
     * controller will not be destroyed when a new main controller is set.
     * <p>
     * If destroyWithCurrent is true, the controller will be added to the set of initialized controllers and will be destroyed when
     * a new main controller is set.
     *
     * @param component          The component instance
     * @param params             The arguments passed to the component
     * @param destroyWithCurrent Whether the component shall be destroyed when a new main component is set
     * @return The rendered component and a disposable that can be used to destroy the component with all its children manually
     */
    public @NotNull <T extends Parent> Rendered<T> initAndRender(@NotNull T component, Map<String, Object> params, boolean destroyWithCurrent) {
        if (!Util.isComponent(component))
            throw new IllegalArgumentException("Class '%s' is not a component.".formatted(component.getClass().getName()));

        Disposable disposable = this.component.controllerManager().init(component, params, destroyWithCurrent);
        @SuppressWarnings("unchecked") // We know that the component will return itself as the view
        T rendered = (T) this.component.controllerManager().render(component, params);

        return Rendered.of(rendered, disposable);
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
        this.component.controllerManager().destroy(rendered);
        return rendered;
    }

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
     * @param route  The route of the controller to render
     * @param params The arguments passed to the controller
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> params) {
        cleanup();
        Tuple<Object, Parent> rendered = this.component.router().renderRoute(route, params);
        this.currentMainController = rendered.first();
        display(rendered.second());
        onShow(route, rendered.first(), rendered.second(), params);
        return rendered.second();
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
     * @param route      The route of the controller
     * @param controller The controller instance
     * @param rendered   The rendered parent of the controller
     * @param params     The arguments passed to the controller
     */
    protected void onShow(String route, Object controller, Parent rendered, Map<String, Object> params) {
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
        Map<String, Object> params = this.component.router().current().second(); // Use the same parameters as before
        this.component.controllerManager().init(currentMainController, params, true); // Re-initialize the controller
        Parent parent = this.component.controllerManager().render(currentMainController, params); // Re-render the controller
        display(parent); // Display the controller
    }

    /**
     * Returns the instance of the current main controller.
     *
     * @return The instance of the currently displayed controller
     */
    public Object currentMainController() {
        return currentMainController;
    }

}

package io.github.sekassel.jfxframework;

import io.github.sekassel.jfxframework.controller.AutoRefresher;
import io.github.sekassel.jfxframework.controller.ControllerManager;
import io.github.sekassel.jfxframework.controller.Router;
import io.github.sekassel.jfxframework.annotation.controller.Component;
import io.github.sekassel.jfxframework.annotation.controller.Controller;
import io.github.sekassel.jfxframework.dagger.DaggerFrameworkComponent;
import io.github.sekassel.jfxframework.dagger.FrameworkComponent;
import io.github.sekassel.jfxframework.data.Tuple;
import io.github.sekassel.jfxframework.util.Util;
import io.reactivex.rxjava3.core.Scheduler;
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
     * Initializes and renders a controller instance.
     * This method only works with controllers which extend Parent.
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
    public @NotNull <T> T initAndRender(@NotNull T controller, boolean destroyWithCurrent) {
        return initAndRender(controller, Map.of(), destroyWithCurrent);
    }

    /**
     * Initializes and renders a controller instance.
     * This method only works with controllers which extend Parent.
     * <p>
     * If destroyWithCurrent is false, the method will NOT add the controller to the set of initialized controllers and the
     * controller will not be destroyed when a new main controller is set.
     * <p>
     * If destroyWithCurrent is true, the controller will be added to the set of initialized controllers and will be destroyed when
     * a new main controller is set.
     *
     * @param controller         The controller instance
     * @param params             The arguments passed to the controller
     * @param destroyWithCurrent Whether the controller shall be destroyed when a new main controller is set
     * @return The rendered controller
     */
    public @NotNull <T> T initAndRender(@NotNull T controller, Map<String, Object> params, boolean destroyWithCurrent) {
        if (!controller.getClass().isAnnotationPresent(Controller.class) && !controller.getClass().isAnnotationPresent(Component.class))
            throw new IllegalArgumentException("Class '%s' is not a controller.".formatted(controller.getClass().getName()));

        // If the controller shall be destroyed, we can just call initAndRender
        if (destroyWithCurrent) {
            Parent rendered = this.component.controllerManager().initAndRender(controller, params);
            if (Util.isComponent(rendered)) {
                return (T) rendered;
            }
            throw new IllegalStateException("Providing a controller only works for controllers extending Parent.");
        }

        // If the controller shall not be destroyed, we have to manually initialize and render it
        this.component.controllerManager().init(controller, params);
        Parent rendered = this.component.controllerManager().render(controller, params);
        if (Util.isComponent(rendered)) {
            return (T) rendered;
        }
        throw new IllegalStateException("Providing a controller only works for controllers extending Parent.");
    }

    /**
     * Destroys a rendered controller extending Parent.
     *
     * @param rendered The rendered controller instance
     * @param <T>      The type of the controller
     * @return The destroyed controller instance
     * @throws IllegalArgumentException If the given instance is not a controller extending Parent
     */
    public @NotNull <T> T destroy(@NotNull T rendered) {
        this.component.controllerManager().destroy(rendered);
        return rendered;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        this.component = DaggerFrameworkComponent.builder().framework(this).build();
        this.component.controllerManager().setMainClass(this.getClass());

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
     *
     * @return The component
     */
    public FrameworkComponent frameworkComponent() {
        return this.component;
    }

    /**
     * Returns the router of the application.
     */
    public Router router() {
        return this.component.router();
    }

    /**
     * Returns the controller manager of the application.
     */
    public ControllerManager manager() {
        return this.component.controllerManager();
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
        this.manager().init(currentMainController, params); // Re-initialize the controller
        Parent parent = this.manager().render(currentMainController, params); // Re-render the controller
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

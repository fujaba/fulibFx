package io.github.sekassel.jfxframework;

import io.github.sekassel.jfxframework.controller.ControllerManager;
import io.github.sekassel.jfxframework.controller.Router;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.dagger.DaggerFrameworkComponent;
import io.github.sekassel.jfxframework.dagger.FrameworkComponent;
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

import java.util.Map;
import java.util.logging.Logger;

public abstract class FxFramework extends Application {

    private static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);
    private static final Logger LOGGER = Logger.getLogger(FxFramework.class.getName());
    private static FxFramework instance;

    private FrameworkComponent component;
    private Stage stage;

    public FxFramework() {
        instance = this;
    }

    /**
     * Returns the logger of the framework.
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
     * Provides an initialized and rendered instance of the given controller class.
     * <p>
     * If destroy is false, the method will NOT add the controller to the set of initialized controllers and the
     * controller will not be destroyed when a new main controller is set.
     * <p>
     * If destroy is true, the controller will be added to the set of initialized controllers and will be destroyed when
     * a new main controller is set.
     *
     * @param clazz  The controller class
     * @param params The arguments passed to the controller
     * @return The rendered controller
     */
    public @NotNull Parent provide(@NotNull Class<?> clazz, Map<String, Object> params, boolean destroy) {
        if (clazz.isAnnotationPresent(Controller.class))
            throw new IllegalArgumentException("Class '%s' is not a controller.".formatted(clazz.getName()));

        Object instance = this.component.router().getProvidedInstance(clazz);

        // If the controller shall be destroyed, we can just call initAndRender
        if (destroy) return this.component.controllerManager().initAndRender(instance, params);

        // If the controller shall not be destroyed, we have to manually initialize and render it
        this.component.controllerManager().init(instance, params);
        return this.component.controllerManager().render(instance, params);
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
        cleanup();
        System.exit(0);
    }

    /**
     * Initializes and renders a controller.
     *
     * @param route  The route of the controller to render
     * @param params The arguments passed to the controller
     * @return The rendered parent of the controller
     */
    public @NotNull Parent show(@NotNull String route, @NotNull Map<@NotNull String, @Nullable Object> params) {
        cleanup();
        Parent parent = this.component.router().renderRoute(route, params);
        display(parent);
        onShow(route, parent, params);
        return parent;
    }

    // Internal helper method
    private void display(@NotNull Parent parent) {
        stage.getScene().setRoot(parent);
    }

    // Internal helper method
    private void cleanup() {
        this.component.controllerManager().cleanup();
    }

    /**
     * Called when the application shows a new controller.
     * <p>
     * This method is called after the controller is initialized and rendered.
     *
     * @param route    The route of the controller
     * @param rendered The rendered parent of the controller
     * @param params   The arguments passed to the controller
     */
    protected void onShow(String route, Parent rendered, Map<String, Object> params) {
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

}

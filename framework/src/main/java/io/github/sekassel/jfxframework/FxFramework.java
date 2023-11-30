package io.github.sekassel.jfxframework;

import io.github.sekassel.jfxframework.controller.ControllerManager;
import io.github.sekassel.jfxframework.controller.Router;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
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

public class FxFramework extends Application {

    private static final Scheduler FX_SCHEDULER = Schedulers.from(Platform::runLater);
    private static final Logger LOGGER = Logger.getLogger(FxFramework.class.getName());

    private static final Router router = new Router();
    private static final ControllerManager controllerManager = new ControllerManager();

    private Stage stage;

    /**
     * Returns the logger of the framework.
     * @return The logger
     */
    public static Logger logger() {
        return LOGGER;
    }

    /**
     * Returns the scheduler of the framework.
     * @return The scheduler
     */
    public static Scheduler scheduler() {
        return FX_SCHEDULER;
    }

    /**
     * Returns the router of the framework.
     * @return The router
     */
    public static Router router() {
        return router;
    }

    /**
     * Returns the controller manager of the framework.
     * @return The controller manager
     */
    public static ControllerManager manager() {
        return controllerManager;
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
    public static @NotNull Parent provide(@NotNull Class<?> clazz, Map<String, Object> params, boolean destroy) {
        if (clazz.isAnnotationPresent(Controller.class))
            throw new IllegalArgumentException("Class '%s' is not a controller.".formatted(clazz.getName()));

        Object instance = router.getProvidedInstance(clazz);

        // If the controller shall be destroyed, we can just call initAndRender
        if (destroy) return controllerManager.initAndRender(instance, params);

        // If the controller shall not be destroyed, we have to manually initialize and render it
        controllerManager.init(instance, params);
        return controllerManager.render(instance, params);

    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        controllerManager.setMainClass(this.getClass());

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
        Parent parent = router.renderRoute(route, params);
        display(parent);
        return parent;
    }

    // Internal helper method
    private void display(@NotNull Parent parent) {
        stage.getScene().setRoot(parent);
    }

    // Internal helper method
    private void cleanup() {
        controllerManager.cleanup();
    }

    /**
     * Returns the stage of the application.
     *
     * @return The stage of the application
     */
    public Stage stage() {
        return this.stage;
    }

}

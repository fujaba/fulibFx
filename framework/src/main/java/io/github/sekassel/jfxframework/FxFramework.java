package io.github.sekassel.jfxframework;

import io.github.sekassel.jfxframework.controller.ControllerManager;
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

    private final ControllerManager controllerManager = new ControllerManager(this.getClass());

    private Stage stage;

    public static Logger logger() {
        return LOGGER;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

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
        this.controllerManager.init(route, params);
        Parent parent = this.controllerManager.render(route, params);
        show(parent);
        return parent;
    }

    private void show(@NotNull Parent parent) {
        stage.getScene().setRoot(parent);
    }


    private void cleanup() {
        return;
    }

    public Stage getStage() {
        return this.stage;
    }

    public ControllerManager controllerManager() {
        return this.controllerManager;
    }
}

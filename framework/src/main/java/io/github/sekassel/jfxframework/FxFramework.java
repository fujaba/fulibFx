package io.github.sekassel.jfxframework;

import io.github.sekassel.jfxframework.controller.Router;
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

    private Stage stage;

    public static Logger logger() {
        return LOGGER;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;

        router.setMainClass(this.getClass());

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

    private void display(@NotNull Parent parent) {
        stage.getScene().setRoot(parent);
    }


    private void cleanup() {
        return;
    }

    public Stage stage() {
        return this.stage;
    }

    public static Router router() {
        return router;
    }

    public void back() {
        cleanup();
        Parent parent = router.back();
        display(parent);
    }

    public void forward() {
        cleanup();
        Parent parent = router.forward();
        display(parent);
    }
}

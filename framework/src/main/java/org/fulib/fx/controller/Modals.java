package org.fulib.fx.controller;

import org.fulib.fx.FulibFxApp;
import org.fulib.fx.data.TriConsumer;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Modals {

    private Modals() {
        // Prevent instantiation
    }

    /**
     * Shows a component as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * When closing the window, the component will be destroyed.
     * <p>
     * Warning: If the {@link Stage#setOnCloseRequest(EventHandler)} method is overridden,
     * the controller will not be destroyed automatically.
     *
     * @param currentStage the current stage (see {@link FulibFxApp#stage()}
     * @param component    The controller to show
     * @param initializer  the initializer for passing more arguments to the stage and controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Node> void showModal(Stage currentStage, Display component, TriConsumer<Stage, Scene, Display> initializer) {
        showModal(currentStage, component, initializer, Map.of(), true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param currentStage the current stage
     * @param component    the class of the controller to show
     * @param params       the parameters to pass to the controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Node> void showModal(Stage currentStage, Display component, Map<String, Object> params) {
        showModal(currentStage, component, (stage, scene, controller) -> {
        }, params, true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param currentStage the current stage
     * @param component    the class of the controller to show
     * @param <Display>    the type of the controller
     */
    public static <Display extends Node> void showModal(Stage currentStage, Display component) {
        showModal(currentStage, component, (stage, scene, controller) -> {
        }, Map.of(), true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * If destroyOnClose is enabled, when closing (or to be more exact, hiding) the window, the controller will be destroyed.
     * The controller will be destroyed before the stage is hidden.
     *
     * @param currentStage the current stage
     * @param component    the class of the controller to show
     * @param initializer  the initializer for passing more arguments to the stage and controller
     * @param params       the parameters to pass to the controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Node> void showModal(Stage currentStage, Display component, TriConsumer<Stage, Scene, Display> initializer, Map<String, Object> params, boolean destroyOnClose) {
        FulibFxApp.scheduler().scheduleDirect(() -> {
            ModalStage modalStage = new ModalStage(destroyOnClose ? () -> ControllerManager.destroy(component) : null);


            ControllerManager.init(component, params);
            Parent rendered = ControllerManager.render(component, params);

            Scene scene = new Scene(rendered);
            scene.setFill(Paint.valueOf("transparent"));

            modalStage.setScene(scene);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.initOwner(currentStage);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.show();
            modalStage.requestFocus();
            initializer.accept(modalStage, scene, component);
        });
    }

    /**
     * Slightly modified version of {@link Stage} that calls a Runnable when the stage is hidden.
     */
    public static class ModalStage extends Stage {

        private final Runnable onClose;

        public ModalStage(@Nullable Runnable onClose) {
            super();
            this.onClose = onClose;
        }

        public void hide() {
            if (onClose != null)
                onClose.run();
            super.hide();
        }

    }

}

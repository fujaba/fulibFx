package io.github.sekassel.jfxframework.controller;

import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.jfxframework.util.Initializer;
import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Modals {

    private Modals() {
        // Prevent instantiation
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param currentStage    the current stage
     * @param controllerClazz the class of the controller to show
     * @param initializer     the initializer for passing more arguments to the stage and controller
     * @param <T>             the type of the controller
     * @return the modal stage
     */
    public static <T> Stage showModal(Stage currentStage, Class<T> controllerClazz, Initializer<Stage, T> initializer) {
        return showModal(currentStage, controllerClazz, initializer, Map.of(), true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param currentStage    the current stage
     * @param controllerClazz the class of the controller to show
     * @param params          the parameters to pass to the controller
     * @param <T>             the type of the controller
     * @return the modal stage
     */
    public static <T> Stage showModal(Stage currentStage, Class<T> controllerClazz, Map<String, Object> params) {
        return showModal(currentStage, controllerClazz, (stage, controller) -> {
        }, params, true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param currentStage    the current stage
     * @param controllerClazz the class of the controller to show
     * @param <T>             the type of the controller
     * @return the modal stage
     */
    public static <T> Stage showModal(Stage currentStage, Class<T> controllerClazz) {
        return showModal(currentStage, controllerClazz, (stage, controller) -> {
        }, Map.of(), true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * If destroyOnClose is enabled, when closing (or to be more exact, hiding) the window, the controller will be destroyed.
     * The controller will be destroyed before the stage is hidden.
     *
     * @param currentStage    the current stage
     * @param controllerClazz the class of the controller to show
     * @param initializer     the initializer for passing more arguments to the stage and controller
     * @param params          the parameters to pass to the controller
     * @param <T>             the type of the controller
     * @return the modal stage
     */
    public static <T> Stage showModal(Stage currentStage, Class<T> controllerClazz, Initializer<Stage, T> initializer, Map<String, Object> params, boolean destroyOnClose) {
        T controller = FxFramework.framework().router().getProvidedInstance(controllerClazz);
        ModalStage modalStage = new ModalStage(() -> {
            if (destroyOnClose)
                FxFramework.framework().manager().destroy(controller);
        });

        initializer.initialize(modalStage, controller);

        Parent rendered = FxFramework.framework().manager().initAndRender(controller, params);

        Scene scene = new Scene(rendered);
        scene.setFill(Paint.valueOf("transparent"));

        modalStage.setScene(scene);
        modalStage.initOwner(currentStage);
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.show();
        modalStage.requestFocus();
        return modalStage;
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

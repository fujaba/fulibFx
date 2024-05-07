package org.fulib.fx.controller;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.fulib.fx.FulibFxApp;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.fulib.fx.util.FrameworkUtil.error;

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
     * @param app          the app instance
     * @param currentStage the current stage (see {@link FulibFxApp#stage()}
     * @param component    The controller to show
     * @param initializer  the initializer for passing more arguments to the stage and controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Stage currentStage, Display component, BiConsumer<Stage, Display> initializer) {
        showModal(app, currentStage, component, initializer, Map.of(), true);
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
     * @param app         the app instance
     * @param component   The controller to show
     * @param initializer the initializer for passing more arguments to the stage and controller
     * @param <Display>   the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Display component, BiConsumer<Stage, Display> initializer) {
        showModal(app, app.stage(), component, initializer, Map.of(), true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param app          the app instance
     * @param currentStage the current stage
     * @param component    the class of the controller to show
     * @param params       the parameters to pass to the controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Stage currentStage, Display component, Map<String, Object> params) {
        showModal(app, currentStage, component, (stage, controller) -> {
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
     * @param app          the app instance
     * @param component    the class of the controller to show
     * @param params       the parameters to pass to the controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Display component, Map<String, Object> params) {
        showModal(app, app.stage(), component, (stage, controller) -> {
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
     * @param app          the app instance
     * @param currentStage the current stage
     * @param component    the class of the controller to show
     * @param <Display>    the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Stage currentStage, Display component) {
        showModal(app, currentStage, component, (stage, controller) -> {
        }, Map.of(), true);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * When closing the window, the controller will be destroyed. If the {@link Stage#setOnCloseRequest(EventHandler)}
     * method is overridden, the controller will not be destroyed automatically.
     *
     * @param app       the app instance
     * @param component the class of the controller to show
     * @param <Display> the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Display component) {
        showModal(app, app.stage(), component, (stage, controller) -> {
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
     * @param app            the app instance
     * @param component      the class of the controller to show
     * @param initializer    the initializer for passing more arguments to the stage and controller
     * @param params         the parameters to pass to the controller
     * @param destroyOnClose if the controller should be destroyed when the window is closed
     * @param <Display>      the type of the controller
     */
    public static <Display extends Parent> void showModal(FulibFxApp app, Display component, BiConsumer<Stage, Display> initializer, Map<String, Object> params, boolean destroyOnClose) {
        showModal(app, app.stage(), component, initializer, params, destroyOnClose);
    }

    /**
     * Shows a controller as a modal window.
     * <p>
     * <b>Important:</b> The component shouldn't be initialized/rendered before calling this method.
     * <p>
     * If destroyOnClose is enabled, when closing (or to be more exact, hiding) the window, the controller will be destroyed.
     * The controller will be destroyed before the stage is hidden.
     *
     * @param app          the app instance
     * @param currentStage the current stage
     * @param component    the class of the controller to show
     * @param initializer  the initializer for passing more arguments to the stage and controller
     * @param params       the parameters to pass to the controller
     * @param <Display>    the type of the controller
     */
    public static <Display extends Node> void showModal(FulibFxApp app, Stage currentStage, Display component, BiConsumer<Stage, Display> initializer, Map<String, Object> params, boolean destroyOnClose) {
        FulibFxApp.FX_SCHEDULER.scheduleDirect(() -> {
            ModalStage modalStage = new ModalStage(app, destroyOnClose, component);

            // Add additional default parameters
            Map<String, Object> parameters = new HashMap<>(params);
            parameters.putIfAbsent("modalStage", modalStage);
            parameters.putIfAbsent("ownerStage", currentStage);

            // Initialize and render the component
            app.frameworkComponent().controllerManager().init(component, parameters);
            Node rendered = app.frameworkComponent().controllerManager().render(component, parameters);

            // As the displayed component will be the root of a stage, it has to be a parent
            if (!(rendered instanceof Parent parent)) {
                throw new IllegalArgumentException(error(1011).formatted(component.getClass().getName()));
            }

            // Set the title if present
            app.getTitle(component).ifPresent(title -> modalStage.setTitle(app.formatTitle(title)));

            // Configure scene to look like a popup (can be changed using the initializer)
            Scene scene = new Scene(parent);
            scene.setFill(Paint.valueOf("transparent"));
            modalStage.setScene(scene);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.initOwner(currentStage);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.show();
            modalStage.requestFocus();
            initializer.accept(modalStage, component);
        });
    }

    /**
     * Slightly modified version of {@link Stage} that destroys the controller when the stage is hidden.
     */
    public static class ModalStage extends Stage {

        private final FulibFxApp app;
        private final Object component;
        private final boolean destroyOnClose;

        public ModalStage(FulibFxApp app, boolean destroyOnClose, @NotNull Object component) {
            super();
            this.destroyOnClose = destroyOnClose;
            this.app = app;
            this.component = component;
        }

        @Override
        public void hide() {
            if (destroyOnClose) app.frameworkComponent().controllerManager().destroy(component);
            super.hide();
        }

    }

}

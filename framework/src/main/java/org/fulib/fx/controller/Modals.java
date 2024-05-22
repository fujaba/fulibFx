package org.fulib.fx.controller;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.fulib.fx.FulibFxApp;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Helper class for creating modal windows.
 *
 * @deprecated Use {@link org.fulib.fx.constructs.Modals} instead.
 */
@Deprecated(forRemoval = true)
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
     * @param app       the app instance
     * @param component the class of the controller to show
     * @param params    the parameters to pass to the controller
     * @param <Display> the type of the controller
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
    public static <Display extends Parent> void showModal(FulibFxApp app, Stage currentStage, Display component, BiConsumer<Stage, Display> initializer, Map<String, Object> params, boolean destroyOnClose) {
        org.fulib.fx.constructs.Modals modals = new org.fulib.fx.constructs.Modals(app);
        modals.modal(component)
                .owner(currentStage)
                .dialog(true)
                .init(initializer)
                .params(params)
                .destroyOnClose(destroyOnClose)
                .show();
    }

}

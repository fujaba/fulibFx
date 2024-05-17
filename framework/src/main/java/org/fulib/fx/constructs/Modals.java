package org.fulib.fx.constructs;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.fulib.fx.FulibFxApp;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.fulib.fx.util.FrameworkUtil.error;

public class Modals {

    FulibFxApp app;

    @Inject
    public Modals(FulibFxApp app) {
        this.app = app;
    }

    /**
     * Creates a new modal instance for building modals.
     *
     * @param component The component instance to display
     * @param <T>       The type of component
     * @return A modal instance
     */
    public <T extends Node> ModalBuilder<T> modal(T component) {
        return new ModalBuilder<>(app, component);
    }


    /**
     * Builder class for displaying modals.
     *
     * @param <T> The type of the component
     */
    public static class ModalBuilder<T extends Node> {

        /**
         * Initializes the stage with some default options like transparency and modality
         */
        private final BiConsumer<Stage, T> FULIBFX_DIALOG = ((modalStage, component) -> {
            modalStage.getScene().setFill(Paint.valueOf("transparent"));
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.setAlwaysOnTop(true);
        });

        private final T component;
        private final FulibFxApp app;

        private BiConsumer<Stage, T> initializer;
        private Stage owner;
        private Map<String, Object> params;
        private boolean destroyOnClose = true;
        private boolean dialog = false;

        public ModalBuilder(FulibFxApp app, T component) {
            this.app = app;
            this.component = component;
            this.owner = app.stage();
        }

        /**
         * Adds an initializer.
         * <p>
         * If another initializer has been added already, the new initializer will be called after the previous one.
         *
         * @param initializer The initializer to add
         * @return The current modal instance
         */
        public ModalBuilder<T> init(BiConsumer<Stage, T> initializer) {
            if (this.initializer == null) {
                this.initializer = initializer;
                return this;
            }
            this.initializer = this.initializer.andThen(initializer);
            return this;
        }

        /**
         * Sets the owner stage.
         * <p>
         * If the owner stage is closed, the modal will be closed as well.
         * <p>
         * If no owner is set, the {@link FulibFxApp#stage()} will be used.
         *
         * @param owner The owner stage.
         * @return The current modal instance
         */
        public ModalBuilder<T> owner(Stage owner) {
            this.owner = owner;
            return this;
        }

        /**
         * Adds a pre-made initializer with some default options regarding transparency and modality.
         * <p>
         * This will set the stage style to {@link StageStyle#TRANSPARENT}, the modality to {@link Modality#WINDOW_MODAL}
         * and the scene fill to transparent (see {@link ModalBuilder#FULIBFX_DIALOG}).
         *
         * @param dialog Whether the default dialog options should be used
         * @return The current modal instance
         */
        public ModalBuilder<T> dialog(boolean dialog) {
            this.dialog = dialog;
            return this;
        }

        /**
         * Sets the parameters to be used when initializing/rendering the component.
         * <p>
         * The default parameters "modalStage" and "ownerStage" will be added automatically if they are not present already.
         * The modal stage is the current modal stage and the owner stage is the stage that opened the modal (see {@link ModalBuilder#owner(Stage)}).
         *
         * @param params The parameter map
         * @return The current modal instance
         */
        public ModalBuilder<T> params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public ModalBuilder<T> destroyOnClose(boolean destroyOnClose) {
            this.destroyOnClose = destroyOnClose;
            return this;
        }

        /**
         * Displays the current modal.
         * This can only be called once per modal builder.
         */
        public ModalStage show() {

            if (component.getScene() != null) {
                throw new RuntimeException(error(1014));
            }

            ModalStage modalStage = new ModalStage(app, destroyOnClose, component);

            // Add additional default parameters
            Map<String, Object> parameters = params == null ? new HashMap<>() : new HashMap<>(params);
            parameters.putIfAbsent("modalStage", modalStage);
            parameters.putIfAbsent("ownerStage", owner);

            // Initialize and render the component
            app.frameworkComponent().controllerManager().init(component, parameters);
            Node rendered = app.frameworkComponent().controllerManager().render(component, parameters);

            // As the displayed component will be the root of a stage, it has to be a parent
            if (!(rendered instanceof Parent parent)) {
                throw new IllegalArgumentException(error(1011).formatted(component.getClass().getName()));
            }

            // Set the title if present
            app.applyTitle(component, modalStage);

            // Setup the stage and scene
            Scene scene = new Scene(parent);
            modalStage.setScene(scene);
            modalStage.initOwner(owner);
            if (dialog) {
                FULIBFX_DIALOG.accept(modalStage, component);
            }
            if (initializer != null) {
                initializer.accept(modalStage, component);
            }
            modalStage.show();
            modalStage.requestFocus();
            return modalStage;
        }
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

    /**
     * Returns a list of all visible modal stages
     *
     * @return A list of all visible modal stages
     */
    public static List<ModalStage> getModalStages() {
        return Window.getWindows()
            .stream()
            .filter(window -> window instanceof ModalStage)
            .map(window -> (ModalStage) window)
            .toList();
    }

}
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

    @Inject
    FulibFxApp app;

    @Inject
    public Modals() {
    }

    public Modals(FulibFxApp app) {
        this.app = app;
    }

    public <T extends Node> Modal<T> modal(T component) {
        return new Modal<>(app, component);
    }


    public static class Modal<T extends Node> {

        private final BiConsumer<Stage, T> FULIBFX_DIALOG = ((modalStage, component) -> {
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.setAlwaysOnTop(true);
        });

        private final T component;
        private final FulibFxApp app;

        private BiConsumer<Stage, T> initializer = (stage, component) -> {
        };
        private Stage owner;
        private Map<String, Object> params = new HashMap<>();
        private boolean destroyOnClose = true;

        public Modal(FulibFxApp app, T component) {
            this.app = app;
            this.component = component;
            this.owner = app.stage();
        }

        public Modal<T> init(BiConsumer<Stage, T> initializer) {
            this.initializer = initializer;
            return this;
        }

        public Modal<T> owner(Stage owner) {
            this.owner = owner;
            return this;
        }

        public Modal<T> initDialog(BiConsumer<Stage, T> initializer) {
            this.initializer = FULIBFX_DIALOG.andThen(initializer);
            return this;
        }

        public Modal<T> params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public Modal<T> destroyOnClose(boolean destroyOnClose) {
            this.destroyOnClose = destroyOnClose;
            return this;
        }

        public void show() {
            FulibFxApp.FX_SCHEDULER.scheduleDirect(() -> {
                ModalStage modalStage = new ModalStage(app, destroyOnClose, component);

                // Add additional default parameters
                Map<String, Object> parameters = new HashMap<>(params);
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

                // Configure scene to look like a popup (can be changed using the initializer)
                Scene scene = new Scene(parent);
                scene.setFill(Paint.valueOf("transparent"));
                modalStage.setScene(scene);
                modalStage.initOwner(owner);
                initializer.accept(modalStage, component);
                modalStage.show();
                modalStage.requestFocus();
            });
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
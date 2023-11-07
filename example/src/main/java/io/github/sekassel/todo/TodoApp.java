package io.github.sekassel.todo;

import io.github.sekassel.todo.dagger.DaggerMainComponent;
import io.github.sekassel.jfxframework.FxFramework;
import javafx.stage.Stage;
import io.github.sekassel.todo.dagger.MainComponent;

import java.util.Map;

public class TodoApp extends FxFramework {

    public static TodoApp instance;

    private final MainComponent component;

    public TodoApp() {
        super();
        this.component = DaggerMainComponent.builder().mainApp(this).build();
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            super.start(primaryStage);
            instance = this;
            controllerManager().registerRoutes(component.router());
            show("", Map.of());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    public MainComponent component() {
        return component;
    }
}

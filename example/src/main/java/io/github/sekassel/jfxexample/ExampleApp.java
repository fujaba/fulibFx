package io.github.sekassel.jfxexample;

import io.github.sekassel.jfxexample.dagger.MainComponent;
import io.github.sekassel.jfxexample.dagger.DaggerMainComponent;
import io.github.sekassel.jfxframework.FxFramework;
import javafx.stage.Stage;

import java.util.Map;

public class ExampleApp extends FxFramework {

    public static ExampleApp instance;

    private final MainComponent component;

    public ExampleApp() {
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

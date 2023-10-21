package io.github.sekassel.jfxexample;

import io.github.sekassel.jfxframework.FxFramework;
import javafx.stage.Stage;

import java.util.Map;

public class ExampleApp extends FxFramework {

    public static ExampleApp instance;

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);
        instance = this;
        controllerManager().register("io.github.sekassel.jfxexample.controller", "io.github.sekassel.jfxexample.controller2");
        show("/menu/login", Map.of());
    }

    @Override
    public void stop() {
        super.stop();
    }

}

package io.github.sekassel.person;

import io.github.sekassel.person.dagger.DaggerMainComponent;
import io.github.sekassel.jfxframework.FxFramework;
import io.github.sekassel.person.dagger.MainComponent;
import javafx.stage.Stage;

import java.util.Map;

public class PersonApp extends FxFramework {

    public static PersonApp instance;

    private final MainComponent component;

    public PersonApp() {
        super();
        this.component = DaggerMainComponent.builder().mainApp(this).build();
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            super.start(primaryStage);
            instance = this;
            router().registerRoutes(component.router());
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

package de.uniks.example;

import de.uniks.example.dagger.DaggerMainComponent;
import de.uniks.example.dagger.MainComponent;
import javafx.stage.Stage;
import org.fulib.fx.FulibFxApp;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.logging.Level;

@Singleton
public class ExampleApp extends FulibFxApp {

    private final MainComponent component;

    public ExampleApp() {
        super();

        this.component = DaggerMainComponent.builder().mainApp(this).build();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            super.start(primaryStage);
            registerRoutes(component.routes());
            setTitlePattern("Example - %s");
            setResourcesPath(Path.of("example/src/main/resources/"));
            autoRefresher().setup(Path.of("example/src/main/resources/de/uniks/example"));
            setDefaultResourceBundle(component.bundle());
            show("");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An error occurred while starting the application: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

}

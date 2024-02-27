package de.uniks.ludo;

import de.uniks.ludo.dagger.DaggerMainComponent;
import de.uniks.ludo.dagger.MainComponent;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.fulib.fx.FulibFxApp;

import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.logging.Level;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

@Singleton
public class App extends FulibFxApp {


    private MainComponent component;

    public App() {
        super();

        // The dagger component is created here, which is used to inject the controllers and other components
        this.component = DaggerMainComponent.builder().mainApp(this).build();
    }

    @Override
    public void start(Stage primaryStage) {
        try {

            // Starting the framework, initializes all the necessary components
            super.start(primaryStage);

            // Registering the routes of the application. See Routing.java for more information.
            registerRoutes(component.routes());

            // Setting the default resource bundle of the application to the resource bundle provided by the component
            setDefaultResourceBundle(component.bundle());

            // Adding a key event handler to the stage, which listens for the F5 key to refresh the application
            stage().addEventHandler(KEY_RELEASED, event -> {
                if (event.getCode() == KeyCode.F5) {
                    refresh();
                }
            });

            // Setting the title pattern of the application. The %s will be replaced with the title of the view
            setTitlePattern("Ludo - %s");

            // Setting the resource path to the resources folder of the project (required for reloading in dev)
            // If the resource path is not set, the framework will use the default resource path (src/main/resources)
            setResourcesPath(Path.of("ludo/src/main/resources/"));

            // Setting the path which the auto refresher should watch (required for auto-reloading in dev)
            autoRefresher().setup(Path.of("ludo/src/main/resources/de/uniks/ludo"));

            // Setting the default resource bundle of the application to the resource bundle provided by the component
            setDefaultResourceBundle(component.bundle());

            // Starting the application by showing the main view without any parameters
            show("");

        } catch (Exception e) {
            // If an error occurs while starting the application, we want to log it and exit the application
            LOGGER.log(Level.SEVERE, "An error occurred while starting the application: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * Returns the dagger component of the application.
     *
     * @return The dagger component
     */
    public MainComponent component() {
        return component;
    }

    /**
     * Overrides the dagger component of the application.
     *
     * @param component The new dagger component
     * @return The application itself
     */
    public App setComponent(MainComponent component) {
        this.component = component;
        return this;
    }

}

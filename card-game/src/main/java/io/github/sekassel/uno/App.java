package io.github.sekassel.uno;

import org.fulib.fx.FxFramework;
import io.github.sekassel.uno.controller.Titleable;
import io.github.sekassel.uno.dagger.DaggerMainComponent;
import io.github.sekassel.uno.dagger.MainComponent;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

import static javafx.scene.input.KeyEvent.KEY_PRESSED;

/**
 * The main class of your application.
 */
public class App extends FxFramework {


    /**
     * The dagger component of the application.
     */
    private final MainComponent component;

    /**
     * The constructor of the application.
     */
    public App() {
        super();

        // Create a new dagger component as a starting point for the dependency injection
        this.component = DaggerMainComponent.builder().mainApp(this).build();
    }

    /**
     * The start method of the application.
     * This method is called by the {@link Main} class when the application is started.
     *
     * @param primaryStage The primary stage of the application
     */
    @Override
    public void start(Stage primaryStage) {
        try {

            // Starting the framework, initializes all the necessary components
            super.start(primaryStage);

            // Registering the routes of the application. See UnoRouting.java for more information.
            registerRoutes(component.routes());

            stage().addEventHandler(KEY_PRESSED, event -> {
                if (event.getCode().toString().equals("F5")) {
                    this.refresh();
                }
            });

            // Setting the resource path to the resources folder of the project (required for reloading in dev)
            // If the resource path is not set, the framework will use the default resource path (src/main/resources)
            setResourcesPath(Path.of("card-game/src/main/resources/"));

            // Setting the path which the auto refresher should watch (required for auto-reloading in dev)
            autoRefresher().setup(Path.of("card-game/src/main/resources/io/github/sekassel/uno/view"));
            show("", Map.of());

        } catch (Exception e) {
            // If an error occurs while starting the application, we want to log it and exit the application
            logger().log(Level.SEVERE, "An error occurred while starting the application: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onShow(String route, Object controller, Parent rendered, Map<String, Object> params) {
        // This method is called whenever a new controller is shown.
        // We use this method to set the title of the window to the title of the controller, if the controller implements
        // the Titleable interface. This is a good example of how you can use the onShow method to do some additional operations.
        if (controller instanceof Titleable titleable) {
            this.stage().setTitle(titleable.getTitle());
        }
    }

    /**
     * The stop method of the application. This method will be called when the application stops, for example if the
     * window is closed.
     */
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
}

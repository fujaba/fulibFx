package org.fulib.fx.mocking;

import javafx.stage.Stage;
import org.fulib.fx.FulibFxApp;

import java.util.logging.Level;

public class MyApp extends FulibFxApp {

    @Override
    public void start(Stage primaryStage) {
        try {
            super.start(primaryStage);
        } catch (Exception e) {
            logger().log(Level.SEVERE, "Error while starting the application", e);
        }
    }

}

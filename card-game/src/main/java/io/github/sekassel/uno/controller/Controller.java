package io.github.sekassel.uno.controller;

import io.github.sekassel.uno.Main;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public interface Controller {

    String getTitle();

    void init();

    Parent render() throws IOException;

    void destroy();

    /**
     * Loads a Parent object for a controller from a FXML file path
     * Can be overwritten by classes to allow for custom loading behaviour
     *
     * @param path The path of the file
     * @param controller The controller to be used with the screen
     * @return The Parent built from the file
     */
    default Parent loadControllerScreen(Controller controller, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource(path));
            loader.setControllerFactory(c -> controller);
            return loader.load();
        } catch (Exception e) {
            System.err.println("Couldn't load file '" + path + "'.");
            e.printStackTrace();
            return null;
        }
    }
}
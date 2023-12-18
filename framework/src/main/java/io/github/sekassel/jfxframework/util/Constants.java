package io.github.sekassel.jfxframework.util;

/**
 * Class containing constants used by the framework.
 */
public class Constants {

    // The path to the fxml files relative to the base class in the resources folder
    public static final String FXML_PATH = "view/";

    // The string to look for in fxml files to find the controller class
    public static final String FX_CONTROLLER_STRING = "fx:controller=\"%s\"";

    // Environment variable for telling the framework that it's running in development mode
    public static final String INDEV_ENVIRONMENT_VARIABLE = "INDEV";

    private Constants() {
        // Prevent instantiation
    }
}

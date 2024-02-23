package org.fulib.fx.util;

import org.fulib.fx.FulibFxApp;

public class FrameworkUtil {

    private FrameworkUtil() {
        // Prevent instantiation
    }

    // Environment variable for telling the framework that it's running in development mode
    private static final String INDEV_ENVIRONMENT_VARIABLE = "INDEV";

    /**
     * Checks if the framework is running in development mode. This is the case if the INDEV environment variable is set to true.
     * <p>
     * Since people are dumb and might not set the variable correctly, it also checks if the intellij launcher is used.
     *
     * @return True if the framework is running in development mode
     */
    public static boolean runningInDev() {
        return System.getenv().getOrDefault(INDEV_ENVIRONMENT_VARIABLE, "false").equalsIgnoreCase("true");
    }

    public static String error(int id) {
        return FulibFxApp.ERROR_BUNDLE.getString(String.valueOf(id));
    }
}

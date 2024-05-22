package org.fulib.fx.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class FrameworkUtil {

    private static final ResourceBundle ERROR_BUNDLE = ResourceBundle.getBundle("org.fulib.fx.lang.error", Locale.ROOT);

    private FrameworkUtil() {
        // Prevent instantiation
    }

    public static String error(int id) {
        return ERROR_BUNDLE.getString(String.valueOf(id)) + " [FFX" + id + "]";
    }

    public static String note(int id) {
        return ERROR_BUNDLE.getString(id + ".note");
    }

}

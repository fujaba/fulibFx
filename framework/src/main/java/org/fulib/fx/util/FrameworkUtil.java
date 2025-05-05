package org.fulib.fx.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Different miscellaneous utilities for the framework.
 * Mostly internal, use with care.
 */
@ApiStatus.Internal
public class FrameworkUtil {

    private static final ResourceBundle ERROR_BUNDLE = ResourceBundle.getBundle("org.fulib.fx.lang.error", Locale.ROOT);

    private FrameworkUtil() {
        // Prevent instantiation
    }

    public static String error(int id) {
        return "FFX" + id + ": " + ERROR_BUNDLE.getString(String.valueOf(id));
    }

    public static String note(int id) {
        return ERROR_BUNDLE.getString(id + ".note");
    }

}

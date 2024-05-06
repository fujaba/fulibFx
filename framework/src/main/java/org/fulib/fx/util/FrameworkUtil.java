package org.fulib.fx.util;

import javafx.util.Pair;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiPredicate;

public class FrameworkUtil {

    private static final ResourceBundle ERROR_BUNDLE = ResourceBundle.getBundle("org.fulib.fx.lang.error");

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
        return ERROR_BUNDLE.getString(String.valueOf(id)) + " [FFX" + id + "]";
    }

    public static String note(int id) {
        return ERROR_BUNDLE.getString(id + ".note");
    }


    /**
     * Finds all duplicates in a list based on a custom predicate.
     *
     * @param list      The list to check
     * @param predicate The predicate to use
     * @param <T>       The predicate to use
     * @return A pair of the first found duplicates
     */
    public static <T> Optional<Pair<T, T>> findDuplicate(List<T> list, BiPredicate<T, T> predicate) {
        for (T element1 : list) {
            for (T element2 : list) {
                if (predicate.test(element1, element2)) {
                    return Optional.of(new Pair<>(element1, element2));
                }
            }
        }
        return Optional.empty();
    }
}

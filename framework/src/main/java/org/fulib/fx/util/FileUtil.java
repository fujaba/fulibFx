package org.fulib.fx.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilities for managing files.
 * Mostly internal, use with care.
 */
public class FileUtil {

    private FileUtil() {
        // Prevent instantiation
    }

    /**
     * Returns the content of the given file as a string.
     *
     * @param file The file to read
     * @return The content of the given file as a string or an empty string if the file couldn't be read
     */
    public static @NotNull String getContent(@NotNull File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Returns the file representation of the given resource in the resources folder of the given class.
     *
     * @param basePath The base path to the resources folder
     * @param clazz    The class to get the resource from
     * @param resource The resource to read
     * @return The file of the given resource
     */
    public static @NotNull File getResourceAsLocalFile(Path basePath, Class<?> clazz, String resource) {
        String classPath = clazz.getPackageName().replace(".", "/");
        return basePath.resolve(classPath).resolve(resource).toFile();
    }

}

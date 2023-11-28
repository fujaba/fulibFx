package io.github.sekassel.jfxframework.duplicate;

/**
 * Interface for duplicating objects.
 *
 * @param <T> The type of the object to duplicate
 */
public interface Duplicator<T> {

    /**
     * Duplicates the given object.
     *
     * @param object The object to duplicate
     * @return The duplicated object
     */
    T duplicate(T object);

}

package org.fulib.fx.constructs;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for components that can be reused inside other constructs.
 * If a component implements this interface, it can be reused when its item changes instead of being destroyed and recreated.
 * <p>
 * The component is fully responsible for updating its display when the item changes.
 *
 * @param <T> The type of the item that the component can display
 */
public interface ReusableItemComponent<T> {
    void setItem(@NotNull T item);
}

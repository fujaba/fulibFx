package org.fulib.fx.constructs.listview;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for components that can be reused in a list view.
 * If a component implements this interface, it can be reused when its item changes instead of being destroyed and recreated.
 *
 * @param <T> The type of the item that the component can display
 */
public interface ReusableItemComponent<T> {
    void setItem(@NotNull T item);
}

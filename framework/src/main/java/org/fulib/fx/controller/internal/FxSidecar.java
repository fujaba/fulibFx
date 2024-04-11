package org.fulib.fx.controller.internal;

import javafx.scene.Node;
import org.fulib.fx.annotation.controller.Resource;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.ResourceBundle;

public interface FxSidecar<T> {
    void init(T instance, Map<String, Object> params);

    Node render(T instance, Map<String, Object> params);

    /**
     * Hook for precompiled FXML files.
     * @param controller The controller
     * @param root The root node of the FXML file, if using fx:root. Can be null otherwise.
     * @return The root node of the FXML file, or null if the FXML file is not precompiled
     */
    default @Nullable Node renderFxml(T controller, Node root) {
        return null;
    }

    void destroy(T instance);

    /**
     * Returns the resource bundle of the given instance if it has one.
     * If no resource bundle is set, the default resource bundle will be used.
     * If no default resource bundle is set, null will be returned.
     *
     * @param instance The instance to get the resource bundle from
     * @return The resource bundle of the given instance if it has one or the default resource bundle
     * @throws RuntimeException If the instance has more than one field annotated with {@link Resource}
     */
    @Nullable ResourceBundle getResources(T instance);

    /**
     * Returns the title of the given controller instance if it has one.
     * If the title is a key, the title will be looked up in the resource bundle of the controller.
     *
     * @param instance The controller instance
     * @return The title of the controller
     */
    @Nullable String getTitle(T instance);
}

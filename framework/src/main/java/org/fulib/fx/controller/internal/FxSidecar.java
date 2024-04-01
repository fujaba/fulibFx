package org.fulib.fx.controller.internal;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.ResourceBundle;

public interface FxSidecar<T> {
    void init(T instance, Map<String, Object> params);

    Node render(T instance, Map<String, Object> params);

    void destroy(T instance);

    @NotNull ResourceBundle getResources(T instance);

    @Nullable String getTitle(T instance);
}

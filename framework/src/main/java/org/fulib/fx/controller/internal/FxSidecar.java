package org.fulib.fx.controller.internal;

import javafx.scene.Node;

import java.util.Map;

public interface FxSidecar<T> {
    void init(T instance, Map<String, Object> params);

    Node render(T instance, Map<String, Object> params);

    void destroy(T instance);
}

package org.fulib.fx.controller.internal;

import java.util.Map;

public interface FxSidecar<T> {
    void init(T instance, Map<String, Object> params);
}

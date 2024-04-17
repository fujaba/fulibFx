package org.fulib.fx.util;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import org.fulib.fx.annotation.event.OnKey;

public record KeyEventHolder(
        OnKey.Target target,
        EventType<KeyEvent> type,
        EventHandler<KeyEvent> handler
) {
}

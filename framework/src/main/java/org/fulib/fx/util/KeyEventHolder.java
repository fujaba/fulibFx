package org.fulib.fx.util;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import org.fulib.fx.annotation.event.onKey;

public record KeyEventHolder(
        onKey.Target target,
        EventType<KeyEvent> type,
        EventHandler<KeyEvent> handler
) {
}

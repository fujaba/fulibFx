package org.fulib.fx.util;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import org.fulib.fx.annotation.event.onKey;

public record KeyEventHolder(
        onKey.Target target,
        javafx.event.EventType<KeyEvent> type,
        EventHandler<KeyEvent> handler
) {
}

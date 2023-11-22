package io.github.sekassel.jfxframework.duplicate.duplicators;

import javafx.scene.control.Control;

public abstract class ControlDuplicator<T extends Control> extends ParentDuplicator<T> {

        @Override
        public T duplicate(T control) {
            T duplicate = super.duplicate(control);

            duplicate.setContextMenu(control.getContextMenu());
            duplicate.setSkin(control.getSkin()); // Skinnable
            duplicate.setTooltip(control.getTooltip());

            return duplicate;
        }
}

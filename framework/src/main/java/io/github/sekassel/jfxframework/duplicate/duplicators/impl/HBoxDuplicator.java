package io.github.sekassel.jfxframework.duplicate.duplicators.impl;

import javafx.scene.layout.HBox;

public class HBoxDuplicator<T extends HBox> extends PaneDuplicator<T> {

    @Override
    public T duplicate(T hBox) {
        T newHBox = super.duplicate(hBox);

        newHBox.setSpacing(hBox.getSpacing());

        return newHBox;
    }


}

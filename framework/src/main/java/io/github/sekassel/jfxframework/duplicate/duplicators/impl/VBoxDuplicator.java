package io.github.sekassel.jfxframework.duplicate.duplicators.impl;

import javafx.scene.layout.VBox;

public class VBoxDuplicator<T extends VBox> extends PaneDuplicator<T> {

    @Override
    public T duplicate(T vBox) {
        T newVBox = super.duplicate(vBox);

        newVBox.setSpacing(vBox.getSpacing());

        return newVBox;
    }


}

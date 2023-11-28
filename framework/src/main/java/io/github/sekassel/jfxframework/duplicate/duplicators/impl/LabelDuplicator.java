package io.github.sekassel.jfxframework.duplicate.duplicators.impl;

import io.github.sekassel.jfxframework.duplicate.duplicators.LabeledDuplicator;
import javafx.scene.control.Label;

public class LabelDuplicator<T extends Label> extends LabeledDuplicator<T> {

    @Override
    public T duplicate(T label) {
        T newLabel = super.duplicate(label);

        newLabel.setLabelFor(label.getLabelFor());

        return newLabel;
    }


}

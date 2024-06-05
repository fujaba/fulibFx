package org.fulib.fx.duplicate.duplicators.impl;

import javafx.scene.control.Button;
import org.fulib.fx.duplicate.duplicators.LabeledDuplicator;

public class ButtonDuplicator<T extends Button> extends LabeledDuplicator<T> {

    @Override
    public T duplicate(T button) {
        T newButton = super.duplicate(button);

        newButton.setOnAction(button.getOnAction()); // ButtonBase
        newButton.setDefaultButton(button.isDefaultButton());
        newButton.setCancelButton(button.isCancelButton());

        return newButton;
    }

}

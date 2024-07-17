package org.fulib.fx.app.controller.subcomponent.basic;

import org.fulib.fx.annotation.controller.Component;
import javafx.scene.control.Button;
import org.fulib.fx.constructs.ReusableItemComponent;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

@Component
public class ButtonSubComponent extends Button implements ReusableItemComponent<String> {

    @Inject
    public ButtonSubComponent() {
        this.setText("Sub Component Button");
    }

    @Override
    public void setItem(@NotNull String item) {
        this.setText(item);
    }
}

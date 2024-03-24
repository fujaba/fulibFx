package org.fulib.fx.app.controller.subcomponent.basic;

import org.fulib.fx.annotation.controller.Component;
import javafx.scene.control.Button;

import javax.inject.Inject;

@Component
public class ButtonSubComponent extends Button {

    @Inject
    public ButtonSubComponent() {
        this.setText("Sub Component Button");
    }

}

package org.fulib.fx.app.controllertypes.sub;

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

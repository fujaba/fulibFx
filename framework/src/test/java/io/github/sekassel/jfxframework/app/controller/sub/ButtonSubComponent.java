package io.github.sekassel.jfxframework.app.controller.sub;

import io.github.sekassel.jfxframework.annotation.controller.Component;
import javafx.scene.control.Button;

import javax.inject.Inject;

@Component
public class ButtonSubComponent extends Button {

    @Inject
    public ButtonSubComponent() {
        this.setText("Sub Component Button");
    }

}

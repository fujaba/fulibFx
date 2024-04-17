package org.fulib.fx.app.controller.history;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;

@Controller(view = "#render")
public class CController {

    Label label = new Label();

    public CController() {
    }

    public VBox render() {
        return new VBox(label);
    }

    @OnRender
    public void onRender(@Param("string") String string) {
        label.setText("C:" + string);
    }
}

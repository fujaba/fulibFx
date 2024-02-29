package org.fulib.fx.app.history;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;

@Controller(view = "#render")
public class AController {

    Label label = new Label();

    public AController() {
    }

    public VBox render() {
        return new VBox(label);
    }

    @onRender
    public void onRender(@Param("string") String string) {
        label.setText("A:" + string);
    }
}

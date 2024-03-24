package org.fulib.fx.app.controller;

import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Title;

@Title("Title")
@Controller(view = "#view")
public class TitleController {

    public VBox view() {
        return new VBox();
    }

}

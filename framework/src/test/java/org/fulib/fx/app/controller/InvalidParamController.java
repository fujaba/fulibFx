package org.fulib.fx.app.controller;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.VBox;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.param.Param;

@Controller(view = "#render")
public class InvalidParamController {

    @Param("one")
    Integer integer;

    @Param("two")
    IntegerProperty integerProperty = new SimpleIntegerProperty();

    @Param("three")
    IntegerProperty integerProperty2;

    public VBox render() {
        return new VBox();
    }


}

package io.github.sekassel.jfxexample.controller2;

import io.github.sekassel.jfxframework.controller.Controller;
import io.github.sekassel.jfxframework.controller.ControllerEvent;
import io.github.sekassel.jfxframework.controller.Param;
import javafx.scene.control.Label;

@Controller(id = "/menu/main")
public class MainController {

    public Label welcomeLabel;

    @ControllerEvent.onInit()
    public void init(@Param(name = "username") String user) {
        System.out.println("MainController.onInit() " + user);
    }

    @ControllerEvent.onRender()
    public void setText(@Param(name = "username") String username, @Param(name = "password") String password) {
        welcomeLabel.setText("Welcome " + username + ", your password is " + password);
    }
}

package de.uniks.ludo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Resource;
import org.fulib.fx.annotation.controller.Title;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;

import javax.inject.Inject;
import java.util.ResourceBundle;

@Title("%gameover.title")
@Controller
public class GameOverController {

    @FXML
    private Label playerWonLabel;

    @Resource
    @Inject
    ResourceBundle bundle;

    @Inject
    public GameOverController() {
    }

    @onRender
    public void onRender(@Param("winner") int winner) {
        playerWonLabel.setText(playerWonLabel.getText().formatted(winner));
    }

}

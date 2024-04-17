package de.uniks.ludo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Title;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;

import javax.inject.Inject;

@Title("%gameover.title")
@Controller
public class GameOverController extends BaseController {

    @FXML
    private Label playerWonLabel;

    @Inject
    public GameOverController() {
    }

    @OnRender
    public void onRender(@Param("winner") int winner) {
        playerWonLabel.setText(playerWonLabel.getText().formatted(winner));
    }

}

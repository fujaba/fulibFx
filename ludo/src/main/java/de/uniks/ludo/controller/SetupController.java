package de.uniks.ludo.controller;

import de.uniks.ludo.controller.sub.CreditModalComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Title;
import org.fulib.fx.annotation.event.OnKey;
import org.fulib.fx.constructs.Modals;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

@Title("%setup.title")
@Controller
public class SetupController extends BaseController {

    @FXML
    public Button startButton;
    @FXML
    private Slider playerAmountSlider;

    @Inject
    Provider<CreditModalComponent> versionProvider;
    @Inject
    Modals modals;

    @Inject
    public SetupController() {
    }

    @FXML
    public void onPlayClick() {
        int playerAmount = (int) this.playerAmountSlider.getValue();
        this.app.show("ingame", Map.of("playerAmount", playerAmount));
    }

    @OnKey(code = KeyCode.I, control = true)
    public void moveableInformation() {
        modals.modal(versionProvider.get()).dialog(false).show();
    }

    @OnKey(code = KeyCode.I, control = false, strict = true)
    public void information() {
        modals.modal(versionProvider.get()).dialog(true).show();
    }

}

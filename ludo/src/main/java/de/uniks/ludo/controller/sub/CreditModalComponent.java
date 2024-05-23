package de.uniks.ludo.controller.sub;

import de.uniks.ludo.App;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.OnRender;
import org.fulib.fx.annotation.param.Param;

import javax.inject.Inject;

@Component
public class CreditModalComponent extends VBox {

    @Inject
    App app;

    @Param("modalStage")
    Stage modalStage;

    @Inject
    public CreditModalComponent() {
    }

    @OnRender
    public void render() {
        this.setBorder(Border.stroke(Paint.valueOf("black")));
        Label title = new Label("Ludo");
        title.setUnderline(true);
        Label label = new Label("Powered by FulibFx.\nThis game has been created by LeStegii and ClashSoft.");
        Button ok = new Button("OK");
        ok.setOnAction(e -> modalStage.close());

        this.getChildren().addAll(title, label, ok);
    }


}

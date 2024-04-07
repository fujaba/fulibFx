package de.uniks.example.controller.sub;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.onRender;

import javax.inject.Inject;

@Component
public class ExampleSubComponent extends VBox {

    Label label = new Label();

    @Inject
    public ExampleSubComponent() {
        this.setAlignment(Pos.CENTER);
        this.getChildren().add(label);
    }

    public void setText(String text) {
        label.setText(text);
    }

    @onRender
    public void onRender() {
        Color color = Color.color(Math.random(), Math.random(), Math.random());
        label.setFont(Font.font(20 + Math.random() * 20));
        label.setTextFill(color);
    }
}

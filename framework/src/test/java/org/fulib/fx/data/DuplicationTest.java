package org.fulib.fx.data;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.fulib.fx.duplicate.Duplicators;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class DuplicationTest extends ApplicationTest {


    VBox pane = new VBox();

    @Override
    public void start(Stage stage) {
        pane.setAlignment(Pos.CENTER);
        stage.setScene(new Scene(pane));
        stage.show();
    }

    @Test
    public void button() {
        Button button = new Button();

        // Button(Base)
        button.setDefaultButton(true);
        button.setCancelButton(true);
        button.setOnAction(e -> System.out.println("Button clicked"));

        // Labeled
        button.setText("Button");
        button.setAlignment(Pos.BOTTOM_RIGHT);
        button.setTextAlignment(TextAlignment.RIGHT);
        button.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
        button.setEllipsisString("Button");
        button.setWrapText(true);
        button.setFont(Font.font(20));
        button.setGraphic(new Pane());
        button.setUnderline(true);
        button.setLineSpacing(10);
        button.setContentDisplay(ContentDisplay.TOP);
        button.setGraphicTextGap(10);
        button.setTextFill(Color.color(0.5, 0.5, 0.5));
        button.setMnemonicParsing(true);

        // Control
        button.setContextMenu(new ContextMenu());
        button.setTooltip(new Tooltip("Button"));

        Button button1 = Duplicators.duplicate(button);

        assertEquals(button.isDefaultButton(), button1.isDefaultButton());
        assertEquals(button.isCancelButton(), button1.isCancelButton());
        assertEquals(button.getOnAction(), button1.getOnAction());
        assertEquals(button.getText(), button1.getText());
        assertEquals(button.getAlignment(), button1.getAlignment());
        assertEquals(button.getTextAlignment(), button1.getTextAlignment());
        assertEquals(button.getTextOverrun(), button1.getTextOverrun());
        assertEquals(button.getEllipsisString(), button1.getEllipsisString());
        assertEquals(button.isWrapText(), button1.isWrapText());
        assertEquals(button.getFont(), button1.getFont());
        assertEquals(button.getGraphic(), button1.getGraphic());
        assertEquals(button.isUnderline(), button1.isUnderline());
        assertEquals(button.getLineSpacing(), button1.getLineSpacing());
        assertEquals(button.getContentDisplay(), button1.getContentDisplay());
        assertEquals(button.getGraphicTextGap(), button1.getGraphicTextGap());
        assertEquals(button.getTextFill(), button1.getTextFill());
        assertEquals(button.isMnemonicParsing(), button1.isMnemonicParsing());
        assertEquals(button.getContextMenu(), button1.getContextMenu());
        assertEquals(button.getTooltip(), button1.getTooltip());
    }

    @Test
    public void parents() {
        VBox vBox = new VBox(
                new Label("1"),
                new Text("2")
        );

        VBox duplicate = Duplicators.duplicate(vBox);

        Platform.runLater(() -> {
            pane.getChildren().add(vBox);
            pane.getChildren().add(duplicate);
        });
        waitForFxEvents();
        sleep(200);

        assertEquals(List.of("1", "2"), duplicate.getChildren().stream().map(node -> {
            if (node instanceof Label) {
                return ((Label) node).getText();
            } else if (node instanceof Text) {
                return ((Text) node).getText();
            }
            return null; // should not happen
        }).toList());

    }

    @Test
    public void imageView() throws URISyntaxException, MalformedURLException {
        HBox hBox = new HBox(
                new ImageView(
                        new Image(String.valueOf(Objects.requireNonNull(this.getClass().getResource("128.jpg")).toURI().toURL()))
                )
        );

        HBox duplicate = Duplicators.duplicate(hBox);

        ImageView original = (ImageView) hBox.getChildren().get(0);
        ImageView copied = (ImageView) duplicate.getChildren().get(0);

        assertEquals(original.getImage(), copied.getImage());
    }


}

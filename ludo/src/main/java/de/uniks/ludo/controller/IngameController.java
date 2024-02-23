package de.uniks.ludo.controller;

import de.uniks.ludo.Constants;
import de.uniks.ludo.controller.sub.DiceSubComponent;
import de.uniks.ludo.model.*;
import de.uniks.ludo.service.GameService;
import io.reactivex.rxjava3.schedulers.Schedulers;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.Shadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.controller.Subscriber;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class IngameController {

    @FXML
    public Label playerLabel;
    @FXML
    private AnchorPane boardPane;

    @Inject
    GameService gameService;
    @Inject
    Subscriber subscriber;
    @Inject
    ResourceBundle bundle;

    @FXML
    @Inject
    @SubComponent
    DiceSubComponent diceSubComponent;

    private Game game;
    private final Map<Field, Circle> fieldToCircle;
    private final Map<Piece, Circle> pieceToCircle;
    private final ObjectProperty<Player> currentPlayer;
    private final IntegerProperty eyes;

    @Inject
    public IngameController() {
        this.fieldToCircle = new HashMap<>();
        this.pieceToCircle = new HashMap<>();
        this.currentPlayer = new SimpleObjectProperty<>();
        this.eyes = new SimpleIntegerProperty();
    }

    @Param("playerAmount")
    public void setup(int playerAmount) {
        this.game = this.gameService.createGame(playerAmount);
        this.currentPlayer.set(this.game.getCurrentPlayer());
    }

    @onRender
    public void drawBoard() {
        for (Field field : this.game.getBoard().getFields()) {
            Circle circle = new Circle(20);
            AnchorPane.setTopAnchor(circle, (double) field.getY() * 50);
            AnchorPane.setLeftAnchor(circle, (double) field.getX() * 50);
            String color = field instanceof HomeField ? "gray" : "white";
            String borderColor = field instanceof GoalField baseField ? Constants.COLORS.get(baseField.getOwner().getId()) : "black";
            circle.setStyle("-fx-fill: %s; -fx-stroke: %s; -fx-stroke-width: 2;".formatted(color, borderColor));
            this.boardPane.getChildren().add(circle);
            this.fieldToCircle.put(field, circle);
        }

        for (Player player : this.game.getPlayers()) {
            for (int i = 0; i < player.getPieces().size(); i++) {
                Circle circle = new Circle(16);
                AnchorPane.setTopAnchor(circle, (double) player.getHomeFields().get(i).getY() * 50 + 4);
                AnchorPane.setLeftAnchor(circle, (double) player.getHomeFields().get(i).getX() * 50 + 4);
                String color = Constants.COLORS.get(player.getId());
                circle.setStyle("-fx-fill: %s; -fx-stroke: black; -fx-stroke-width: 2;".formatted(color));
                this.boardPane.getChildren().add(circle);
                this.pieceToCircle.put(player.getPieces().get(i), circle);
            }
            Circle startCircle = fieldToCircle.get(player.getStartField());
            startCircle.setStyle(startCircle.getStyle() + "-fx-stroke: %s; -fx-stroke-width: 4;".formatted(Constants.COLORS.get(player.getId())));
        }
    }

    @onRender
    public void setupDice() {
        this.diceSubComponent.setOnMouseClicked(event -> {
            if (!this.diceSubComponent.isEnabled()) return;
            this.subscriber.subscribe(this.diceSubComponent.roll(), Schedulers.computation(),
                    eyes -> {
                        this.eyes.set(eyes);
                        displayCurrentTurn();
                    },
                    Throwable::printStackTrace
            );
        });
    }

    @onRender
    private void displayCurrentTurn() {

        if (eyes.get() == 0) return;

        currentPlayer.get().getPieces().forEach(piece -> {
            Circle circle = this.pieceToCircle.get(piece);
            circle.setOnMouseClicked(click ->
                    this.gameService.getTargetField(piece.getOn(), currentPlayer.get(), eyes.get()).ifPresent(targetField -> {
                        Circle targetCircle = this.fieldToCircle.get(targetField);
                        targetCircle.setEffect(new Shadow(10, Color.web(Constants.COLORS.get(currentPlayer.get().getId()))));
                        targetCircle.setOnMouseClicked(click1 -> {
                            this.gameService.movePiece(piece, targetField);
                            targetCircle.setEffect(null);

                        });
                    })
            );
        });
    }

    @onRender
    public void setupTexts() {
        this.subscriber.listen(
                game.listeners(),
                Game.PROPERTY_CURRENT_PLAYER,
                evt -> this.currentPlayer.set((Player) evt.getNewValue())
        );
        this.subscriber.bind(this.playerLabel.textProperty(), this.currentPlayer.map(Player::getId).map(id -> this.bundle.getString("ingame.current.player").formatted(id)));
    }

    @onRender
    public void setupPieceMovements() {
        this.game.getPlayers().forEach(player ->
                player.getPieces().forEach(piece ->
                        this.subscriber.listen(
                                piece.listeners(),
                                Piece.PROPERTY_ON,
                                evt -> {
                                    Field newField = (Field) evt.getNewValue();
                                    if (newField != null) {
                                        AnchorPane.setTopAnchor(this.pieceToCircle.get(piece), (double) newField.getY() * 50 + 4);
                                        AnchorPane.setLeftAnchor(this.pieceToCircle.get(piece), (double) newField.getX() * 50 + 4);
                                    }
                                }
                        ))
        );
    }

    @onDestroy
    public void onDestroy() {
        this.boardPane.getChildren().clear();
        this.fieldToCircle.clear();
        this.pieceToCircle.clear();
        this.subscriber.dispose();
    }

}

package de.uniks.ludo.controller;

import de.uniks.ludo.App;
import de.uniks.ludo.Constants;
import de.uniks.ludo.LudoUtil;
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
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Shadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.fulib.fx.annotation.controller.Controller;
import org.fulib.fx.annotation.controller.Resource;
import org.fulib.fx.annotation.controller.SubComponent;
import org.fulib.fx.annotation.controller.Title;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onKey;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.controller.Subscriber;

import javax.inject.Inject;
import java.util.*;

@Title("%ingame.title")
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
    @Resource
    @Inject
    ResourceBundle bundle;
    @Inject
    App app;

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

    @onInit
    public void setup(@Param("playerAmount") int playerAmount) {
        if (this.game == null) this.game = this.gameService.createGame(playerAmount);
        this.currentPlayer.set(this.game.getCurrentPlayer());
    }

    @onRender
    public void drawBoard() {
        for (Field field : this.game.getBoard().getFields()) {
            Circle circle = createFieldCircle(field);
            this.boardPane.getChildren().add(circle);
            this.fieldToCircle.put(field, circle);
        }
    }

    @onRender(1)
    public void drawPieces() {
        for (Player player : this.game.getPlayers()) {
            for (int i = 0; i < player.getPieces().size(); i++) {
                Piece piece = player.getPieces().get(i);
                Circle circle = createPieceCircle(piece);
                setupListenerForPiece(circle, piece, Constants.COLORS.get(player.getId()));
                this.boardPane.getChildren().add(circle);
                this.pieceToCircle.put(player.getPieces().get(i), circle);
            }
            Circle startCircle = fieldToCircle.get(player.getStartField());
            startCircle.setFill(Color.web(Constants.COLORS.get(player.getId())));
            startCircle.setStrokeType(javafx.scene.shape.StrokeType.INSIDE);
            startCircle.setStroke(Color.BLACK);
        }
    }

    @onRender
    public void setupDice() {
        this.diceSubComponent.setOnMouseClicked(event -> {
            rollDice();
        });
        this.subscriber.bind(this.diceSubComponent.eyesLabel.textFillProperty(), this.currentPlayer.map(player -> Color.web(Constants.COLORS.get(player.getId()))));
    }

    @onKey(code = KeyCode.R)
    private void rollDice() {
        System.out.println("Rolling dice");
        if (!this.diceSubComponent.isEnabled()) return;
        LudoUtil.playSound(Constants.SOUND_ROLL_DICES);
        this.subscriber.subscribe(this.diceSubComponent.roll(), Schedulers.computation(),
                eyes -> {
                    this.eyes.set(eyes);
                    if (this.gameService.stuck(this.currentPlayer.get(), eyes)) {
                        this.gameService.nextPlayer(game);
                        this.eyes.set(0);
                        this.diceSubComponent.reset();
                    }
                },
                Throwable::printStackTrace
        );
    }

    @onRender
    private void displayCurrentTurn() {
        if (eyes.get() != 0) {
            this.diceSubComponent.setLabel(eyes.get());
        }
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

    @onRender(2)
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

    // Sets up the listener for a piece
    private void setupListenerForPiece(Circle circle, Piece piece, String color) {
        Player player = piece.getOwner();
        circle.setOnMouseEntered(event -> {
            if (player == this.currentPlayer.get() && this.eyes.get() != 0) {
                Optional<Field> target = gameService.getTargetField(piece.getOn(), player, this.eyes.get());
                target.ifPresent(field -> {
                    // When the mouse enters the circle, the field the piece can move to is highlighted
                    fieldToCircle.get(field).setEffect(new Shadow(BlurType.ONE_PASS_BOX, Color.web(color), 10));
                    // When the circle is clicked, the piece is moved to the field
                    circle.setOnMouseClicked(click -> {
                        if (player == this.currentPlayer.get() && this.eyes.get() != 0) {
                            boolean won = this.gameService.movePiece(piece, field);
                            LudoUtil.playSound(Constants.SOUND_PLACE_PIECE);
                            // If the player won, the game over screen is shown
                            if (won) {
                                app.show("/ingame/gameover", Map.of("winner", player.getId()));
                                return;
                            }
                            fieldToCircle.get(field).setEffect(null);
                            if (this.eyes.get() != 6) {
                                this.gameService.nextPlayer(game);
                            }
                            this.eyes.set(0);
                            this.diceSubComponent.reset();
                        }
                    });
                });
            }

        });
        circle.setOnMouseExited(event -> {
            if (player == this.currentPlayer.get() && this.eyes.get() != 0) {
                Optional<Field> target = gameService.getTargetField(piece.getOn(), player, this.eyes.get());
                target.ifPresent(field -> fieldToCircle.get(field).setEffect(null));
                circle.setOnMouseClicked(null);
            }
        });
    }

    // Creates a circle for a piece and sets the position to the piece's current field
    private Circle createPieceCircle(Piece piece) {
        Circle circle = new Circle(16);
        circle.setId("piece-" + piece.getOwner().getId() + "-" + piece.getOwner().getPieces().indexOf(piece));
        Player player = piece.getOwner();
        AnchorPane.setTopAnchor(circle, (double) piece.getOn().getY() * 50 + 4);
        AnchorPane.setLeftAnchor(circle, (double) piece.getOn().getX() * 50 + 4);
        circle.setCursor(javafx.scene.Cursor.HAND);
        return style(circle, Constants.COLORS.get(player.getId()), "black");
    }

    // Creates a circle for a field and sets the position to the field's coordinates
    private Circle createFieldCircle(Field field) {
        Circle circle = new Circle(20);
        circle.setId("field-" + field.getX() + "-" + field.getY());
        AnchorPane.setTopAnchor(circle, (double) field.getY() * 50);
        AnchorPane.setLeftAnchor(circle, (double) field.getX() * 50);
        String color = field instanceof HomeField ? "gray" : "white";
        String borderColor = field instanceof GoalField baseField ? Constants.COLORS.get(baseField.getOwner().getId()) : "black";
        return style(circle, color, borderColor);
    }

    // Styles a circle with a color and a border color
    private Circle style(Circle circle, String color, String borderColor) {
        circle.setFill(Color.web(color));
        circle.setStrokeType(StrokeType.OUTSIDE);
        circle.setStroke(Color.web(borderColor));
        circle.setStrokeWidth(2);
        return circle;
    }

    @onDestroy
    public void onDestroy() {
        this.boardPane.getChildren().forEach(node -> {
            node.setOnMouseClicked(null);
            node.setOnMouseEntered(null);
            node.setOnMouseExited(null);
        });
        this.boardPane.getChildren().clear();
        this.fieldToCircle.clear();
        this.pieceToCircle.clear();
        this.subscriber.dispose();
    }

}

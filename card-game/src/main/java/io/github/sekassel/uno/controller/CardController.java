package io.github.sekassel.uno.controller;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.controller.Subscriber;
import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.service.GameService;
import io.github.sekassel.uno.util.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javax.inject.Inject;

@Component(view = "view/sub/card.fxml")
public class CardController extends VBox implements Titleable {

    public static final String CARD_TITLE = "Uno - Card";

    @FXML
    public VBox cardScreen;
    @FXML
    public Label cardTypeLabel;

    @Inject
    GameService gameService;

    @Inject
    Subscriber subscriber;

    private Card card;

    @Inject
    public CardController() {
        System.out.println(this + " created.");
    }

    @Override
    public String getTitle() {
        return CARD_TITLE;
    }

    @onRender
    public void render() {

        cardScreen.setId((this.card.getColor() + "_" + this.card.getType()).toLowerCase());

        setupPropertyChangeListeners();
        setupSelection();
        setColor();
    }

    /**
     * Sets up all the required listeners for displaying the card.
     */
    private void setupPropertyChangeListeners() {
        // Change the card color if the card color changes (wild cards)
        subscriber.listen(card.listeners(), Card.PROPERTY_COLOR, event -> setColor());
    }

    /**
     * Set up a click listener so that the card will be selected when it's clicked.
     */
    private void setupSelection() {

        Player owner = card.getOwner();

        if (owner == null) {
            return;
        }

        cardScreen.setOnMouseClicked(event -> gameService.selectCard(this.card));
    }

    /**
     * Update the color of the controller.
     */
    private void setColor() {
        cardScreen.setStyle(Utils.getCardStyle(this.card.getColor()));
        cardTypeLabel.setText(card.getType().getIcon());
    }

    /**
     * Highlights a card or removes the highlight from a card.
     *
     * @param highlight Whether the card should be highlighted
     */
    public void highlight(boolean highlight) {
        cardTypeLabel.setFont(Font.font(Constants.CARD_FONT_FAMILY, highlight ? FontWeight.BOLD : FontWeight.NORMAL, Constants.CARD_FONT_SIZE));
        cardTypeLabel.setUnderline(highlight);
    }

    /**
     * Sets the card that should be displayed.
     *
     * @param card The card that should be displayed
     */
    public CardController setCard(Card card) {
        this.card = card;
        return this;
    }

    @onDestroy
    public void destroy() {
        // Remove the card from the game
        this.subscriber.dispose();
    }


    @onInit
    public void initSout() {
        System.out.println(this + " initialized.");
    }

    @onRender()
    public void renderSout() {
        System.out.println(this + " rendered.");
    }

    @onDestroy
    public void destroySout() {
        System.out.println(this + " destroyed.");
    }

}

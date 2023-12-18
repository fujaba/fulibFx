package io.github.sekassel.uno.controller;

import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.util.Utils;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.PropertyChangeListener;

public class CardController implements Controller {

    public static final String CARD_TITLE = "Uno - Card";

    private final Card card;
    private final IngameController parent;

    @FXML
    public VBox cardScreen;
    @FXML
    public Label cardTypeLabel;

    public CardController(IngameController parent, Card card) {
        this.parent = parent;
        this.card = card;
    }

    @Override
    public String getTitle() {
        return CARD_TITLE;
    }

    @Override
    public void init() {
        // Nothing to init here
    }

    @Override
    public Parent render() {
        Parent rendered = loadControllerScreen(this, "view/card.fxml");

        rendered.setId((this.card.getColor() + "_" + this.card.getType()).toLowerCase());

        setupPropertyChangeListeners();
        setupSelection();
        setColor();

        return rendered;
    }

    /**
     * Sets up all the required listeners for displaying the card.
     */
    private void setupPropertyChangeListeners() {
        // Change the card color if the card color changes (wild cards)
        this.card.listeners().addPropertyChangeListener(Card.PROPERTY_COLOR, event -> setColor());
    }

    /**
     * Set up a click listener so that the card will be selected when it's clicked.
     */
    private void setupSelection() {

        Player owner = card.getOwner();

        if (owner == null) {
            return;
        }

        cardScreen.setOnMouseClicked(event -> parent.getApp().getGameService().selectCard(this.card));
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
     * @param highlight Whether the card should be highlighted
     */
    public void highlight(boolean highlight) {
        cardTypeLabel.setFont(Font.font(Constants.CARD_FONT_FAMILY, highlight ? FontWeight.BOLD : FontWeight.NORMAL, Constants.CARD_FONT_SIZE));
        cardTypeLabel.setUnderline(highlight);
    }

    @Override
    public void destroy() {
        // Remove all registered card listeners
        for (PropertyChangeListener listener : this.card.listeners().getPropertyChangeListeners()) {
            this.card.listeners().removePropertyChangeListener(listener);
        }
    }

    /**
     * Returns the current screen (main pane for the controller).
     * This can be called before render() has been called to change things before the rendering.
     * It can also be used to get the pane without rendering again.
     *
     * @return The current screen (main pane for the controller)
     */
    public Pane getAsPane() {
        return this.cardScreen;
    }

    /**
     * @return The card this controller displays.
     */
    public Card getCard() {
        return this.card;
    }
}

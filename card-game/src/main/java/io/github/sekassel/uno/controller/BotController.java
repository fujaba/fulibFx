package io.github.sekassel.uno.controller;

import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Player;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.beans.PropertyChangeListener;

public class BotController implements Controller {

    public static final String BOT_TITLE = "Uno - Bot";

    private final Player bot;
    private final IngameController parent;

    @FXML
    public VBox botBox;
    @FXML
    public Label nameLabel;
    @FXML
    public Label currentCardsValueLabel;
    @FXML
    public Label iconLabel;

    public BotController(IngameController parent, Player bot) {
        this.parent = parent;
        this.bot = bot;
    }

    @Override
    public String getTitle() {
        return BOT_TITLE;
    }

    @Override
    public void init() {
        // Nothing to init here
    }

    @Override
    public Parent render() {
        Parent rendered = loadControllerScreen(this, "view/bot.fxml");

        botBox.setId(bot.getName().replace(" ", ""));

        updateCards();
        updateName();
        setupPropertyChangeListeners();


        return rendered;
    }

    /**
     * Sets up all the required listeners for displaying the card.
     */
    private void setupPropertyChangeListeners() {
        // Update the card amount if the cards of the bot change (or display a winning screen if the bot won the game)
        this.bot.listeners().addPropertyChangeListener(Player.PROPERTY_CARDS,
                event -> {
                    updateCards();
                    if (this.bot.getCards().isEmpty()) {
                        parent.displayWinner(this.bot);
                    }
                }
        );
    }

    /**
     * Updates the card amount to the correct number.
     */
    public void updateCards() {
        currentCardsValueLabel.setText(String.valueOf(bot.getCards().size()));
    }

    /**
     * Updates the name to the correct value.
     */
    public void updateName() {
        this.nameLabel.setText(bot.getName());
    }

    /**
     * Highlights a bot or removes the highlight from a bot.
     * @param highlight Whether the bot should be highlighted
     */
    public void highlight(boolean highlight) {
        nameLabel.setUnderline(highlight);
        iconLabel.setFont(Font.font(Constants.BOT_ICON_FONT_FAMILY, highlight ? 72 : 64));
    }

    @Override
    public void destroy() {
        // Remove all registered bot listeners
        for (PropertyChangeListener listener : this.bot.listeners().getPropertyChangeListeners()) {
            this.bot.listeners().removePropertyChangeListener(listener);
        }
        // Remove the bot from the game
        this.parent.getApp().getGameService().getBotService().removeBot(bot);

    }

    /**
     * Returns the current screen (main pane for the controller).
     * This can be called before render() has been called to change things before the rendering.
     * It can also be used to get the pane without rendering again.
     *
     * @return The current screen (main pane for the controller)
     */
    public Pane getAsPane() {
        return botBox;
    }

    /**
     * @return The bot this controller displays.
     */
    public Player getBot() {
        return this.bot;
    }
}

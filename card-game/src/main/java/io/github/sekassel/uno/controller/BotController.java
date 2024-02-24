package io.github.sekassel.uno.controller;

import org.fulib.fx.annotation.controller.Component;
import org.fulib.fx.annotation.event.onDestroy;
import org.fulib.fx.annotation.event.onInit;
import org.fulib.fx.annotation.event.onRender;
import org.fulib.fx.annotation.param.Param;
import org.fulib.fx.controller.Subscriber;
import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.service.GameService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import javax.inject.Inject;

@Component(view = "sub/Bot.fxml")
public class BotController extends VBox {

    public static final String BOT_TITLE = "Uno - Bot";

    @FXML
    public VBox botBox;
    @FXML
    public Label nameLabel;
    @FXML
    public Label currentCardsValueLabel;
    @FXML
    public Label iconLabel;

    @Inject
    GameService gameService;
    @Inject
    Subscriber subscriber;

    @Param("bot")
    private Player bot;

    @Param("parent")
    private IngameController parent;

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

    @onRender()
    public void render() {

        botBox.setId(bot.getName().replace(" ", ""));

        updateCards();
        updateName();
        setupPropertyChangeListeners();
    }

    /**
     * Sets up all the required listeners for displaying the card.
     */
    private void setupPropertyChangeListeners() {
        // Update the card amount if the cards of the bot change (or display a winning screen if the bot won the game)
        this.subscriber.listen(bot.listeners(), Player.PROPERTY_CARDS, event -> {
            updateCards();
            if (bot.getGame() != null && this.bot.getCards().isEmpty()) {
                parent.displayWinner(this.bot);
            }
        });
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
     *
     * @param highlight Whether the bot should be highlighted
     */
    public void highlight(boolean highlight) {
        nameLabel.setUnderline(highlight);
        iconLabel.setFont(Font.font(Constants.BOT_ICON_FONT_FAMILY, highlight ? 72 : 64));
    }

    @onDestroy
    public void destroy() {
        // Remove the bot from the game
        this.gameService.getBotService().removeBot(bot);
        this.subscriber.dispose();
    }

    @Inject
    public BotController() {
        super();
        System.out.println(this + " created.");
    }

}

package io.github.sekassel.uno.service;

import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.util.CardColor;
import io.github.sekassel.uno.util.Utils;
import javafx.application.Platform;

import javax.swing.*;
import java.util.List;
import java.util.Random;

public class BotService {

    private final Random random;
    private final GameService gameService;

    public BotService(GameService gameService) {
        this.random = gameService.getRandom();
        this.gameService = gameService;
    }

    /**
     * Makes a bot play a round after a certain delay.
     *
     * @param bot The bot which should play
     */
    public void startTurn(Player bot) {
        Timer timer = new Timer(Constants.BOT_PLAY_DELAY, task -> Platform.runLater(() -> this.playRound(bot)));
        timer.setInitialDelay(Constants.BOT_PLAY_DELAY);
        timer.setRepeats(false);
        timer.restart();
    }

    /**
     * Selects a random cart out of the bot's cards which can be played
     *
     * @param bot The bot from which a card should be picked
     * @return A card which can be played or null
     */
    public Card selectCardToPlay(Player bot) {
        Card current = bot.getGame().getCurrentCard();
        List<Card> possibleCards = bot.getCards().stream().filter(card -> card.canBeOnTopOf(current)).toList();
        return possibleCards.isEmpty() ? null : possibleCards.get(random.nextInt(possibleCards.size()));
    }

    /**
     * Selects a card and plays it. If no card can be found, a card will be drawn.
     * If there is still no playable card, the next player will be selected.
     *
     * @param bot The bot which should play a card
     */
    private void playRound(Player bot) {
        Card selected = selectCardToPlay(bot);

        // If no card can be selected, draw a card
        if (selected == null) {
            Card drawn = this.gameService.handoutCards(bot, 1).get(0);
            if (!drawn.canBeOnTopOf(bot.getGame().getCurrentCard())) {
                this.gameService.selectNextPlayer(bot.getGame(), 1);
                Utils.playSound(Constants.SOUND_FAIL);
                return;
            }
            selected = drawn;
        }

        CardColor color = selected.getColor();

        // If the card is a wildcard, select a random color
        color = color == CardColor.WILD ? Utils.getRandomColor(this.random) : null;

        // Play the card
        gameService.playRound(selected, color);
    }

    /**
     * Removes a bot from the game.
     * @param player The bot to remove
     */
    public void removeBot(Player player) {
        player.removeYou();
    }

    /**
     * @return The currently used random instance.
     */
    public Random getRandom() {
        return this.random;
    }
}

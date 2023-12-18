package io.github.sekassel.uno.service;

import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Game;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.util.CardColor;
import io.github.sekassel.uno.util.CardType;

import java.util.Random;

public class CardService {

    private final Random random;

    public CardService(Random random) {
        this.random = random;
    }

    /**
     * Generates a card and gives it to the player
     *
     * @param player The player which should receive a card
     * @return The card given to the player
     */
    public Card giveCardTo(Player player) {
        return generateCard(player.getGame()).setOwner(player);
    }

    /**
     * Creates a random card
     *
     * @return The random card
     */
    public Card generateCard(Game game) {
        // Generate a random number (common or special)
        int num = this.random.nextInt(CardType.values().length);

        // If the card isn't a wild card, pick a random color
        CardColor color;
        if (CardType.values()[num] == CardType.WILD) {
            color = CardColor.WILD;
        } else {
            int colorNum = this.random.nextInt(CardColor.values().length - 1);
            color = CardColor.values()[colorNum];
        }

        // Generate the card and return it
        return new Card().setColor(color).setType(CardType.values()[num]).setGame(game);

    }

    /**
     * @return The currently used random instance.
     */
    public Random getRandom() {
        return this.random;
    }

}

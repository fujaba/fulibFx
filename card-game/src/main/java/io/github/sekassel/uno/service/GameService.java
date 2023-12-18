package io.github.sekassel.uno.service;

import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.Returnable;
import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Game;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.util.CardColor;
import io.github.sekassel.uno.util.CardType;
import io.github.sekassel.uno.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameService {

    private final CardService cardService;
    private final BotService botService;
    private final Random random;

    public GameService(Random random) {
        this.random = random;
        this.cardService = new CardService(random);
        this.botService = new BotService(this);
    }

    public static Returnable<Card> getCard() {
        return new Returnable<>(Returnable.ReturnStatus.SUCCESS, new Card());
    }

    public static void main(String[] args) {
        Returnable<Card> card = getCard();
        if (card.getStatus().equals(Returnable.ReturnStatus.SUCCESS)) {
            System.out.println(card.getValue());
        }

        System.out.println(card.getValueOrElse(new Card()));

    }

    /**
     * Creates a game with a player and a certain number of bots.
     *
     * @param playerName The name of the player
     * @param bots The amount of bots
     * @return The created game
     */
    public Game createGame(String playerName, int bots) {
        Player player = new Player().setName(playerName);

        // Initialize the game with the human as the current player
        Game game = new Game().setClockwise(true).withPlayers(player).setCurrentPlayer(player);

        // Create bots
        for (int i = 1; i <= bots; i++) {
            game.withPlayers(new Player().setName(Constants.BOT_NAME.formatted(i)));
        }

        return game;
    }

    /**
     * Hands out cards to every player and selects a random start card.
     *
     * @param game The game to initialize
     */
    public void initialize(Game game) {

        // Handout cards to every player
        game.getPlayers().forEach(player -> handoutCards(player, Constants.START_CARD_AMOUNT));

        // Generate a card and give it a random color if it's a wild card
        Card firstCard = cardService.generateCard(game);
        if (firstCard.getType() == CardType.WILD) {
            firstCard.setColor(Utils.getRandomColor(this.random));
        }

        // Place the card
        game.setCurrentCard(firstCard);

    }

    /**
     * Hands out cards to a player.
     *
     * @param player The player which should receive cards
     * @param amount The amount of cards
     * @return The cards given to the player
     */
    public List<Card> handoutCards(Player player, int amount) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            cards.add(cardService.giveCardTo(player));
        }
        return cards;
    }

    public void selectCard(Card card) {
        card.getOwner().setCurrentCard(card);
    }

    /**
     * Plays a given card as a given color and run its special effects.
     *
     * @param card The card to play
     * @param color The color to play the card as (if null, the color will not be changed)
     */
    public void playRound(Card card, CardColor color) {

        Game game = card.getGame();
        Card current = game.getCurrentCard();

        if (!card.canBeOnTopOf(current)) {
            return;
        }

        if (color != null) {
            card.setColor(color);
        }

        Utils.playSound(Constants.SOUND_CLICK);

        // Remove the card from the player
        card.getOwner().setCurrentCard(null);
        card.setOwner(null);

        // Delete the old card, as it will never appear again
        game.withoutCards(current);
        current.removeYou();

        // Place the card
        game.setCurrentCard(card);

        // Check for special cards
        if (card.getType().hasAction()) {
            card.getType().getAction().run(card, this);
        } else {
            selectNextPlayer(game, 1);
        }
    }


    /**
     * Updates the current player of a given game.
     *
     * @param game The game to update.
     * @param amount The amount of players to go over (amount of players to skip plus one)
     */
    public void selectNextPlayer(Game game, int amount) {
        Player next = getNextPlayer(game, amount);

        Player current = game.getCurrentPlayer();
        game.setCurrentPlayer(next);

        // If the player stays the same, no event gets fired, so we have to call the method manually
        if (current == next && next != game.getFirstPlayer()) {
            getBotService().startTurn(next);
        }

    }

    /**
     * Returns the player which should play next.
     *
     * @param game The current game
     * @param amount The amount of players to go over (amount of players to skip plus one)
     * @return The next player
     */
    public Player getNextPlayer(Game game, int amount) {
        int nextIndex = getNextPlayerIndex(game, amount);
        return game.getPlayers().get(nextIndex);
    }

    /**
     * Returns the index of the player which should play next.
     *
     * @param game The current game
     * @param amount The amount of players to go over (amount of players to skip plus one)
     * @return The index of the next player
     */
    private int getNextPlayerIndex(Game game, int amount) {
        int current = game.getPlayers().indexOf(game.getCurrentPlayer());
        int players = game.getPlayers().size();

        if (game.isClockwise()) {
            return (current + amount) % players;
        } else {
            return (current + players - amount) % players;
        }
    }

    /**
     * @return The currently used bot service instance.
     */
    public BotService getBotService() {
        return botService;
    }

    /**
     * @return The currently used card service instance.
     */
    public CardService getCardService() {
        return cardService;
    }

    /**
     * @return The currently used random instance.
     */
    public Random getRandom() {
        return this.random;
    }
}

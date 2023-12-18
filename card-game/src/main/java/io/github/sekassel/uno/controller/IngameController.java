package io.github.sekassel.uno.controller;

import io.github.sekassel.uno.App;
import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Game;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.util.CardColor;
import io.github.sekassel.uno.util.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;

public class IngameController implements Controller {

    public static final String INGAME_SCREEN_TITLE = "Uno - In Game";

    private final App app;
    private final Game game;

    private final BooleanProperty wildCardSelectedProperty = new SimpleBooleanProperty();
    private final BooleanProperty playersTurn = new SimpleBooleanProperty();
    private final BooleanProperty cardSelected = new SimpleBooleanProperty();

    @FXML
    public VBox ingameScreen;
    @FXML
    public VBox colorSelectorBox;
    @FXML
    public HBox mainBox1;
    @FXML
    public HBox mainBox2;
    @FXML
    public HBox cardListHBox;
    @FXML
    public Button drawButton;
    @FXML
    public Button playButton;
    @FXML
    public Label playersTurnText;
    @FXML
    public Label directionIconLabel;

    HashMap<Card, CardController> cards = new HashMap<>();
    HashMap<Player, BotController> bots = new HashMap<>();

    private CardController currentCardController;

    public IngameController(App app, Game game) {
        this.app = app;
        this.game = game;
    }

    @Override
    public String getTitle() {
        return INGAME_SCREEN_TITLE;
    }

    @Override
    public void init() {
        // Nothing to init here
    }

    @Override
    public Parent render() {
        Parent rendered = loadControllerScreen(this, "view/ingame.fxml");

        // Setup gui elements
        setupPropertyChangeListeners();
        renderBots();

        // Initialize the game
        this.app.getGameService().initialize(this.game);

        return rendered;
    }

    /**
     * Sets up all the required listeners for displaying the game.
     */
    private void setupPropertyChangeListeners() {

        // Listener for displaying the direction of the game
        this.game.listeners().addPropertyChangeListener(Game.PROPERTY_CLOCKWISE,
                event -> this.directionIconLabel.setText(this.game.isClockwise() ? Constants.CLOCKWISE_ICON : Constants.COUNTER_CLOCKWISE_ICON));

        // Listener for displaying the last played card in the center
        this.game.listeners().addPropertyChangeListener(Game.PROPERTY_CURRENT_CARD,
                event -> displayLastPlayed((Card) event.getNewValue())
        );

        // Listener for removing/adding new cards to a player
        this.game.getFirstPlayer().listeners().addPropertyChangeListener(Player.PROPERTY_CARDS,
                event -> {
                    // Card is removed or played
                    if (event.getNewValue() == null) {
                        removeCardFromInventory((Card) event.getOldValue());
                    }
                    // Card is added to the player
                    else {
                        renderCardInInventory((Card) event.getNewValue());
                    }
                    // Check if the player wins
                    if (game.getFirstPlayer().getCards().isEmpty()) {
                        displayWinner(game.getFirstPlayer());
                    }
                }
        );

        // Listener for toggling the play/color picker buttons when a wild card is selected
        this.game.getFirstPlayer().listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_CARD,
                event -> this.wildCardSelectedProperty.set(event.getNewValue() != null && ((Card) event.getNewValue()).getColor() == CardColor.WILD)
        );

        // Listener for highlighting the selected card
        this.game.getFirstPlayer().listeners().addPropertyChangeListener(Player.PROPERTY_CURRENT_CARD,
                event -> {
                    if (event.getOldValue() != null) {
                        Card oldCard = ((Card) event.getOldValue());
                        getControllerByCard(oldCard).highlight(false);
                    }

                    if (event.getNewValue() != null) {
                        Card newCard = ((Card) event.getNewValue());
                        getControllerByCard(newCard).highlight(true);
                        cardSelected.set(newCard.canBeOnTopOf(game.getCurrentCard()));
                    } else {
                        cardSelected.set(false);
                    }
                }
        );


        // Listener for toggling the play/draw button when the player is at turn and selecting the playing bot
        this.game.listeners().addPropertyChangeListener(Game.PROPERTY_CURRENT_PLAYER,
                event -> {
                    Player newPlayer = (Player) event.getNewValue();
                    Player oldPlayer = (Player) event.getOldValue();

                    BotController oldController = getControllerByBot(oldPlayer);
                    BotController newController = getControllerByBot(newPlayer);

                    // Highlight the next player (and remove the highlighting from the old one)
                    // As human players don't have a controller, the if-statement won't pass
                    if (oldController != null) {
                        oldController.highlight(false);
                    }
                    if (newController != null) {
                        newController.highlight(true);
                    }

                    // If the new player is a human, it's the player's turn now
                    boolean isHuman = this.game.getFirstPlayer().equals(newPlayer);
                    playersTurn.set(isHuman);

                    // If the new player is a bot, let the bot play
                    if (!isHuman) {
                        app.getGameService().getBotService().startTurn(newPlayer);
                    }

                }
        );

        playersTurn.set(true);
        // Display the color selection when a wildcard has been selected and it's the player's turn
        colorSelectorBox.visibleProperty().bind(wildCardSelectedProperty.and(playersTurn));
        // Disable the play button if the player selected a wildcard, has no card selected or if it's not the player's turn
        playButton.disableProperty().bind(wildCardSelectedProperty.or(playersTurn.not()).or(cardSelected.not()));
        // Disable the draw button if it isn't the player's turn
        drawButton.disableProperty().bind(playersTurn.not());
        // Display the text if it's the player's turn
        playersTurnText.visibleProperty().bind(playersTurn);
    }

    /**
     * The method triggered by the quit button.
     * Display the setup screen again (with the currently used values as initial values).
     */
    @FXML
    public void onQuitPressed() {
        this.app.show(new SetupController(this.app).setInitialBotAmount(this.game.getPlayers().size() - 1).setInitialText(this.game.getFirstPlayer().getName()));
    }

    /**
     * The method triggered by the draw button.
     * Checks if it's the player's turn and then draws a new card.
     * If the card can be played, play it (if it's a wild card randomize the color).
     */
    @FXML
    public void onDrawPressed() {
        if (playersTurn.get()) {
            Card drawn = this.getApp().getGameService().handoutCards(game.getPlayers().get(0), 1).get(0);
            if (drawn.canBeOnTopOf(game.getCurrentCard())) {
                playCard(drawn, drawn.getColor() == CardColor.WILD ? Utils.getRandomColor(this.app.getGameService().getRandom()) : null);
            } else {
                this.app.getGameService().selectNextPlayer(game, 1);
                Utils.playSound(Constants.SOUND_FAIL);
            }
        }
    }

    /**
     * The method triggered by the play button.
     * Checks if it's the player's turn and then plays the currently selected card.
     */
    @FXML
    public void onPlayPressed() {
        Card selected = this.game.getFirstPlayer().getCurrentCard();
        if (playersTurn.get() && selected != null) {
            playCard(selected, null);
        }
    }

    /**
     * The method triggered by the wild card buttons.
     * Different behaviour is defined by the id of the clicked button.
     * Chooses the card color and plays the wild card as a colored card.
     */
    @FXML
    public void onWildPressed(ActionEvent event) {
        Card selected = this.game.getFirstPlayer().getCurrentCard();
        Button pressed = (Button) event.getSource();
        CardColor color = switch (pressed.getId()) {
            case "redButton" -> CardColor.RED;
            case "blueButton" -> CardColor.BLUE;
            case "yellowButton" -> CardColor.YELLOW;
            case "greenButton" -> CardColor.GREEN;
            default -> null;
        };
        playCard(selected, color);
    }

    /**
     * Plays a card and changes its color if set.
     *
     * @param card The card to play
     * @param color The color the card should become (not set if null)
     */
    private void playCard(Card card, CardColor color) {
        this.getApp().getGameService().playRound(card, color);
    }

    /**
     * Creates bot controllers and renders them at the correct place.
     */
    private void renderBots() {
        List<Player> players = this.game.getPlayers();
        players.forEach(player -> {
            if (this.game.getFirstPlayer() == player)
                return;

            BotController botController = generateBotController(player);

            switch (players.indexOf(player)) {
                case 1 -> renderBot(botController, mainBox2, 0);
                case 2 -> renderBot(botController, mainBox1, 0);
                case 3 -> renderBot(botController, mainBox2, 1);
            }
        });
    }

    /**
     * Renders a bot controller at a given pane and index.
     *
     * @param controller The controller to be rendered
     * @param pane Where the controller should be displayed
     * @param index The index of the bot controller
     */
    private void renderBot(BotController controller, Pane pane, int index) {
        pane.getChildren().remove(index);
        pane.getChildren().add(index, controller.render());
    }

    /**
     * Creates a new controller for a bot and adds it to the controller map.
     *
     * @param bot The card to be added
     * @return The created controller
     */
    private BotController generateBotController(Player bot) {
        BotController controller = new BotController(this, bot);
        bots.put(bot, controller);
        controller.init();
        return controller;
    }

    /**
     * Displays a card as the last played card.
     *
     * @param card The card that has been played
     */
    public void displayLastPlayed(Card card) {

        // Destroy the controller of the last played card
        if (currentCardController != null) {
            cards.remove(currentCardController);
            currentCardController.destroy();
        }

        // Remove the last played card if it exists
        if (mainBox2.getChildren().size() >= 3) {
            mainBox2.getChildren().remove(1);
        }

        // Create a new controller and initialize it, if there isn't already one
        if (!cards.containsKey(card)) {
            generateCardController(card).render();
        }

        // Move the controller in the middle
        CardController newController = getControllerByCard(card);

        Parent rendered = newController.getAsPane();
        rendered.setId("lastPlayedCard");
        mainBox2.getChildren().add(1, rendered);

        this.currentCardController = newController;

    }

    /**
     * Renders a card in the player's inventory.
     *
     * @param card The card to be rendered
     */
    public void renderCardInInventory(Card card) {
        cardListHBox.getChildren().add(generateCardController(card).render());
    }

    /**
     * Removes a rendered card.
     *
     * @param card The card to be removed
     */
    public void removeCardFromInventory(Card card) {
        CardController controller = getControllerByCard(card);
        controller.destroy();
        cardListHBox.getChildren().remove(controller.getAsPane());
    }

    /**
     * Creates a new controller for a card and adds it to the controller map.
     *
     * @param card The card to be added
     * @return The created controller
     */
    private CardController generateCardController(Card card) {
        CardController controller = new CardController(this, card);
        cards.put(card, controller);
        controller.init();
        return controller;
    }

    /**
     * Returns the controller for a given card.
     *
     * @param card The card of which the controller is searched
     * @return The controller of the card or null
     */
    public CardController getControllerByCard(Card card) {
        return cards.get(card);
    }

    /**
     * Returns the controller for a given bot.
     *
     * @param bot The bot of which the controller is searched
     * @return The controller of the bot or null
     */
    public BotController getControllerByBot(Player bot) {
        return bots.get(bot);
    }

    /**
     * Displays a winning screen with a given player as the winner of the game.
     *
     * @param player The player who won the game
     */
    public void displayWinner(Player player) {
        this.app.show(new GameOverController(this.app, player));
    }

    @Override
    public void destroy() {
        // Remove all registered game listeners
        for (PropertyChangeListener listener : this.game.listeners().getPropertyChangeListeners()) {
            this.game.listeners().removePropertyChangeListener(listener);
        }
        // Remove all registered player listeners
        for (PropertyChangeListener listener : this.game.getFirstPlayer().listeners().getPropertyChangeListeners()) {
            this.game.getFirstPlayer().listeners().removePropertyChangeListener(listener);
        }
        // Destroy all sub controllers
        cards.values().forEach(cardController -> cardController.destroy());
        bots.values().forEach(botController -> botController.destroy());
    }


    /**
     * Returns the current screen (main pane for the controller).
     * This can be called before render() has been called to change things before the rendering.
     * It can also be used to get the pane without rendering again.
     *
     * @return The current screen (main pane for the controller)
     */
    public Pane getAsPane() {
        return this.ingameScreen;
    }

    /**
     * @return The current App instance
     */
    public App getApp() {
        return this.app;
    }
}

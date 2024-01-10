package io.github.sekassel.uno.controller;

import io.github.sekassel.jfxframework.constructs.For;
import io.github.sekassel.jfxframework.controller.Subscriber;
import io.github.sekassel.jfxframework.controller.annotation.Controller;
import io.github.sekassel.jfxframework.controller.annotation.ControllerEvent;
import io.github.sekassel.jfxframework.controller.annotation.Param;
import io.github.sekassel.jfxframework.controller.annotation.SubController;
import io.github.sekassel.uno.App;
import io.github.sekassel.uno.Constants;
import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.model.Game;
import io.github.sekassel.uno.model.Player;
import io.github.sekassel.uno.service.GameService;
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

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller for the ingame screen.
 * Displays the game and handles the user input.
 * <p>
 * The controller annotation is required for the framework to recognize this class as a controller.
 * As there is no view specified, the framework will use the default file name which is located in the 'resources' folder
 * and has a name based on the class name (IngameController.class --> ingame.fxml).
 */
@Controller
public class IngameController implements Titleable {

    public static final String INGAME_SCREEN_TITLE = "Uno - In Game";

    private final BooleanProperty wildCardSelectedProperty = new SimpleBooleanProperty();
    private final BooleanProperty playersTurn = new SimpleBooleanProperty();
    private final BooleanProperty cardSelected = new SimpleBooleanProperty();

    HashMap<Card, CardController> cards = new HashMap<>();
    HashMap<Player, BotController> bots = new HashMap<>();

    // We're using dagger for injecting our dependencies
    @Inject
    App app;
    @Inject
    Subscriber subscriber;
    @Inject
    GameService gameService;
    @Inject
    Provider<BotController> botControllerProvider;
    @Inject
    Provider<CardController> cardControllerProvider;

    @SubController("button")
    @Inject
    ButtonController buttonController;

    @FXML
    private VBox colorSelectorBox;
    @FXML
    private HBox mainBox1;
    @FXML
    private HBox mainBox2;
    @FXML
    private HBox cardListHBox;
    @FXML
    private Button drawButton;
    @FXML
    private Button playButton;
    @FXML
    private Label playersTurnText;
    @FXML
    private Label directionIconLabel;


    private CardController currentCardController;

    @Param("game") // Fields annotated with @Param will be injected when the controller is initialized
    private Game game;

    @Inject
    public IngameController() {
        // The annotation @Inject is required for dagger to recognize this constructor as an injectable constructor
    }

    @Override
    public String getTitle() {
        return INGAME_SCREEN_TITLE;
    }

    @ControllerEvent.onInit
    public void init() {
        buttonController.setParentController(this);
    }

    // Since this method is annotated wth @ControllerEvent.onRender, it will be called when the controller is rendered
    @ControllerEvent.onRender
    public void render() {
        // Setup gui elements
        setupPropertyChangeListeners();
        renderBots();

        // Initialize the game
        this.gameService.initialize(this.game);

        // Render the cards of the player
        subscriber.addDestroyable(For.controller(this.cardListHBox, this.game.getFirstPlayer().getCards(), CardController.class, (controller, card) -> {
            controller.setCard(card);
            this.cards.put(card, controller);
            controller.subscriber.addDestroyable(() -> this.cards.remove(card));
        }).disposable());
    }

    /**
     * Sets up all the required listeners for displaying the game.
     */
    private void setupPropertyChangeListeners() {

        // Listener for displaying the direction of the game
        subscriber.listen(game.listeners(), Game.PROPERTY_CLOCKWISE, event ->
                this.directionIconLabel.setText(this.game.isClockwise() ? Constants.CLOCKWISE_ICON : Constants.COUNTER_CLOCKWISE_ICON)
        );

        // Listener for displaying the last played card in the center
        subscriber.listen(game.listeners(), Game.PROPERTY_CURRENT_CARD, event -> {
            Card newCard = (Card) event.getNewValue();
            if (newCard != null) {
                displayLastPlayed(newCard);
            }
        });


        // Listener for toggling the play/color picker buttons when a wild card is selected
        subscriber.listen(game.getFirstPlayer().listeners(), Player.PROPERTY_CURRENT_CARD, event ->
                this.wildCardSelectedProperty.set(event.getNewValue() != null && ((Card) event.getNewValue()).getColor() == CardColor.WILD)
        );

        // Listener for highlighting the selected card
        subscriber.listen(game.getFirstPlayer().listeners(), Player.PROPERTY_CURRENT_CARD, event -> {
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
        });


        // Listener for toggling the play/draw button when the player is at turn and selecting the playing bot
        subscriber.listen(game.listeners(), Game.PROPERTY_CURRENT_PLAYER, event -> {
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
                this.gameService.getBotService().startTurn(newPlayer);
            }

        });

        // Listener for displaying the winner if the player wins
        this.game.getFirstPlayer().listeners().addPropertyChangeListener(Player.PROPERTY_CARDS,
                event -> {
                    // Check if the player wins
                    if (game.getFirstPlayer().getCards().isEmpty()) {
                        displayWinner(game.getFirstPlayer());
                    }
                }
        );

        playersTurn.set(true);
        // Display the color selection when a wildcard has been selected and it's the player's turn
        subscriber.bind(colorSelectorBox.visibleProperty(), wildCardSelectedProperty.and(playersTurn));
        // Disable the play button if the player selected a wildcard, has no card selected or if it's not the player's turn
        subscriber.bind(playButton.disableProperty(), wildCardSelectedProperty.or(playersTurn.not()).or(cardSelected.not()));
        // Disable the draw button if it isn't the player's turn
        subscriber.bind(drawButton.disableProperty(), playersTurn.not());
        // Display the text if it's the player's turn
        subscriber.bind(playersTurnText.visibleProperty(), playersTurn);
    }

    /**
     * The method triggered by the quit button.
     * Display the setup screen again (with the currently used values as initial values).
     */
    @FXML
    public void onQuitPressed() {
        System.out.println("Quit");
        this.app.show("/", Map.of("initialBotAmount", this.game.getPlayers().size() - 1, "initialText", this.game.getFirstPlayer().getName()));
    }

    /**
     * The method triggered by the draw button.
     * Checks if it's the player's turn and then draws a new card.
     * If the card can be played, play it (if it's a wild card randomize the color).
     */
    @FXML
    public void onDrawPressed() {
        if (playersTurn.get()) {
            Card drawn = this.gameService.handoutCards(game.getPlayers().get(0), 1).get(0);
            if (drawn.canBeOnTopOf(game.getCurrentCard())) {
                playCard(drawn, drawn.getColor() == CardColor.WILD ? Utils.getRandomColor(this.gameService.getRandom()) : null);
            } else {
                this.gameService.selectNextPlayer(game, 1);
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
     * @param card  The card to play
     * @param color The color the card should become (not set if null)
     */
    private void playCard(Card card, CardColor color) {
        this.gameService.playRound(card, color);
    }

    /**
     * Creates bot controllers and renders them at the correct place.
     */
    private void renderBots() {
        List<Player> players = this.game.getPlayers();
        players.forEach(player -> {
            if (this.game.getFirstPlayer() == player)
                return;

            // Create a new controller for the bot. Since the controller isn't rendered by the framework, we have to initialize it manually using initAndRender.
            // Since the controller will persist for the whole lifetime of the game, we can let the framework handle the destruction of the controller.
            BotController botController = app.initAndRender(botControllerProvider.get(), Map.of("bot", player, "parent", this), true);

            bots.put(player, botController);

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
     * @param botNode The controller to be rendered
     * @param pane    Where the controller should be displayed
     * @param index   The index of the bot controller
     */
    private void renderBot(Parent botNode, Pane pane, int index) {
        pane.getChildren().remove(index);
        pane.getChildren().add(index, botNode);
    }

    /**
     * Displays a card as the last played card.
     *
     * @param card The card that has been played
     */
    public void displayLastPlayed(Card card) {

        // Destroy the controller of the last played card
        if (currentCardController != null) {
            // We have to destroy the controller manually, since it was rendered manually (see below)
            app.destroy(currentCardController);
        }

        // Remove the last played card if it exists
        if (mainBox2.getChildren().size() >= 3) {
            mainBox2.getChildren().remove(1);
        }

        // Create a new controller and initialize it, if there isn't already one
        if (!cards.containsKey(card)) {
            // Create a new controller for the card. Since the controller isn't rendered by the framework, we have to initialize it manually using initAndRender.
            // Since the controller won't persist for the whole lifetime of the game, we have to handle the destruction of the controller manually (see above).
            CardController controller = app.initAndRender(cardControllerProvider.get().setCard(card), false);
            cards.put(card, controller);
        }

        // Move the controller in the middle
        CardController newController = getControllerByCard(card);

        mainBox2.getChildren().add(1, newController);

        this.currentCardController = newController;

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
        // In order to show the winner screen, we have to start with a '/', since otherwise path traversal would lead us
        // to the controller at the route '/ingame/gameover', which isn't specified.
        this.app.show("/gameover", Map.of("winner", player));
    }

    @ControllerEvent.onDestroy
    public void destroy() {
        // Remove the card from the game
        this.subscriber.destroy();
    }


}

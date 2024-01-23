package io.github.sekassel.uno.util;

import io.github.sekassel.uno.Constants;

/**
 * Defines the (special) mechanic of a card.
 * Number cards don't need any special mechanic and can therefore be defined as a plain enum instance.
 * Cards with a special mechanic have an ActionRunnable which will be called.
 */
public enum CardType {

    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,

    REVERSE(Constants.COUNTER_CLOCKWISE_ICON, (card, service) -> {
        var game = card.getGame();
        game.setClockwise(!game.isClockwise());
        if (game.getPlayers().size() > 2) {
            service.selectNextPlayer(game, 1);
        } else if (game.getCurrentPlayer() != card.getGame().getFirstPlayer()) {
            service.selectNextPlayer(card.getGame(), 2);
        }
    }),

    SKIP(Constants.SKIP_ICON, ((card, service) -> service.selectNextPlayer(card.getGame(), 2))),

    DRAW_TWO(Constants.DRAW_TWO_ICON, ((card, service) -> {
        var receiver = service.getNextPlayer(card.getGame(), 1);
        service.handoutCards(receiver, 2);
        service.selectNextPlayer(card.getGame(), 2);
    })),

    WILD(Constants.WILD_ICON);

    private final String icon;
    private ActionRunnable action;

    CardType() {
        this.icon = String.valueOf(this.ordinal());
    }

    CardType(String icon) {
        this.icon = icon;
    }

    CardType(String icon, ActionRunnable action) {
        this.icon = icon;
        this.action = action;
    }

    @Deprecated
    public int getNumber() {
        return this.ordinal();
    }

    public String getIcon() {
        return this.icon;
    }

    public boolean hasAction() {
        return this.action != null;
    }

    public ActionRunnable getAction() {
        return this.action;
    }

}

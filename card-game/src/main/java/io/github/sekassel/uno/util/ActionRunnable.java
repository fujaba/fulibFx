package io.github.sekassel.uno.util;

import io.github.sekassel.uno.model.Card;
import io.github.sekassel.uno.service.GameService;

/**
 * Defines the behaviour of a card.
 * Every aspect of the game can be accessed through the card instance.
 * Every method can be accessed through the game service instance.
 */
@FunctionalInterface
public interface ActionRunnable {

    void run(Card card, GameService service);

}

package io.github.sekassel.uno.model;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.InitialValue;
import org.fulib.builder.reflect.Link;
import org.fulib.builder.reflect.Type;

import java.util.List;

public class GenModel implements ClassModelDecorator {

    public class Player {

        String name;

        @Link
        Card currentCard;

        @Link("players")
        Game game;

        @Link("owner")
        List<Card> cards;

    }

    public class Card {

        @InitialValue("CardType.ZERO")
        @Type("io.github.sekassel.uno.util.CardType")
        Object type;

        @InitialValue("CardColor.WILD")
        @Type("io.github.sekassel.uno.util.CardColor")
        Object color;

        @Link("cards")
        Player owner;

        @Link("cards")
        Game game;
    }

    public class Game {

        @InitialValue("true")
        boolean clockwise;

        @Link
        Player currentPlayer;

        @Link
        Card currentCard;

        @Link("game")
        List<Card> cards;

        @Link("game")
        List<Player> players;

    }

    @Override
    public void decorate(ClassModelManager mm) {
        mm.getClassModel().setDefaultPropertyStyle(org.fulib.builder.Type.BEAN);
        mm.haveNestedClasses(this.getClass());
    }
}

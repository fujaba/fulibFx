package de.uniks.ludo.model;

import org.fulib.builder.ClassModelDecorator;
import org.fulib.builder.ClassModelManager;
import org.fulib.builder.reflect.Link;

import java.util.List;

public class GenModel implements ClassModelDecorator {

    @Override
    public void decorate(ClassModelManager classModelManager) {
        classModelManager.haveNestedClasses(GenModel.class);
    }

    class Player {
        int id;

        @Link("players")
        Game game;

        @Link("owner")
        List<Piece> pieces;

        Field startField;

        @Link("owner")
        List<HomeField> homeFields;

        @Link("owner")
        List<GoalField> goalFields;
    }

    class Piece {
        @Link("piece")
        Field on;

        @Link("pieces")
        Player owner;
    }

    class Field {
        @Link("on")
        Piece piece;

        @Link("prev")
        Field next;

        @Link("next")
        Field prev;

        @Link("fields")
        Board board;

        int x;

        int y;
    }

    class HomeField extends Field {
        @Link("homeFields")
        Player owner;
    }

    class GoalField extends Field {
        @Link("goalFields")
        Player owner;
    }

    class Game {
        @Link("game")
        List<Player> players;

        Player currentPlayer;

        @Link("game")
        Board board;
    }

    class Board {
        @Link("board")
        List<Field> fields;

        @Link("board")
        Game game;
    }

}

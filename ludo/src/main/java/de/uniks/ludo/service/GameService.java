package de.uniks.ludo.service;

import de.uniks.ludo.Constants;
import de.uniks.ludo.model.*;
import javafx.util.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class GameService {

    @Inject
    public GameService() {
    }

    /**
     * Initializes the game by creating a board and adding the players to the game.
     *
     * @param playerAmount the amount of players
     * @return the created game
     */
    public Game createGame(int playerAmount) {

        Game game = new Game();

        for (int i = 0; i < playerAmount; i++) {
            game.withPlayers(new Player().setId(i + 1));
        }

        game.setCurrentPlayer(game.getPlayers().get(0));
        game.setBoard(createBoard(game));

        return game;

    }


    /**
     * Creates a new board for the game by creating fields with their corresponding positions and links.
     *
     * @return the created board
     */
    public Board createBoard(Game game) {
        Board board = new Board();

        // Create fields with the correct positions
        for (int i = 0; i < 40; i++) {

            Field field = new Field().setX(Constants.FIELD_COORDINATES.get(i).getKey()).setY(Constants.FIELD_COORDINATES.get(i).getValue());

            // Link the field with the previous field
            if (i > 0) {
                board.getFields().get(i - 1).setNext(field);
                field.setPrev(board.getFields().get(i - 1));
            }

            board.withFields(field);
        }

        // Link the last field with the first field
        board.getFields().get(39).setNext(board.getFields().get(0));

        for (Player player : game.getPlayers()) {
            // Set the start field for each player
            player.setStartField(board.getFields().get(((player.getId() - 1) * 10 - 1 + 40) % 40));

            // Set the home fields for each player
            for (int i = 0; i < 4; i++) {
                HomeField homeField = (HomeField) new HomeField()
                        .setX((player.getId() == 1 || player.getId() == 4 ? 0 : 9) + (i % 2))
                        .setY((player.getId() < 3 ? 0 : 9) + (i / 2));
                homeField.setOwner(player);
                board.withFields(homeField);

                new Piece().setOn(homeField).setOwner(player);

            }

            // Set the base fields for each player
            for (int i = 0; i < 4; i++) {
                Pair<Integer, Integer> baseCoordinates = Constants.BASE_COORDINATES.get((player.getId() - 1) * 4 + i);
                GoalField goalField = (GoalField) new GoalField()
                        .setX(baseCoordinates.getKey())
                        .setY(baseCoordinates.getValue());
                goalField.setOwner(player);
                board.withFields(goalField);
            }
        }

        return board;
    }


    public boolean stuck(Player player, int eyes) {
        return player.getPieces().stream().noneMatch(piece -> getTargetField(piece.getOn(), player, eyes).isPresent());
    }

    public Optional<Field> getTargetField(Field currentField, Player player, int eyes) {

        if (eyes < 1 || eyes > 6) {
            return Optional.empty();
        }

        Field field = currentField;

        // If the current field is a home field, the piece can only move if a 6 was rolled
        if (currentField instanceof HomeField) {
            if (eyes == 6) {
                return Optional.of(player.getStartField()); // The piece can move to the start field
            }
            return Optional.empty();
        }

        for (int i = 0; i < eyes; i++) {
            if (field instanceof GoalField) {
                int goalField = (player.getGoalFields().indexOf(field));
                if (eyes - i > player.getGoalFields().size() - (goalField + 1)) {
                    return Optional.empty();
                } else {
                    Field goal = player.getGoalFields().get(goalField + eyes - i);
                    return goal.getPiece() == null ? Optional.of(goal) : Optional.empty();
                }
            }

            if (field.getNext() == player.getStartField()) {
                field = player.getGoalFields().get(0);
            } else {
                field = field.getNext();
            }
        }

        if (field.getPiece() != null) {
            if (field.getPiece().getOwner() == player) {
                return Optional.empty();
            }
        }

        return Optional.of(field);
    }

    public void sendHome(Piece piece) {
        piece.getOwner().getHomeFields().stream().filter(homeField -> homeField.getPiece() == null).findFirst().ifPresent(piece::setOn);
    }

    public boolean hasPieceOut(Player player) {
        return player.getPieces().stream().anyMatch(piece -> !(piece.getOn() instanceof HomeField) && !(piece.getOn() instanceof GoalField));
    }

    public boolean isDone(Player player) {
        return player.getPieces().stream().allMatch(piece -> piece.getOn() instanceof GoalField);
    }

    public void nextPlayer(Game game) {
        int currentPlayerIndex = game.getPlayers().indexOf(game.getCurrentPlayer());
        Player nextPlayer = game.getPlayers().get((currentPlayerIndex + 1) % game.getPlayers().size());
        game.setCurrentPlayer(nextPlayer);
    }

    public boolean movePiece(Piece piece, Field targetField) {
        if (targetField.getPiece() != null) {
            sendHome(targetField.getPiece());
        }
        piece.setOn(targetField);
        return isDone(piece.getOwner());
    }
}

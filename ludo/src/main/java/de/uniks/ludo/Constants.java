package de.uniks.ludo;

import javafx.scene.media.Media;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Constants {

    public static final List<Pair<Integer, Integer>> FIELD_COORDINATES = List.of(

            new Pair<>(1, 4),
            new Pair<>(2, 4),
            new Pair<>(3, 4),

            new Pair<>(4, 4),

            new Pair<>(4, 3),
            new Pair<>(4, 2),
            new Pair<>(4, 1),

            new Pair<>(4, 0),
            new Pair<>(5, 0),
            new Pair<>(6, 0),

            new Pair<>(6, 1),
            new Pair<>(6, 2),
            new Pair<>(6, 3),

            new Pair<>(6, 4),

            new Pair<>(7, 4),
            new Pair<>(8, 4),
            new Pair<>(9, 4),

            new Pair<>(10, 4),
            new Pair<>(10, 5),
            new Pair<>(10, 6),

            new Pair<>(9, 6),
            new Pair<>(8, 6),
            new Pair<>(7, 6),

            new Pair<>(6, 6),

            new Pair<>(6, 7),
            new Pair<>(6, 8),
            new Pair<>(6, 9),

            new Pair<>(6, 10),
            new Pair<>(5, 10),
            new Pair<>(4, 10),

            new Pair<>(4, 9),
            new Pair<>(4, 8),
            new Pair<>(4, 7),

            new Pair<>(4, 6),

            new Pair<>(3, 6),
            new Pair<>(2, 6),
            new Pair<>(1, 6),

            new Pair<>(0, 6),
            new Pair<>(0, 5),
            new Pair<>(0, 4)
    );

    public static final List<Pair<Integer, Integer>> BASE_COORDINATES = List.of(
            new Pair<>(1, 5),
            new Pair<>(2, 5),
            new Pair<>(3, 5),
            new Pair<>(4, 5),

            new Pair<>(5, 1),
            new Pair<>(5, 2),
            new Pair<>(5, 3),
            new Pair<>(5, 4),

            new Pair<>(9, 5),
            new Pair<>(8, 5),
            new Pair<>(7, 5),
            new Pair<>(6, 5),

            new Pair<>(5, 9),
            new Pair<>(5, 8),
            new Pair<>(5, 7),
            new Pair<>(5, 6)
    );


    public static final Map<Integer, String> COLORS = Map.of(
            1, "red",
            2, "blue",
            3, "green",
            4, "orange"
    );

    public static final Media SOUND_ROLL_DICES = new Media(Objects.requireNonNull(LudoMain.class.getResource("sounds/rolling_dices.mp3")).toString());
    public static final Media SOUND_PLACE_PIECE = new Media(Objects.requireNonNull(LudoMain.class.getResource("sounds/place_piece.mp3")).toString());

}

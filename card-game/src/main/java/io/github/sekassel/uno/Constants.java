package io.github.sekassel.uno;

import javafx.scene.media.Media;


public class Constants {

    public static final int START_CARD_AMOUNT = 7;

    public static final int MAX_NAME_LENGTH = 16;
    public static final String BOT_NAME = "Bot %s";

    public static final int BOT_PLAY_DELAY = 2000;

    public static final String CARD_FONT_FAMILY = "System";
    public static final String BOT_ICON_FONT_FAMILY = "Cooper Black";
    public static final int CARD_FONT_SIZE = 64;

    public static final String CARD_STYLE = "-fx-border-radius: 10px; -fx-border-color: BLACK; -fx-border-width: 10px; -fx-background-color: %s; -fx-background-radius: 16px; -fx-background-insets: 3px;";

    public static final Media SOUND_CLICK = new Media(Main.class.getResource("sound/click.mp3").toString());
    public static final Media SOUND_FAIL = new Media(Main.class.getResource("sound/fail.mp3").toString());

    public static final String COUNTER_CLOCKWISE_ICON = "\u21BA";
    public static final String CLOCKWISE_ICON = "\u21BB";
    public static final String WILD_ICON = "\u2605";
    public static final String SKIP_ICON = "\u2A02";
    public static final String DRAW_TWO_ICON = "+2";
}

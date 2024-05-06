package de.uniks.ludo;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class LudoUtil {

    private static final String CONTROLLER_TEST = "controller.test.enabled";

    public static boolean inControllerTest() {
        return Boolean.parseBoolean(System.getProperty(CONTROLLER_TEST));
    }

    public static void enableControllerTest() {
        System.setProperty(CONTROLLER_TEST, Boolean.toString(true));
    }

    /**
     * Plays a media/sound.
     *
     * @param sound The sound to be played
     */
    public static void playSound(Media sound) {
        try {
            new MediaPlayer(sound).play();
        } catch (IllegalStateException e) {
            System.err.println("Tried to play sound whilst game was in test mode.");
        }
    }

}

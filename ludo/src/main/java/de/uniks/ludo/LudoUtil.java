package de.uniks.ludo;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class LudoUtil {

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

package io.github.sekassel.uno.util;

import io.github.sekassel.uno.Constants;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;

public class Utils {

    /**
     * Replaces all illegal characters in a String
     *
     * @param name The string which should be cleared of illegal items
     * @return The string with all illegal characters replaced
     */
    public static String replaceIllegals(String name) {
        return name.replaceAll("[/\\\\:*?\"<>|]", "x"); // Needs 4 backslashes since it's a regex and java escape char
    }

    /**
     * Trims a string to a certain length.
     * @param toTrim The string to trim.
     * @param size The size to trim the string to.
     * @return The trimmed string.
     */
    public static String trim(String toTrim, int size) {
        return toTrim.length() < size ? toTrim : toTrim.substring(0, size);
    }

    /**
     * Generates a random color by a given random instance.
     * Every color except the last one (wild) can be selected.
     *
     * @param random The instance to use for the random selection
     * @return A random color
     */
    public static CardColor getRandomColor(Random random) {
        return CardColor.values()[random.nextInt(CardColor.values().length - 1)];
    }

    public static String getCardStyle(CardColor color) {
        return Constants.CARD_STYLE.formatted(color.getColorString());
    }

    /**
     * Generates a random instance by loading a seed from a file.
     * If no file is found or the seed in the file is invalid, generate an instance without a seed.
     *
     * @return The generated random instance
     */
    public static Random getRandomBySeedFile() {
        try {
            File file = new File("seed.txt");
            if (file.exists())
                return new Random(Integer.parseInt(Files.readString(file.toPath())));
        } catch (IOException | NumberFormatException exception) {
            System.out.println("Couldn't read seed from 'seed.txt'.");
        }
        return new Random();
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

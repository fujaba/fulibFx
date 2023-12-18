package io.github.sekassel.uno.util;

/**
 * Defines the color a card can have.
 * If no color string is defined, it will use the enum's name as the color.
 */
public enum CardColor {

    RED,
    BLUE,
    GREEN,
    YELLOW,
    WILD("GRAY");

    private final String color;

    CardColor(String color) {
        this.color = color;
    }

    CardColor() {
        this.color = this.name();
    }

    public String getColorString() {
        return this.color;
    }

}

package de.beusterse.abfalllro;


/**
 * Holds a single trash can
 *
 * Created by Felix Beuster
 */
public class Can {

    public static String COLOR_BLACK = "black";
    public static String COLOR_BLUE = "blue";
    public static String COLOR_GREEN = "green";
    public static String COLOR_YELLOW = "yellow";

    private boolean monthly;
    private String color;
    private char letter;

    public Can(boolean monthly, String color, char letter) {
        this.monthly = monthly;
        this.color = color;
        this.letter = letter;
    }

    public String getColor() {
        return color;
    }

    public char getLetter() {
        return letter;
    }

    public boolean isMonthly() { return monthly; }

    public String toString() {
        return (monthly ? "2w" : "4w") + " " + color + " " + letter;
    }
}

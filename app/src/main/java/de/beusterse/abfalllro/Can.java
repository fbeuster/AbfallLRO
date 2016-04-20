package de.beusterse.abfalllro;


/**
 * Holds a single trash can
 *
 * Created by Felix Beuster
 */
public class Can {

    public static final int INVALID = -1;
    public static final int BLACK   = 0;
    public static final int BLUE    = 1;
    public static final int GREEN   = 2;
    public static final int YELLOW  = 3;

    private boolean monthly;
    private char letter;
    private int color;

    public Can(boolean monthly, int color, char letter) {
        this.monthly = monthly;
        this.color = color;
        this.letter = letter;
    }

    public int getColor() {
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

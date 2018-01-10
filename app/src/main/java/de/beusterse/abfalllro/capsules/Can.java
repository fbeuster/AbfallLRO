package de.beusterse.abfalllro.capsules;


import de.beusterse.abfalllro.utils.CanUtils;

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

    public static final int SCHEDULE_NEVER          = 0;
    public static final int SCHEDULE_MONTHLY        = 1;
    public static final int SCHEDULE_BIWEEKLY       = 2;
    public static final int SCHEDULE_WEEKLY         = 3;
    public static final int SCHEDULE_TWICA_A_WEEK   = 4;

    private char letter;
    private int color;
    private int schedule;

    public Can(int schedule, int color, char letter) {
        this.schedule = schedule;
        this.color = color;
        this.letter = letter;
    }

    public int getColor() {
        return color;
    }

    public char getLetter() {
        return letter;
    }

    public int getSchedule() { return schedule; }

    public String toString() {
        return CanUtils.scheduleIntToString(schedule) + " " + color + " " + letter;
    }
}

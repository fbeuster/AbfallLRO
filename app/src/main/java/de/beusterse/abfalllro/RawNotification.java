package de.beusterse.abfalllro;

import android.app.PendingIntent;

/**
 * Holds the data to make a notification.
 *
 * Created by Felix Beuster
 */
public class RawNotification {

    public static final int INVALID_CAN = -1;

    public static final int BLACK_CAN   = 0;
    public static final int BLUE_CAN    = 1;
    public static final int GREEN_CAN   = 2;
    public static final int YELLOW_CAN  = 3;

    private int can;
    private PendingIntent intent;

    public RawNotification(int can, PendingIntent intent) {
        this.can    = can;
        this.intent = intent;
    }

    public CharSequence getText() {
        return "Morgen Abholung.";
    }

    public CharSequence getTitle() {
        String[] titles = { "Graue Tonne", "Blaue Tonne",
                            "Grüne Tonne", "Gelbe Tonne"};
        return titles[can];
    }

    public int getColor() {
        int[] colors = {R.color.can_black, R.color.can_blue,
                        R.color.can_green, R.color.can_yellow};
        return colors[can];
    }

    public int getColoredIcon() {
        int[] icons = { R.drawable.can_black_notification, R.drawable.can_blue_notification,
                        R.drawable.can_green_notification, R.drawable.can_yellow_notification};
        return icons[can];
    }

    public int getIcon() {
        return R.drawable.can_material_notification;
    }

    public int getUniqueId() {
        return can;
    }

    public PendingIntent getIntent() {
        return intent;
    }
}

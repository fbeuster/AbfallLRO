package de.beusterse.abfalllro.capsules;

import android.app.PendingIntent;
import android.content.res.Resources;

import de.beusterse.abfalllro.R;

/**
 * Holds the data to make a notification.
 *
 * Created by Felix Beuster
 */
public class RawNotification {

    private int can;
    private PendingIntent intent;
    private Resources resources;

    public RawNotification(int can, PendingIntent intent, Resources resources) {
        this.can        = can;
        this.intent     = intent;
        this.resources  = resources;
    }

    public CharSequence getText() {
        return resources.getString(R.string.notification_text_line);
    }

    public CharSequence getTitle() {
        return resources.getStringArray(R.array.can_names_upper)[can];
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

package de.beusterse.abfalllro.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import de.beusterse.abfalllro.BuildConfig;
import de.beusterse.abfalllro.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Utility functions for notifications
 *
 * Created by Felix Beuster on 1/29/2019.
 */
public class NotificationUtils {

    public static final long[] vibrate_pattern = new long[]{ 31, 415, 92, 653 };

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    context.getString(R.string.notification_channel_id),
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW);

            channel.enableLights(true);
            if (BuildConfig.FLAVOR == "dev") {
                channel.setLightColor(R.color.can_yellow);

            } else {
                channel.setLightColor(R.color.can_blue);
            }

            int vibratePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE);
            channel.enableVibration(true);

            if (vibratePermission == PackageManager.PERMISSION_GRANTED) {
                channel.setVibrationPattern(vibrate_pattern);
            }

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

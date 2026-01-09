package de.beusterse.abfalllro.service;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.content.ContextCompat;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.activities.MainActivity;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.RawNotification;
import de.beusterse.abfalllro.utils.NotificationUtils;

/**
 * Created by Felix Beuster on 1/12/2019.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTIFY_CAN = "de.beusterse.abfalllro.EXTRA_NOTIFY_CAN";

    private Context context;
    private NotificationManager notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        int can = intent.getIntExtra(EXTRA_NOTIFY_CAN, Can.INVALID);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        showNotification(can, pref);
    }

    private void showNotification(int can, SharedPreferences pref) {
        if (can == Can.INVALID) {
            return;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), getPendingIntentFlagForVersionS(0));
        RawNotification rawNotification = new RawNotification(can, pendingIntent, context.getResources());
        Notification notification = createNotification(rawNotification, pref);

        notificationManager.notify(rawNotification.getUniqueId(), notification);
    }

    private Notification createNotification(RawNotification rawNotification, SharedPreferences pref) {
        Notification.Builder notificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(context, context.getString(R.string.notification_channel_id));

        } else {
            /* SDK_INT < 26 */
            notificationBuilder = new Notification.Builder(context);

            boolean soundsActive = pref.getBoolean(
                    context.getString(R.string.pref_key_notifications_sound),
                    context.getResources().getBoolean(R.bool.notifications_sound_default));

            if (soundsActive) {
                Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notificationBuilder.setSound(sound);
            }

            int vibratePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.VIBRATE);
            boolean vibrationsActive = pref.getBoolean(
                    context.getString(R.string.pref_key_notifications_vibrate),
                    context.getResources().getBoolean(R.bool.notifications_vibrate_default) );

            if (vibrationsActive && vibratePermission == PackageManager.PERMISSION_GRANTED) {
                notificationBuilder.setVibrate(NotificationUtils.vibrate_pattern);
            }
        }

        notificationBuilder
                .setAutoCancel(true)
                .setContentIntent(rawNotification.getIntent())
                .setContentTitle(rawNotification.getTitle())
                .setColor(getNotificationColor(rawNotification))
                .setContentText(rawNotification.getText())
                .setSmallIcon(getNotificationIcon(rawNotification))
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            notificationBuilder.setShowWhen(true);
        }

        return notificationBuilder.build();
    }

    private int getPendingIntentFlagForVersionS(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_IMMUTABLE | flags;
        }

        return flags;
    }

    private int getNotificationColor(RawNotification rawNotification) {
        return context.getResources().getColor(rawNotification.getColor(), context.getTheme());
    }

    private int getNotificationIcon(RawNotification rawNotification) {
        return rawNotification.getIcon();
    }
}

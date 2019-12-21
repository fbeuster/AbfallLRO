package de.beusterse.abfalllro.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;

import de.beusterse.abfalllro.activities.MainActivity;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.RawNotification;
import de.beusterse.abfalllro.utils.NotificationUtils;

/**
 * Creates an notification based on passed in parameters
 *
 * Created by Felix Beuster
 */
public class NotificationService extends Service {

    private NotificationManager notificationManager;

    public static final String EXTRA_INTENT_NOTIFY  = "de.beusterse.abfalllro.EXTRA_INTENT_NOTIFY";
    public static final String EXTRA_NOTIFY_CAN     = "de.beusterse.abfalllro.EXTRA_NOTIFY_CAN";

    public class ServiceBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }


    @Override
    public void onCreate() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra(EXTRA_INTENT_NOTIFY, false)) {
            int can = intent.getIntExtra(EXTRA_NOTIFY_CAN, Can.INVALID);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

            showNotification(can, pref);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ServiceBinder();

    private int getNotificationColor(RawNotification rawNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(rawNotification.getColor(), getTheme());

        } else {
            return getResources().getColor(rawNotification.getColor());
        }
    }

    private int getNotificationIcon(RawNotification rawNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return rawNotification.getIcon();
        } else {
            return rawNotification.getColoredIcon();
        }
    }

    private void showNotification(int can, SharedPreferences pref) {
        if (can != Can.INVALID) {
            Notification notification;
            Notification.Builder notificationBuilder;
            PendingIntent pendingIntent     = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            RawNotification rawNotification = new RawNotification(can, pendingIntent, getResources());

            boolean soundsActive        = pref.getBoolean(
                    getString(R.string.pref_key_notifications_sound),
                    getResources().getBoolean(R.bool.notifications_sound_default));

            boolean vibrationsActive    = pref.getBoolean(
                    getString(R.string.pref_key_notifications_vibrate),
                    getResources().getBoolean(R.bool.notifications_vibrate_default) );

            int vibratePermission       = ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE);

            Uri sound                   = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationBuilder = new Notification.Builder(this, getString(R.string.notification_channel_id));
                notificationBuilder.setColor( getNotificationColor(rawNotification) );

            } else {
                /* SDK_INT < 26 */
                notificationBuilder = new Notification.Builder(this);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notificationBuilder.setColor( getNotificationColor(rawNotification) );
                }

                if (soundsActive) {
                    notificationBuilder.setSound(sound);
                }

                if (vibrationsActive && vibratePermission == PackageManager.PERMISSION_GRANTED) {
                    notificationBuilder.setVibrate(NotificationUtils.vibrate_pattern);
                }
            }

            notificationBuilder
                    .setAutoCancel(true)
                    .setContentIntent(rawNotification.getIntent())
                    .setContentTitle(rawNotification.getTitle())
                    .setContentText(rawNotification.getText())
                    .setSmallIcon(getNotificationIcon(rawNotification))
                    .setWhen(System.currentTimeMillis());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                notificationBuilder.setShowWhen(true);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                notification = notificationBuilder.build();
            } else {
                notification = notificationBuilder.getNotification();
            }

            notificationManager.notify(rawNotification.getUniqueId(), notification);
        }
    }
}

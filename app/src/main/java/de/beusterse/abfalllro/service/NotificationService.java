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
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

import de.beusterse.abfalllro.activities.MainActivity;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.capsules.RawNotification;

/**
 * Creates an notification based on passed in parameters
 *
 * Created by Felix Beuster
 */
public class NotificationService extends Service {

    private NotificationManager notificationManager;

    public static final String EXTRA_INTENT_NOTIFY  = "de.beusterse.abfalllro.EXTRA_INTENT_NOTIFY";
    public static final String EXTRA_NOTIFY_CAN     = "de.beusterse.abfalllro.EXTRA_NOTIFY_CAN";

    private long[] vibrate_pattern = new long[]{ 31, 415, 92, 653 };

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
//            saveLastAlarmTime(can, pref);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ServiceBinder();

    private int getNotificationColor(RawNotification rawNotification) {
        if (Build.VERSION.SDK_INT >= 23) {
            return getResources().getColor(rawNotification.getColor(), getTheme());

        } else {
            return getResources().getColor(rawNotification.getColor());
        }
    }

    private int getNotificationIcon(RawNotification rawNotification) {
        if (Build.VERSION.SDK_INT >= 21) {
            return rawNotification.getIcon();
        } else {
            return rawNotification.getColoredIcon();
        }
    }

    private void saveLastAlarmTime(int can, SharedPreferences pref) {
        SharedPreferences.Editor editor = pref.edit();

        Calendar date = Calendar.getInstance();
        date.getTimeInMillis();

        switch (can) {
            case Can.BLACK:
                editor.putLong(getString(R.string.pref_key_intern_last_alarm_black), date.getTimeInMillis());
                break;
            case Can.BLUE:
                editor.putLong(getString(R.string.pref_key_intern_last_alarm_blue), date.getTimeInMillis());
                break;
            case Can.GREEN:
                editor.putLong(getString(R.string.pref_key_intern_last_alarm_green), date.getTimeInMillis());
                break;
            case Can.YELLOW:
                editor.putLong(getString(R.string.pref_key_intern_last_alarm_yellow), date.getTimeInMillis());
                break;
            default:
                break;
        }

        editor.apply();
    }

    private void showNotification(int can, SharedPreferences pref) {
        if (can != Can.INVALID) {
            Notification notification;
            PendingIntent pendingIntent     = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            RawNotification rawNotification = new RawNotification(can, pendingIntent, getResources());

            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setContentIntent(rawNotification.getIntent())
                    .setContentTitle(rawNotification.getTitle())
                    .setContentText(rawNotification.getText())
                    .setSmallIcon(getNotificationIcon(rawNotification));

            if (Build.VERSION.SDK_INT >= 21) {
                notificationBuilder.setColor( getNotificationColor(rawNotification) );
            }

            /* notification sounds */
            boolean soundsActive = pref.getBoolean( getString(R.string.pref_key_notifications_sound),
                                                    getResources().getBoolean(R.bool.notifications_vibrate_default) );

            if (soundsActive) {
                Uri sound = RingtoneManager.getDefaultUri( RingtoneManager.TYPE_NOTIFICATION );
                notificationBuilder.setSound( sound );
            }

            /* notification vibrations */
            boolean vibrationsActive = pref.getBoolean( getString(R.string.pref_key_notifications_vibrate),
                                                        getResources().getBoolean(R.bool.notifications_vibrate_default) );

            if (vibrationsActive) {
                int vibratePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE);

                if (vibratePermission == PackageManager.PERMISSION_GRANTED) {
                    notificationBuilder.setVibrate(vibrate_pattern);
                }
            }

            if (Build.VERSION.SDK_INT >= 16) {
                notification = notificationBuilder.build();
            } else {
                notification = notificationBuilder.getNotification();
            }

            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(rawNotification.getUniqueId(), notification);
        }
    }
}

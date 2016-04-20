package de.beusterse.abfalllro.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;

import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.DataLoader;
import de.beusterse.abfalllro.MainActivity;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.RawNotification;
import de.beusterse.abfalllro.TimePreference;
import de.beusterse.abfalllro.TrashController;

/**
 * Created by Felix Beuster
 */
public class NotifyService extends Service {

    private NotificationManager notificationManager;

    public static final String EXTRA_INTENT_NOTIFY  = "de.beusterse.abfalllro.EXTRA_INTENT_NOTIFY";
    public static final String EXTRA_NOTIFY_CAN     = "de.beusterse.abfalllro.EXTRA_NOTIFY_CAN";

    private long[] vibrate_pattern = new long[]{ 31, 415, 92, 653 };

    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
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
            setNewAlarm(can, pref);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ServiceBinder();

    private int getDayOffset(SharedPreferences pref, int can) {
        DataLoader loader           = new DataLoader(pref, getResources(), getPackageName());
        TrashController controller  = new TrashController(pref, loader.getCode(), loader.getSchedule());

        // getPreview() has the day diff for the current can, for the day this alarm was for.
        // offset is added to get the next batch
        int[] currentPreview    = controller.getPreview();
        int[] nextPreview       = controller.getNextPreview( currentPreview[can] + 1);

        return currentPreview[can] + nextPreview[can];
    }

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

    private void setNewAlarm(int can, SharedPreferences pref) {
        // clean up the alarm that just went off
        new AlarmTask(this, Calendar.getInstance(),can).cancel();

        Calendar nextDate   = Calendar.getInstance();
        String alarmTime    = pref.getString(   getString(R.string.pref_key_notifications_time),
                                                getString(R.string.pref_notifications_default_time));
        int alarmHour       = TimePreference.getHour(alarmTime);
        int alarmMinute     = TimePreference.getMinute(alarmTime);

        nextDate.add(Calendar.DATE, getDayOffset(pref, can));
        nextDate.set(Calendar.HOUR_OF_DAY, alarmHour);
        nextDate.set(Calendar.MINUTE, alarmMinute);

        new AlarmTask(this, nextDate, can).run();
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

        stopSelf();
    }
}

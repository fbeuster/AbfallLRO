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
import android.util.Log;

import java.util.Calendar;

import de.beusterse.abfalllro.DataLoader;
import de.beusterse.abfalllro.MainActivity;
import de.beusterse.abfalllro.RawNotification;
import de.beusterse.abfalllro.TimePreference;
import de.beusterse.abfalllro.TrashController;

/**
 * Created by Felix Beuster
 */
public class NotifyService extends Service {

    public class ServiceBinder extends Binder {
        NotifyService getService() {
            return NotifyService.this;
        }
    }

    public static final String INTENT_NOTIFY = "de.beusterse.abfalllro.INTENT_NOTIFY";
    public static final String NOTIFY_CAN = "de.beusterse.abfalllro.NOTIFY_CAN";
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        Log.i("NotifyService", "onCreate()");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);

        if (intent.getBooleanExtra(INTENT_NOTIFY, false)) {
            int can = intent.getIntExtra(NOTIFY_CAN, RawNotification.INVALID_CAN);
            showNotification(can);
            setNewAlarm(can);
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

    private void setNewAlarm(int can) {
        // clean up the alarm that just went off
        new AlarmTask(this, Calendar.getInstance(),can).cancel();

        Calendar nextDate       = Calendar.getInstance();
        SharedPreferences pref  = PreferenceManager.getDefaultSharedPreferences(this);

        String alarmTime    = pref.getString("pref_notifications_time", "18:00");
        int alarmHour       = TimePreference.getHour(alarmTime);
        int alarmMinute     = TimePreference.getMinute(alarmTime);

        nextDate.add(Calendar.DATE, getDayOffset(pref, can));
        nextDate.set(Calendar.HOUR_OF_DAY, alarmHour);
        nextDate.set(Calendar.MINUTE, alarmMinute);

        new AlarmTask(this, nextDate, can).run();
    }

    private void showNotification(int can) {
        if (can != RawNotification.INVALID_CAN) {
            Notification notification;
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            RawNotification rawNotification = new RawNotification(can, pendingIntent);

            Notification.Builder notificationBuilder = new Notification.Builder(this)
                    .setContentIntent(rawNotification.getIntent())
                    .setContentTitle(rawNotification.getTitle())
                    .setContentText(rawNotification.getText())
                    .setSmallIcon(getNotificationIcon(rawNotification));

            if (Build.VERSION.SDK_INT >= 21) {
                notificationBuilder.setColor(getNotificationColor(rawNotification));
            }

            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_notifications_vibrate", true)) {
                int vibratePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.VIBRATE);
                if (vibratePermission == PackageManager.PERMISSION_GRANTED) {
                    notificationBuilder.setVibrate(new long[] {31, 415, 92, 653});
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
}

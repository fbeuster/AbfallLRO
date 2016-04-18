package de.beusterse.abfalllro.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;

import de.beusterse.abfalllro.MainActivity;
import de.beusterse.abfalllro.RawNotification;

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

            new AlarmTask(this, Calendar.getInstance(),can).cancel();
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ServiceBinder();

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

package de.beusterse.abfalllro.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.beusterse.abfalllro.capsules.Can;

/**
 * Created by Felix Beuster on 1/12/2019.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context.getApplicationContext(), NotificationService.class);
        serviceIntent.putExtra(NotificationService.EXTRA_INTENT_NOTIFY, intent.getBooleanExtra(NotificationService.EXTRA_INTENT_NOTIFY, true));
        serviceIntent.putExtra(NotificationService.EXTRA_NOTIFY_CAN, intent.getIntExtra(NotificationService.EXTRA_NOTIFY_CAN, Can.INVALID));

        context.startService(serviceIntent);
    }
}

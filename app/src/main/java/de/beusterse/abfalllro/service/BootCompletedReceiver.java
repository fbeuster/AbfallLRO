package de.beusterse.abfalllro.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Reciever for the BOOT_COMPLETE event.
 *
 * Created by Felix Beuster
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BootAlarmService.class));
    }
}

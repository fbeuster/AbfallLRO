package de.beusterse.abfalllro.service.legacy;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Receives the daily alarm and invokes the check service
 *
 * Created by Felix Beuster
 */

public class DailyCheckReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, DailyCheckService.class);
        startWakefulService(context, service);
    }
}

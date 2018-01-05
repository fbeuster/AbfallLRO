package de.beusterse.abfalllro.service;

import android.app.IntentService;
import android.content.Intent;

import de.beusterse.abfalllro.interfaces.DailyCheckCallback;

/**
 * Performs the daily check whether a can is due in the next days or not.
 * Schedules alarm for notification if needed.
 *
 * Created by Felix Beuster
 */

public class DailyCheckService extends IntentService implements DailyCheckCallback {


    private Intent mIntent;

    public DailyCheckService() {
        super("DailyCheckService");
    }

    /**
     * Entrypoint when service is called.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;

        DailyCheck dailyCheck = new DailyCheck(this, this);
        dailyCheck.run();
    }

    @Override
    public void dailyCheckComplete() {
        DailyCheckReceiver.completeWakefulIntent(mIntent);
    }
}

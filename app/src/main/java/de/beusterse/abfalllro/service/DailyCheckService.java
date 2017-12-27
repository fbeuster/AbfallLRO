package de.beusterse.abfalllro.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.beusterse.abfalllro.DataLoader;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.TimePreference;
import de.beusterse.abfalllro.TrashController;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.interfaces.SyncCallback;

/**
 * Performs the daily check whether a can is due in the next days or not.
 * Schedules alarm for notification if needed.
 *
 * Created by Felix Beuster
 */

public class DailyCheckService extends IntentService implements SyncCallback {

    private int[] canAlarmTimes = null;
    private int[] canAlarmTypes = { Can.BLACK, Can.BLUE,
                                    Can.GREEN, Can.YELLOW};

    private Intent mIntent;
    private SharedPreferences pref;
    private SyncClient mSyncClient;

    public DailyCheckService() {
        super("DailyCheckService");
    }

    private boolean alarmWentOff(Calendar cal, int can) {
        long lastAlarm      = getLastAlarmTime(can);
        Calendar now        = Calendar.getInstance();
        Calendar lastCal    = Calendar.getInstance();
        lastCal.setTimeInMillis(lastAlarm);

        return lastAlarm > 0 && lastCal.get(Calendar.DATE) == cal.get(Calendar.DATE) && lastCal.before(now);
    }

    @Override
    public void finishDownloading() {
        mSyncClient.finishDownloading();
    }

    private long getLastAlarmTime(int can) {
        switch (can) {
            case Can.BLACK:
                return pref.getLong(this.getString(R.string.pref_key_intern_last_alarm_black), 0);
            case Can.BLUE:
                return pref.getLong(this.getString(R.string.pref_key_intern_last_alarm_blue), 0);
            case Can.GREEN:
                return pref.getLong(this.getString(R.string.pref_key_intern_last_alarm_green), 0);
            case Can.YELLOW:
                return pref.getLong(this.getString(R.string.pref_key_intern_last_alarm_yellow), 0);
            default:
                return 0;
        }
    }

    /**
     * Gets information about the current network connection.
     *
     * @return NetworkInfo
     */
    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    private void getPreview() {
        DataLoader loader           = new DataLoader(this);
        TrashController controller  = new TrashController(pref, loader, getResources());

        canAlarmTimes = controller.getPreview();
    }

    /**
     * Entrypoint when service is called.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent     = intent;
        mSyncClient = new SyncClient(this, "daily_check");
        pref        = PreferenceManager.getDefaultSharedPreferences(this);

        Calendar now            = Calendar.getInstance();
        SimpleDateFormat df     = new SimpleDateFormat("yyyy-MM-dd");
        String lasDailyCheck    = pref.getString(getResources().getString(R.string.pref_key_intern_last_daily_check), "");

        if (!df.format(now.getTime()).equals(lasDailyCheck)) {
            mSyncClient.run();

        } else {
            DailyCheckReceiver.completeWakefulIntent(mIntent);
        }
    }

    /**
     * Progress update from the sync process.
     *
     * @param progressCode must be one of the constants defined in DownloadCallback.Progress.
     * @param percentComplete must be 0-100.
     */
    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        mSyncClient.onProgressUpdate(progressCode, percentComplete);
    }

    /**
     * Saces the current date as last checked date.
     */
    private void saveLastDailyCheckDate() {
        Calendar now                    = Calendar.getInstance();
        SimpleDateFormat df             = new SimpleDateFormat("yyyy-MM-dd");
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(getResources().getString(R.string.pref_key_intern_last_daily_check), df.format(now.getTime()));
        editor.apply();
    }

    private void scheduleNotification() {

        String alarmTime    = pref.getString(   this.getString(R.string.pref_key_notifications_time),
                this.getString(R.string.pref_notifications_default_time));
        int alarmHour       = TimePreference.getHour(alarmTime);
        int alarmMinute     = TimePreference.getMinute(alarmTime);

        for (int i = 0; i < canAlarmTypes.length; i++) {

            if (canAlarmTimes[i] > 0 && canAlarmTimes[i] <= 2) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, canAlarmTimes[i] - 1);
                cal.set(Calendar.HOUR_OF_DAY, alarmHour);
                cal.set(Calendar.MINUTE, alarmMinute);

                Calendar now = Calendar.getInstance();

                if (now.before(cal) || cal.before(now) && !alarmWentOff(cal, i)) {
                    /* cancel alarm to make sure all current settings are applied */
                    if (new NotificationAlarmTask(this, Calendar.getInstance(), canAlarmTypes[i]).exists()) {
                        new NotificationAlarmTask(this, Calendar.getInstance(), canAlarmTypes[i]).cancel();
                    }

                    new NotificationAlarmTask(this, cal, canAlarmTypes[i]).run();
                }
            }
        }
    }

    /**
     * Callback when sync is complete.
     */
    @Override
    public void syncComplete() {
        getPreview();
        scheduleNotification();
        saveLastDailyCheckDate();

        DailyCheckReceiver.completeWakefulIntent(mIntent);
    }

    /**
     * Callback for an update from the download.
     *
     * @param result
     */
    @Override
    public void updateFromDownload(Object result) {
        mSyncClient.updateFromDownload(result);
    }
}

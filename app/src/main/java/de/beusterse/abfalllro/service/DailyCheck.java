package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.capsules.Can;
import de.beusterse.abfalllro.controller.DataController;
import de.beusterse.abfalllro.controller.SyncController;
import de.beusterse.abfalllro.controller.TrashController;
import de.beusterse.abfalllro.interfaces.DailyCheckCallback;
import de.beusterse.abfalllro.interfaces.SyncCallback;
import de.beusterse.abfalllro.utils.TimePreference;

/**
 * Daily Check
 *
 * Starts the sync to check for new data, and also checks
 * if any reminders need to be set.
 *
 * Created by Felix Beuster on 1/4/2018.
 */

public class DailyCheck implements SyncCallback {

    public static int INTERVAL          = 86400000;
    public static int INTERVAL_DEBUG    = 900000;

    private final Context mContext;
    private final DailyCheckCallback mDailyCheckCallback;
    private int[] canAlarmTimes = null;
    private int[] canAlarmTypes = { Can.BLACK, Can.BLUE,
            Can.GREEN, Can.YELLOW};

    private SharedPreferences pref;
    private SyncController mSyncController;

    public DailyCheck(Context context, DailyCheckCallback dailyCheckCallback) {
        mDailyCheckCallback = dailyCheckCallback;
        mContext            = context;
    }

    private boolean alarmWentOff(Calendar cal, int can) {
        long lastAlarm      = getLastAlarmTime(can);
        Calendar now        = Calendar.getInstance();
        Calendar lastCal    = Calendar.getInstance();
        lastCal.setTimeInMillis(lastAlarm);

        return lastAlarm > 0 && lastCal.get(Calendar.DATE) == cal.get(Calendar.DATE) && lastCal.before(now);
    }

    private long getLastAlarmTime(int can) {
        switch (can) {
            case Can.BLACK:
                return pref.getLong(mContext.getString(R.string.pref_key_intern_last_alarm_black), 0);
            case Can.BLUE:
                return pref.getLong(mContext.getString(R.string.pref_key_intern_last_alarm_blue), 0);
            case Can.GREEN:
                return pref.getLong(mContext.getString(R.string.pref_key_intern_last_alarm_green), 0);
            case Can.YELLOW:
                return pref.getLong(mContext.getString(R.string.pref_key_intern_last_alarm_yellow), 0);
            default:
                return 0;
        }
    }

    private void getPreview() {
        DataController loader       = new DataController(mContext);
        TrashController controller  = new TrashController(mContext, loader);

        canAlarmTimes = controller.getPreview();
    }

    public void run() {
        pref                    = PreferenceManager.getDefaultSharedPreferences(mContext);
        Calendar now            = Calendar.getInstance();
        SimpleDateFormat df     = new SimpleDateFormat("yyyy-MM-dd");
        String lastDailyCheck   = pref.getString(mContext.getResources().getString(R.string.pref_key_intern_last_daily_check), "");

        if (!df.format(now.getTime()).equals(lastDailyCheck)) {
            if (pref.getBoolean(
                    mContext.getString(R.string.pref_key_sync_auto),
                    mContext.getResources().getBoolean(R.bool.sync_auto))) {
                mSyncController = new SyncController(mContext, "daily_check", this);
                mSyncController.run();

            } else {
                // sync disabled, just take care of notifications
                getPreview();
                scheduleNotification();
                saveLastDailyCheckDate();

                mDailyCheckCallback.dailyCheckComplete();
            }

        } else {
            // Daily check did run already this day, but could be running again (e.g. reboot)
            getPreview();
            scheduleNotification();
            saveLastDailyCheckDate();

            mDailyCheckCallback.dailyCheckComplete();
        }
    }

    /**
     * Saces the current date as last checked date.
     */
    private void saveLastDailyCheckDate() {
        Calendar now                    = Calendar.getInstance();
        SimpleDateFormat df             = new SimpleDateFormat("yyyy-MM-dd");
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(mContext.getResources().getString(R.string.pref_key_intern_last_daily_check), df.format(now.getTime()));
        editor.apply();
    }

    private void scheduleNotification() {

        String alarmTime    = pref.getString(
                mContext.getString(R.string.pref_key_notifications_time),
                mContext.getString(R.string.pref_notifications_default_time));
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
                    if (new NotificationAlarmTask(mContext, Calendar.getInstance(), canAlarmTypes[i]).exists()) {
                        new NotificationAlarmTask(mContext, Calendar.getInstance(), canAlarmTypes[i]).cancel();
                    }

                    new NotificationAlarmTask(mContext, cal, canAlarmTypes[i]).run();
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

        mDailyCheckCallback.dailyCheckComplete();
    }
}

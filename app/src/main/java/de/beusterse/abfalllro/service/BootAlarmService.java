package de.beusterse.abfalllro.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Calendar;

import de.beusterse.abfalllro.DataLoader;
import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.TimePreference;
import de.beusterse.abfalllro.TrashController;
import de.beusterse.abfalllro.capsules.Can;

/**
 * Service to re-set alarms after a device boot.
 *
 * Created by Felix Beuster
 */
public class BootAlarmService extends Service {
    private int[] canAlarmTypes = { Can.BLACK, Can.BLUE,
                                    Can.GREEN, Can.YELLOW};
    private SharedPreferences pref;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pref = PreferenceManager.getDefaultSharedPreferences(this);

        DataLoader loader               = new DataLoader(pref, getResources(), getPackageName());
        TrashController controller      = new TrashController(pref, loader.getCode(), loader.getSchedule(), getResources());
        int[] canAlarmTimes             = controller.getPreview();

        String alarmTime    = pref.getString(   getString(R.string.pref_key_notifications_time),
                                                getString(R.string.pref_notifications_default_time));
        int alarmHour       = TimePreference.getHour(alarmTime);
        int alarmMinute     = TimePreference.getMinute(alarmTime);
        int today           = 0;

        for (int i = 0; i < canAlarmTypes.length; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, canAlarmTimes[i] - 1);
            cal.set(Calendar.HOUR_OF_DAY, alarmHour);
            cal.set(Calendar.MINUTE, alarmMinute);

            AlarmTask at = new AlarmTask(this, cal, i);

            /* cancel alarm to make sure all current settings are applied */
            if (at.exists()) {
                at.cancel();
            }

            if (alarmWentOff(cal, i) || canAlarmTimes[i] == today) {
                cal.add(Calendar.DATE, canAlarmTimes[i] + controller.getNextPreview(2)[i]);
            }

            new AlarmTask(this, cal, i).run();

        }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean alarmWentOff(Calendar cal, int can) {
        long lastAlarm      = getLastAlarmTime(can);
        Calendar lastCal    = Calendar.getInstance();
        lastCal.setTimeInMillis(lastAlarm);

        return lastAlarm > 0 && lastCal.get(Calendar.DATE) == cal.get(Calendar.DATE);
    }

    private long getLastAlarmTime(int can) {
        switch (can) {
            case Can.BLACK:
                return pref.getLong(getString(R.string.pref_key_intern_last_alarm_black), 0);
            case Can.BLUE:
                return pref.getLong(getString(R.string.pref_key_intern_last_alarm_black), 0);
            case Can.GREEN:
                return pref.getLong(getString(R.string.pref_key_intern_last_alarm_black), 0);
            case Can.YELLOW:
                return pref.getLong(getString(R.string.pref_key_intern_last_alarm_black), 0);
            default:
                return 0;
        }
    }
}

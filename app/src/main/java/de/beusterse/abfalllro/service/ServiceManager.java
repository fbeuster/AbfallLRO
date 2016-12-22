package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.Calendar;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.TimePreference;
import de.beusterse.abfalllro.TrashController;
import de.beusterse.abfalllro.capsules.Can;

/**
 * Manages connections to services and service classes.
 *
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;
    public static final int SCHEDULE_IDLE   = 50;
    private int[] canAlarmTimes             = null;
    private int[] canAlarmTypes             = { Can.BLACK, Can.BLUE,
                                                Can.GREEN, Can.YELLOW};
    private ScheduleClient scheduleClient;
    private SharedPreferences pref;
    private TrashController controller;

    public ServiceManager(Context context, SharedPreferences pref, TrashController controller) {
        this.context    = context;
        this.controller = controller;
        this.pref       = pref;
        scheduleClient  = new ScheduleClient(context);
        canAlarmTimes   = controller.getPreview();
    }

    private boolean alarmWentOff(Calendar cal, int can) {
        long lastAlarm      = getLastAlarmTime(can);
        Calendar now        = Calendar.getInstance();
        Calendar lastCal    = Calendar.getInstance();
        lastCal.setTimeInMillis(lastAlarm);

        return lastAlarm > 0 && lastCal.get(Calendar.DATE) == cal.get(Calendar.DATE) && lastCal.before(now);
    }

    public void bind() {
        scheduleClient.doBindService();
    }

    private void cancelScheduledAlarms() {
        for (int i = 0; i < canAlarmTypes.length; i++) {
            if (scheduleClient.hasAlarmForNotification(canAlarmTypes[i])) {
                scheduleClient.cancelAlarmForNotification(canAlarmTypes[i]);
            }
        }
    }

    private long getLastAlarmTime(int can) {
        switch (can) {
            case Can.BLACK:
                return pref.getLong(context.getString(R.string.pref_key_intern_last_alarm_black), 0);
            case Can.BLUE:
                return pref.getLong(context.getString(R.string.pref_key_intern_last_alarm_blue), 0);
            case Can.GREEN:
                return pref.getLong(context.getString(R.string.pref_key_intern_last_alarm_green), 0);
            case Can.YELLOW:
                return pref.getLong(context.getString(R.string.pref_key_intern_last_alarm_yellow), 0);
            default:
                return 0;
        }
    }

    private boolean hasValidPreview() {
        return canAlarmTimes[0] != -1;
    }

    public void run() {
        if (canAlarmTimes != null) {
            scheduleAlarms();
        }
    }

    private void scheduleAlarms() {
        if (scheduleClient.isBound()) {
            if (pref.getBoolean(context.getString(R.string.pref_key_notifications_active),
                                context.getResources().getBoolean(R.bool.notifications_active_default))
                    && hasValidPreview()) {
                setScheduledAlarms();
            } else {
                cancelScheduledAlarms();
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduleAlarms();
                }
            }, SCHEDULE_IDLE);
        }
    }

    private void setScheduledAlarms() {
        String alarmTime    = pref.getString(   context.getString(R.string.pref_key_notifications_time),
                                                context.getString(R.string.pref_notifications_default_time));
        int alarmHour       = TimePreference.getHour(alarmTime);
        int alarmMinute     = TimePreference.getMinute(alarmTime);
        int today           = 0;

        for (int i = 0; i < canAlarmTypes.length; i++) {

            if (canAlarmTimes[i] >= 0) {
                /* cancel alarm to make sure all current settings are applied */
                if (scheduleClient.hasAlarmForNotification(canAlarmTypes[i])) {
                    scheduleClient.cancelAlarmForNotification(canAlarmTypes[i]);
                }

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, canAlarmTimes[i] - 1);
                cal.set(Calendar.HOUR_OF_DAY, alarmHour);
                cal.set(Calendar.MINUTE, alarmMinute);

                if (alarmWentOff(cal, i) || canAlarmTimes[i] == today) {
                    cal.add(Calendar.DATE, canAlarmTimes[i] + controller.getNextPreview(2)[i]);
                }

                scheduleClient.setAlarmForNotification(cal, canAlarmTypes[i]);
            }
        }
    }

    public void unbind() {
        if (scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
    }
}

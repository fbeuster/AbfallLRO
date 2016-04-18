package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import java.util.Calendar;

import de.beusterse.abfalllro.RawNotification;

/**
 * Manages connections to services and service classes.
 *
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;
    private int[] scheduledAlarms = {   RawNotification.BLACK_CAN, RawNotification.BLUE_CAN,
                                        RawNotification.GREEN_CAN, RawNotification.YELLOW_CAN};
    private ScheduleClient scheduleClient;
    private SharedPreferences pref;

    public ServiceManager(Context context, SharedPreferences pref) {
        this.context    = context;
        this.pref       = pref;
        scheduleClient  = new ScheduleClient(context);
    }

    public void bind() {
        scheduleClient.doBindService();
    }

    private void cancelScheduledAlarms() {
        for (int i = 0; i < scheduledAlarms.length; i++) {
            if (scheduleClient.hasAlarmForNotification(scheduledAlarms[i])) {
                scheduleClient.cancelAlarmForNotification(scheduledAlarms[i]);
            }
        }
    }

    public void run() {
        scheduleAlarms();
    }

    private void scheduleAlarms() {
        if (scheduleClient.isBound()) {
            if (pref.getBoolean("pref_notifications_active", false)) {
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
            }, 50);
        }
    }

    private void setScheduledAlarms() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);
        for (int i = 0; i < scheduledAlarms.length; i++) {

            /* cancel alarm to make sure all current settings are applied */
            if (scheduleClient.hasAlarmForNotification(scheduledAlarms[i])) {
                scheduleClient.cancelAlarmForNotification(scheduledAlarms[i]);
            }

            scheduleClient.setAlarmForNotification(cal, scheduledAlarms[i]);
        }
    }

    public void unbind() {
        if (scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
    }
}

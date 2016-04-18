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

    private void cancelAlarms(Calendar cal) {
        for (int i = 0; i < scheduledAlarms.length; i++) {
            if (scheduleClient.hasAlarmForNotification(cal, scheduledAlarms[i])) {
                scheduleClient.cancelAlarmForNotification(cal, scheduledAlarms[i]);
            }
        }
    }

    public void run() {
        scheduleAlarms();
    }

    private void scheduleAlarms() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);
        if (scheduleClient.isBound()) {
            if (pref.getBoolean("pref_notifications_active", false)) {
                setScheduledAlarms(cal);
            } else {
                cancelAlarms(cal);
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

    private void setScheduledAlarms(Calendar cal) {
        for (int i = 0; i < scheduledAlarms.length; i++) {
            if (!scheduleClient.hasAlarmForNotification(cal, scheduledAlarms[i])) {
                scheduleClient.setAlarmForNotification(cal, scheduledAlarms[i]);
            }
        }
    }

    public void unbind() {
        if (scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
    }
}

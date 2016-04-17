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

    public void run() {
        setAlarms();
    }

    private void setAlarms() {
        if (pref.getBoolean("pref_notifications_active", false)) {
            if (scheduleClient.isBound()) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, 5);
                scheduleClient.setAlarmForNotification(cal, RawNotification.BLACK_CAN);
                scheduleClient.setAlarmForNotification(cal, RawNotification.BLUE_CAN);
                scheduleClient.setAlarmForNotification(cal, RawNotification.GREEN_CAN);
                scheduleClient.setAlarmForNotification(cal, RawNotification.YELLOW_CAN);

            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setAlarms();
                    }
                }, 50);
            }
        }
    }

    public void unbind() {
        if (scheduleClient != null) {
            scheduleClient.doUnbindService();
        }
    }
}

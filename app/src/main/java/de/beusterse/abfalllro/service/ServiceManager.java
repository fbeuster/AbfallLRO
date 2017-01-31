package de.beusterse.abfalllro.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import de.beusterse.abfalllro.R;

/**
 * Manages connections to services and service classes.
 *
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;
    public static final int SCHEDULE_IDLE   = 50;
    private DailyAlarmClient dailyAlarmClient;
    private SharedPreferences pref;

    public ServiceManager(Context context) {
        this.context    = context;
        this.pref       = PreferenceManager.getDefaultSharedPreferences(context);
        dailyAlarmClient = new DailyAlarmClient(context);
    }

    public void bind() {
        dailyAlarmClient.doBindService();
    }

    private void cancelDailyAlarm() {
        dailyAlarmClient.cancelDailyAlarm();
    }

    public void run() {
        scheduleDailyAlarm();
    }

    private void scheduleDailyAlarm() {
        if (dailyAlarmClient.isBound()) {
            if (pref.getBoolean(context.getString(R.string.pref_key_notifications_active),
                    context.getResources().getBoolean(R.bool.notifications_active_default))) {
                setDailyAlarm();
            } else {
                cancelDailyAlarm();
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduleDailyAlarm();
                }
            }, SCHEDULE_IDLE);
        }
    }

    private void setDailyAlarm() {
        dailyAlarmClient.setDailyAlarm();
    }

    public void unbind() {
        if (dailyAlarmClient != null) {
            dailyAlarmClient.doUnbindService();
        }
    }
}

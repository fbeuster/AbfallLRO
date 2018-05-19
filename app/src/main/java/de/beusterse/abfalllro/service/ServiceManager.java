package de.beusterse.abfalllro.service;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;

import de.beusterse.abfalllro.R;
import de.beusterse.abfalllro.service.legacy.DailyAlarmClient;

/**
 * Manages connections to services and service classes.
 *
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;
    private static final int SCHEDULE_IDLE = 50;
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
        /*
            The daily check and sync will use the JobScheduler for
            Android L and higher, and falls back to the AlarmTask
            for older versions.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DailyCheckJobScheduler.schedule(
                    (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE),
                    context.getPackageName());

        } else {
            scheduleDailyAlarm();
        }
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

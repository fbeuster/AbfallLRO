package de.beusterse.abfalllro.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;

import de.beusterse.abfalllro.BuildConfig;

/**
 * Daily Check JobScheduler
 *
 * Schedules the daily check for updates and the need for notifications.
 *
 * Created by Felix Beuster on 1/4/2018.
 */

public class DailyCheckJobScheduler {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void schedule(JobScheduler jobScheduler, String packageName) {
        ComponentName name      = new ComponentName(packageName, DailyCheckJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(1, name);

        /*
            Have a short interval for debugging, daily otherwise.
         */
        if (BuildConfig.DEBUG) {
            builder.setPeriodic(DailyCheck.INTERVAL_DEBUG);
        } else {
            builder.setPeriodic(DailyCheck.INTERVAL);
        }

        /*
            Job should still be running after reboot and requires network access.
         */
        builder.setPersisted(true);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);

        if (jobScheduler != null && jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_FAILURE) {
            Log.e(DailyCheckJobScheduler.class.getSimpleName(), "run: couldn't start daily check job service");
        }
    }
}

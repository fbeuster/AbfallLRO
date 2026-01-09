package de.beusterse.abfalllro.service;

import android.app.job.JobScheduler;
import android.content.Context;

/**
 * Manages connections to services and service classes.
 * Created by Felix Beuster
 */
public class ServiceManager {

    private Context context;

    public ServiceManager(Context context) {
        this.context = context;
    }

    public void run() {
        DailyCheckJobScheduler.schedule(
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE),
                context.getPackageName());
    }
}

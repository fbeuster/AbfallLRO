package de.beusterse.abfalllro.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import androidx.annotation.RequiresApi;

import de.beusterse.abfalllro.interfaces.DailyCheckCallback;

/**
 * Daily Check JobService
 *
 * This starts the daily check for updates,
 * as well if notifications need to be scheduled.
 *
 * Created by Felix Beuster on 1/4/2018.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DailyCheckJobService extends JobService implements DailyCheckCallback {

    private JobParameters mJobParameters;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        mJobParameters = jobParameters;

        DailyCheck dailyCheck = new DailyCheck(getApplicationContext(), this);
        dailyCheck.run();

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    @Override
    public void dailyCheckComplete() {
        jobFinished(mJobParameters, false);
    }
}

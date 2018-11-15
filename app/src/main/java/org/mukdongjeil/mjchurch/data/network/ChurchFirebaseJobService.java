package org.mukdongjeil.mjchurch.data.network;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.mukdongjeil.mjchurch.util.InjectorUtils;

public class ChurchFirebaseJobService extends JobService {
    private static final String LOG_TAG = ChurchFirebaseJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(LOG_TAG, "Job service started");

        SermonNetworkDataSource networkDataSource =
                InjectorUtils.provideNetworkDataSource(this.getApplicationContext());
        networkDataSource.fetchSermon();

        jobFinished(jobParameters, false);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}

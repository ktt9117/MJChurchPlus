package org.mukdongjeil.mjchurch.data.network;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import org.mukdongjeil.mjchurch.util.InjectorUtils;

public class SermonJobService extends JobService {
    private static final String LOG_TAG = SermonJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters jobParameters) {
        Log.d(LOG_TAG, "Job service started");

        SermonNetworkDataSource networkDataSource =
                InjectorUtils.provideSermonNetworkDataSource(this.getApplicationContext());
        networkDataSource.fetch();

        jobFinished(jobParameters, false);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}

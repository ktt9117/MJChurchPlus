package org.mukdongjeil.mjchurch.data.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SermonNetworkDataSource {
    private static final String TAG = SermonNetworkDataSource.class.getSimpleName();

    private static final int SYNC_INTERVAL_HOURS = 24;
    private static final int SYNC_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_HOURS);
    private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;
    private static final String SERMON_SYNC_TAG = "sermon-sync";

    private static final Object LOCK = new Object();
    private static SermonNetworkDataSource sInstance;
    private final Context mContext;

    private final AppExecutors mExecutors;

    private final MutableLiveData<SermonEntity[]> mDownloadedSermonList;

    private SermonNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedSermonList = new MutableLiveData<>();
    }

    public static SermonNetworkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new SermonNetworkDataSource(context.getApplicationContext(), executors);
                Log.d(TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public void startFetchWeatherService() {
        Intent intentToFetch = new Intent(mContext, SermonSyncIntentService.class);
        mContext.startService(intentToFetch);
        Log.d(TAG, "Service created");
    }

    void fetchSermon() {
        Log.d(TAG, "Fetch sermon started");
        mExecutors.networkIO().execute(() -> {
            try {
                URL sermonRequestUrl = NetworkUtils.getUrl();

                String html = NetworkUtils.getResponseFromHttpUrl(sermonRequestUrl);
                SermonHtmlParser parser = new SermonHtmlParser();
                List<SermonEntity> sermonList = new ArrayList<>();

                List<String> detailLinkList = parser.parseLinkList(html);
                if (detailLinkList != null && detailLinkList.size() > 0) {
                    for (String detailLink : detailLinkList) {
                        String bbsNo = detailLink.substring(detailLink.lastIndexOf("=") + 1);
                        URL sermonDetailUrl = NetworkUtils.makeCompleteUrl(detailLink);
                        String detailHtml = NetworkUtils.getResponseFromHttpUrl(sermonDetailUrl);

                        SermonEntity entity = parser.parse(bbsNo, detailHtml);
                        Log.d(TAG, "Html Parsing finished");
                        sermonList.add(entity);
                    }
                }

                if (sermonList.size() != 0) {
                    Log.d(TAG, "SermonList not null and has " + sermonList.size() + " values");
                    mDownloadedSermonList.postValue(sermonList.toArray(new SermonEntity[sermonList.size()]));
                }

            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
            }
        });
    }

    public LiveData<SermonEntity[]> getSermonEntity() {
        return mDownloadedSermonList;
    }

    public void scheduleRecurringFetchWeatherSync() {
        Driver driver = new GooglePlayDriver(mContext);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job syncChurchJob = dispatcher.newJobBuilder()
                .setService(ChurchFirebaseJobService.class)
                .setTag(SERMON_SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncChurchJob);
        Log.d(TAG, "Job scheduled");
    }

    public Context getContext() {
        return mContext;
    }
}
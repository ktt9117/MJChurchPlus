package org.mukdongjeil.mjchurch.data.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;

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
    private static final String SYNC_TAG = "sermon-sync";

    private static final Object LOCK = new Object();
    private static SermonNetworkDataSource sInstance;
    private final Context mContext;

    private final AppExecutors mExecutors;

    private final MutableLiveData<SermonEntity[]> mDownloadedSermonList;
    private final MutableLiveData<IntroduceEntity[]> mDownloadedIntroduceList;
    private final MutableLiveData<TrainingEntity[]> mDownloadedTrainingList;

    private SermonNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedSermonList = new MutableLiveData<>();
        mDownloadedIntroduceList = new MutableLiveData<>();
        mDownloadedTrainingList = new MutableLiveData<>();
    }

    public static SermonNetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new SermonNetworkDataSource(context.getApplicationContext(), executors);
            }
        }
        return sInstance;
    }

    public void startFetchService() {
        Intent intentToFetch = new Intent(mContext, SermonSyncIntentService.class);
        intentToFetch.putExtra(SermonSyncIntentService.INTENT_KEY_FETCH_TYPE,
                SermonSyncIntentService.INTENT_VALUE_FETCH_TYPE_SERMON);
        mContext.startService(intentToFetch);
    }

    void fetch() {
        // get sermon list
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
                        sermonList.add(entity);
                    }
                }

                if (sermonList.size() != 0) {
                    mDownloadedSermonList.postValue(sermonList.toArray(new SermonEntity[sermonList.size()]));
                }

            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, TAG, "error occurred while get the sermon list : " + e.getMessage());
            }
        });

        // get introduce list
        mExecutors.networkIO().execute(() -> {
            try {
                URL welcomeUrl = NetworkUtils.getWelcomeUrl();
                String html = NetworkUtils.getResponseFromHttpUrl(welcomeUrl);
                IntroduceHtmlParser parser = new IntroduceHtmlParser();
                List<IntroduceEntity> introduceList = new ArrayList<>();

                List<String> detailLinkList = parser.parseLinkList(html);
                if (detailLinkList != null && detailLinkList.size() > 0) {
                    for (String detailLink : detailLinkList) {
                        URL detailUrl = NetworkUtils.makeCompleteUrl(detailLink);
                        String detailHtml = NetworkUtils.getResponseFromHttpUrl(detailUrl);
                        IntroduceEntity entity = parser.parse(detailHtml);
                        if (entity != null) {
                            introduceList.add(entity);
                        }
                    }
                }

                if (introduceList.size() > 0) {
                    mDownloadedIntroduceList.postValue(introduceList.toArray(
                            new IntroduceEntity[introduceList.size()]));
                }
            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, TAG, "error occurred while get the introduce menu list : " + e.getMessage());
            }
        });

        // get training list
        mExecutors.networkIO().execute(() -> {
            try {
                URL trainingUrl = NetworkUtils.getTrainingUrl();
                String html = NetworkUtils.getResponseFromHttpUrl(trainingUrl);
                TrainingHtmlParser parser = new TrainingHtmlParser();
                List<TrainingEntity> list = new ArrayList<>();

                List<String> detailLinkList = parser.parseLinkList(html);
                if (detailLinkList != null && detailLinkList.size() > 0) {
                    for (String detailLink : detailLinkList) {
                        URL detailUrl = NetworkUtils.makeCompleteUrl(detailLink);
                        String detailHtml = NetworkUtils.getResponseFromHttpUrl(detailUrl);
                        TrainingEntity entity = parser.parse(detailHtml);
                        if (entity != null) {
                            list.add(entity);
                        }
                    }
                }

                if (list.size() > 0) {
                    mDownloadedTrainingList.postValue(list.toArray(new TrainingEntity[list.size()]));
                }
            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, TAG, "error occurred while get the training menu list : " + e.getMessage());
            }
        });
    }

    public LiveData<SermonEntity[]> getSermonEntity() {
        return mDownloadedSermonList;
    }

    public LiveData<IntroduceEntity[]> getIntroduceEntity() {
        return mDownloadedIntroduceList;
    }

    public LiveData<TrainingEntity[]> getTrainingEntity() { return mDownloadedTrainingList; }

    public void scheduleRecurringFetchSermonSync() {
        Driver driver = new GooglePlayDriver(mContext);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        Job syncJob = dispatcher.newJobBuilder()
                .setService(SermonJobService.class)
                .setTag(SYNC_TAG)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        SYNC_INTERVAL_SECONDS,
                        SYNC_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        dispatcher.schedule(syncJob);
    }

    public Context getContext() {
        return mContext;
    }
}
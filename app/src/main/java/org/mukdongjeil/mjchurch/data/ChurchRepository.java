package org.mukdongjeil.mjchurch.data;

import android.content.SharedPreferences;
import android.util.Log;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.dao.SermonDao;
import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;
import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;
import org.mukdongjeil.mjchurch.data.network.SermonNetworkDataSource;
import org.mukdongjeil.mjchurch.data.network.SermonReplyNetworkDataSource;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;

public class ChurchRepository {
    private static final String TAG = ChurchRepository.class.getSimpleName();

    private static final String LAST_FETCH_TIME_IN_MILLIS = "lastFetchTimeInMillis";
    private static final long A_DAY_TIME_MILLIS = 1000 * 60 * 60 * 24;
    private static final Object LOCK = new Object();
    private static ChurchRepository sInstance;
    private final SermonDao mSermonDao;
    private final SermonNetworkDataSource mSermonNetworkDataSource;
    private final SermonReplyNetworkDataSource mSermonReplyNetworkSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;

    private LiveData<List<SermonReplyEntity>> mSermonReplyEntityLiveData;

    private ChurchRepository(SermonDao sermonDao,
                             SermonNetworkDataSource sermonNetworkDataSource,
                             SermonReplyNetworkDataSource sermonReplyNetworkDataSource,
                             AppExecutors executors) {
        mSermonDao = sermonDao;
        mSermonNetworkDataSource = sermonNetworkDataSource;
        mSermonReplyNetworkSource = sermonReplyNetworkDataSource;
        mExecutors = executors;

        LiveData<SermonEntity[]> networkData = mSermonNetworkDataSource.getSermonEntity();
        networkData.observeForever(newListFromNetwork
                -> mExecutors.diskIO().execute(()
                -> mSermonDao.insertSermon(newListFromNetwork)));

        LiveData<IntroduceEntity[]> introData = mSermonNetworkDataSource.getIntroduceEntity();
        introData.observeForever(newListFromNetwork
                -> mExecutors.diskIO().execute(()
                -> mSermonDao.insertIntroduce(newListFromNetwork)));

        LiveData<TrainingEntity[]> trainingData = mSermonNetworkDataSource.getTrainingEntity();
        trainingData.observeForever(newListFromNetwork
                -> mExecutors.diskIO().execute(()
                -> mSermonDao.insertTraining(newListFromNetwork)));

    }

    public synchronized static ChurchRepository getInstance(
            SermonDao sermonDao,
            SermonNetworkDataSource sermonNetworkDataSource,
            SermonReplyNetworkDataSource sermonReplyNetworkDataSource,
            AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChurchRepository(sermonDao, sermonNetworkDataSource,
                        sermonReplyNetworkDataSource, executors);
                Log.d(TAG, "Made new repository");
            }
        }

        return sInstance;
    }

    public LiveData<List<SermonEntity>> getSermonList() {
        initializeData();
        return mSermonDao.getSermonList();
    }

    public LiveData<SermonEntity> getSermonEntity(int bbsNo) {
        initializeData();
        return mSermonDao.getSermonEntity(bbsNo);
    }

    public LiveData<List<IntroduceEntity>> getIntroduceList() {
        initializeData();
        return mSermonDao.getIntroduceList();
    }

    public LiveData<List<TrainingEntity>> getTrainingList() {
        initializeData();
        return mSermonDao.getTrainingList();
    }

    public LiveData<List<SermonReplyEntity>> getSermonReplyList(int bbsNo) {
        if (mSermonReplyNetworkSource.getSermonReplyEntity().getValue() != null) {
            mSermonReplyNetworkSource.getSermonReplyEntity().getValue().clear();
        }

        mExecutors.diskIO().execute(()-> {
            startFetchSermonReplyService(bbsNo);
        });

        mSermonReplyEntityLiveData = mSermonReplyNetworkSource.getSermonReplyEntity();
        return mSermonReplyEntityLiveData;
    }

    public void addSermonReply(int bbsNo, SermonReplyEntity entity) {
        mSermonReplyNetworkSource.addReply(bbsNo, entity);
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        mSermonNetworkDataSource.scheduleRecurringFetchSermonSync();

        mExecutors.diskIO().execute(()-> {
            if (isFetchNeeded()) {
                startFetchSermonService();
            }
        });
    }

    private boolean isFetchNeeded() {
        List<SermonEntity> list = mSermonDao.getSermonListNotLiveData();
        int itemSize = list.size();
        if (itemSize > 0) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(mSermonNetworkDataSource.getContext());
            long lastFetchTimeInMillis = prefs.getLong(LAST_FETCH_TIME_IN_MILLIS, 0);
            if (lastFetchTimeInMillis == 0 || (System.currentTimeMillis() - lastFetchTimeInMillis) >= A_DAY_TIME_MILLIS) {
                prefs.edit().putLong(LAST_FETCH_TIME_IN_MILLIS, System.currentTimeMillis()).apply();
                Log.i(TAG, "fetch need caused by lastFetchTime has passed a day.");
                return true;
            }

            Log.i(TAG, "fetch not need caused by lastFetchTime has not passed a day.");
            return false;

        } else {
            Log.i(TAG, "fetch need caused by there is no data in the rooms");
            return true;
        }

    }

    private void startFetchSermonService() {
        mSermonNetworkDataSource.startFetchService();
    }

    private void startFetchSermonReplyService(int bbsNo) {
        mSermonReplyNetworkSource.startFetchService(bbsNo);
    }
}

package org.mukdongjeil.mjchurch.data;

import android.util.Log;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.dao.SermonDao;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;
import org.mukdongjeil.mjchurch.data.network.SermonNetworkDataSource;

import java.util.List;

import androidx.lifecycle.LiveData;

public class ChurchRepository {
    private static final String TAG = ChurchRepository.class.getSimpleName();

    private static final String LAST_FETCH_TIME_IN_MILLIS = "lastFetchTimeInMillis";
    private static final long A_DAY_TIME_MILLIS = 1000 * 60 * 60 * 24;
    private static final Object LOCK = new Object();
    private static ChurchRepository sInstance;
    private final SermonDao mSermonDao;
    private final SermonNetworkDataSource mSermonNetworkDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;

    private ChurchRepository(SermonDao sermonDao,
                             SermonNetworkDataSource sermonNetworkDataSource,
                             AppExecutors executors) {
        mSermonDao = sermonDao;
        mSermonNetworkDataSource = sermonNetworkDataSource;
        mExecutors = executors;

        LiveData<SermonEntity[]> networkData = mSermonNetworkDataSource.getSermonEntity();
        networkData.observeForever(newSermonListFromNetwork -> {
            mExecutors.diskIO().execute(()-> {
                mSermonDao.insert(newSermonListFromNetwork);
            });
        });
    }

    public synchronized static ChurchRepository getInstance(
            SermonDao sermonDao,
            SermonNetworkDataSource sermonNetworkDataSource,
            AppExecutors executors) {
        Log.d(TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ChurchRepository(sermonDao, sermonNetworkDataSource, executors);
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

    public LiveData<List<SermonReplyEntity>> getSermonReplyList(int bbsNo) {
        initializeData();
        return mSermonDao.getSermonReplyList(bbsNo);
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;

        mSermonNetworkDataSource.scheduleRecurringFetchWeatherSync();

        mExecutors.diskIO().execute(()-> {
            if (isFetchNeeded()) {
                startFetchWeatherService();
            }
        });
    }

    private boolean isFetchNeeded() {
        // TODO : check fetch needed
        return false;

//        Date today = SunshineDateUtils.getNormalizedUtcDateForToday();
//        int count = mWeatherDao.countAllFutureWeather(today);
//        return (count < WeatherNetworkDataSource.NUM_DAYS);
//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(mSermonNetworkDataSource.getContext());
//        long lastFetchTimeInMillis = prefs.getLong(LAST_FETCH_TIME_IN_MILLIS, 0);
//
//        if (lastFetchTimeInMillis == 0) {
//            prefs.edit().putLong(LAST_FETCH_TIME_IN_MILLIS, System.currentTimeMillis()).apply();
//            return true;
//        } else {
//            if (System.currentTimeMillis() - lastFetchTimeInMillis >= A_DAY_TIME_MILLIS) {
//                prefs.edit().putLong(LAST_FETCH_TIME_IN_MILLIS, System.currentTimeMillis()).apply();
//                return true;
//            } else {
//                return false;
//            }
//        }
//        return true;
    }

    private void startFetchWeatherService() {
        mSermonNetworkDataSource.startFetchWeatherService();
    }
}

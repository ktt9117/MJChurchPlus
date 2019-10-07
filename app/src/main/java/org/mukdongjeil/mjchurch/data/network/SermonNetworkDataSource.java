package org.mukdongjeil.mjchurch.data.network;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.FirestoreDatabase;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private FirebaseFirestore mFirestore;
    private FirebaseUser mUser;

    private final AppExecutors mExecutors;

    private final MutableLiveData<SermonEntity[]> mDownloadedSermonList;

    private SermonNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mFirestore = FirebaseFirestore.getInstance();
        mDownloadedSermonList = new MutableLiveData<>();
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
        setupUser();
        Intent intentToFetch = new Intent(mContext, DataSyncIntentService.class);
        intentToFetch.putExtra(DataSyncIntentService.INTENT_KEY_FETCH_TYPE,
                DataSyncIntentService.INTENT_VALUE_FETCH_TYPE_SERMON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intentToFetch);
        } else {
            mContext.startService(intentToFetch);
        }

    }

    void fetch() {
        // get sermon list
        selectFetchSource();
    }

    private void selectFetchSource() {
        mExecutors.networkIO().execute(() -> {
            mFirestore.collection(FirestoreDatabase.Collection.APP_SETTINGS)
                    .document(FirestoreDatabase.Document.LAST_SYNC_INFO)
                    .get().addOnCompleteListener(task -> onLastSyncDateResult(task));
        });
    }

    private void onLastSyncDateResult(Task<DocumentSnapshot> task) {
        mExecutors.networkIO().execute(() -> {
            if (task.isSuccessful()) {
                try {
                    DocumentSnapshot doc = task.getResult();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    if (mUser == null || (doc.exists() && doc.getData().get(FirestoreDatabase.Field.SERMON_SYNC_DATE) != null &&
                            doc.getData().get(FirestoreDatabase.Field.SERMON_SYNC_DATE).toString().equals(sdf.format(new Date())))) {
                        Log.i(TAG, "get sermon list from firestore");
                        mFirestore.collection(FirestoreDatabase.Collection.SERMON)
                                .orderBy("bbsNo", Query.Direction.DESCENDING)
                                .get()
                                .addOnCompleteListener((task1)-> onSermonListFirestoreResult(task1));

                    } else {
                        Log.i(TAG, "get sermon list from web sites");
                        URL sermonRequestUrl = NetworkUtils.getSermonUrl();

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
                                if (entity != null) {
                                    sermonList.add(entity);
                                    if (mUser != null) {
                                        createOrUpdateToFirebase(entity);
                                    }
                                }
                            }
                        }

                        mDownloadedSermonList.postValue(sermonList.toArray(new SermonEntity[sermonList.size()]));
                        updateSyncDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "error occured while get the sermon list from http : " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Could not get the AppSettings from firestore");
            }
        });
    }

    private void updateSyncDate(String today) {
        mExecutors.networkIO().execute(() -> {
            Map<String, Object> lastSyncDate = new HashMap<>();
            lastSyncDate.put(FirestoreDatabase.Field.SERMON_SYNC_DATE, today);
            mFirestore.collection(FirestoreDatabase.Collection.APP_SETTINGS)
                    .document(FirestoreDatabase.Document.LAST_SYNC_INFO)
                    .set(lastSyncDate, SetOptions.merge());
        });
    }

    private void onSermonListFirestoreResult(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
            List<DocumentSnapshot> docs = task.getResult().getDocuments();
            if (docs.isEmpty() == false) {
                List<SermonEntity> sermonList = new ArrayList<>();
                for (DocumentSnapshot doc : docs) {
                    SermonEntity entity = doc.toObject(SermonEntity.class);
                    sermonList.add(entity);
                }

                mDownloadedSermonList.postValue(sermonList.toArray(new SermonEntity[sermonList.size()]));
            }
        } else {
            Log.e(TAG, "SermonList get failed with : " + task.getException().getMessage());
        }
    }

    private void createOrUpdateToFirebase(SermonEntity entity) {
        mExecutors.networkIO().execute(() -> {
            mFirestore.collection(FirestoreDatabase.Collection.SERMON)
                    .document(Integer.toString(entity.getBbsNo()))
                    .set(entity)
                    .addOnSuccessListener((aVoid) -> Log.d(TAG, entity.getBbsNo() + " sermon entity has been saved to firestore"))
                    .addOnFailureListener((e)-> Log.e(TAG, "sermon entity could not set to firestore : " + e.getMessage()));

        });
    }

    private void setupUser() {
        if (FirebaseAuth.getInstance() != null) {
            mUser = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    public LiveData<SermonEntity[]> getSermonEntity() {
        return mDownloadedSermonList;
    }

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
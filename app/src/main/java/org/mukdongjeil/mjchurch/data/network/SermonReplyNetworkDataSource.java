package org.mukdongjeil.mjchurch.data.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.FirestoreDatabase;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SermonReplyNetworkDataSource {
    private static final String TAG = SermonReplyNetworkDataSource.class.getSimpleName();

    private static final Object LOCK = new Object();
    private static SermonReplyNetworkDataSource sInstance;
    private final Context mContext;
    private FirebaseFirestore mFirestore;
    private final AppExecutors mExecutors;

    private final MutableLiveData<List<SermonReplyEntity>> mDownloadedSermonReplyList;

    private SermonReplyNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedSermonReplyList = new MutableLiveData<>();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static SermonReplyNetworkDataSource getInstance(Context context, AppExecutors executors) {
        Log.d(TAG, "Getting the network data source");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new SermonReplyNetworkDataSource(context.getApplicationContext(), executors);
                Log.d(TAG, "Made new network data source");
            }
        }
        return sInstance;
    }

    public void startFetchService(int bbsNo) {
        Intent intentToFetch = new Intent(mContext, SermonSyncIntentService.class);
        intentToFetch.putExtra(SermonSyncIntentService.INTENT_KEY_FETCH_TYPE,
                SermonSyncIntentService.INTENT_VALUE_FETCH_TYPE_SERMON_REPLY);
        intentToFetch.putExtra(SermonSyncIntentService.INTENT_KEY_BBS_NO, bbsNo);
        mContext.startService(intentToFetch);
        Log.d(TAG, "Service created");
    }

    void fetch(int bbsNo) {
        Log.d(TAG, "Fetch sermon reply started");
        mExecutors.networkIO().execute(() -> {
            try {
                mFirestore.collection(FirestoreDatabase.Collection.SERMON)
                        .document(Integer.toString(bbsNo))
                        .collection(FirestoreDatabase.Collection.REPLIES)
                        .orderBy("createdAt", Query.Direction.ASCENDING)
                        .get()
                        .addOnCompleteListener((task)-> {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                if (docs.isEmpty() == false) {
                                    List<SermonReplyEntity> sermonReplyList = new ArrayList<>();
                                    for (DocumentSnapshot doc : docs) {
                                        SermonReplyEntity entity = doc.toObject(SermonReplyEntity.class);
                                        entity.setDocumentId(doc.getId());
                                        sermonReplyList.add(entity);
                                    }

                                    Log.i(TAG, "sermonReplyList postValue size : " + sermonReplyList.size());
                                    mDownloadedSermonReplyList.postValue(sermonReplyList);
                                }
                            } else {
                                Log.e(TAG, "reply get failed with : " + task.getException().getMessage());
                                Crashlytics.log(Log.ERROR, TAG, "Reply get failed with : " + task.getException().getMessage());
                            }
                        });
            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, TAG, "error occurred while get the sermon reply list : " + e.getMessage());
            }
        });
    }

    public void addReply(int bbsNo, SermonReplyEntity entity) {
        mExecutors.networkIO().execute(() -> {
            mFirestore.collection(FirestoreDatabase.Collection.SERMON)
                    .document(Integer.toString(bbsNo))
                    .collection(FirestoreDatabase.Collection.REPLIES)
                    .add(entity)
                    .addOnSuccessListener((documentReference)-> {
                        Log.i(TAG, "add reply succeed : " + documentReference.getId());
                        fetch(bbsNo);

                    }).addOnFailureListener((e)-> {
                        Log.e(TAG, "add reply failed : " + e.getMessage());
                        Crashlytics.log(Log.ERROR, TAG, "Add reply failed: " + e.getMessage());
                    });
        });
    }

    public LiveData<List<SermonReplyEntity>> getSermonReplyEntity() {
        return mDownloadedSermonReplyList;
    }

    public Context getContext() {
        return mContext;
    }
}
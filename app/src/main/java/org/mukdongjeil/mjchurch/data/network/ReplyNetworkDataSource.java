package org.mukdongjeil.mjchurch.data.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.FirestoreDatabase;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ReplyNetworkDataSource {
    private static final String TAG = ReplyNetworkDataSource.class.getSimpleName();

    private static final Object LOCK = new Object();
    private static ReplyNetworkDataSource sInstance;
    private final Context mContext;
    private FirebaseFirestore mFirestore;
    private final AppExecutors mExecutors;

    private final MutableLiveData<List<ReplyEntity>> mDownloadedReplyList;

    private ReplyNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedReplyList = new MutableLiveData<>();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static ReplyNetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ReplyNetworkDataSource(context.getApplicationContext(), executors);
            }
        }
        return sInstance;
    }

    public void startFetchService(String collectionType, String documentNo) {
        Intent intentToFetch = new Intent(mContext, DataSyncIntentService.class);
        intentToFetch.putExtra(DataSyncIntentService.INTENT_KEY_FETCH_TYPE,
                DataSyncIntentService.INTENT_VALUE_FETCH_TYPE_REPLY);
        intentToFetch.putExtra(DataSyncIntentService.INTENT_KEY_COLLECTION_TYPE, collectionType);
        intentToFetch.putExtra(DataSyncIntentService.INTENT_KEY_DOCUMENT_NO, documentNo);
        mContext.startService(intentToFetch);
    }

    void fetch(String collectionType, String documentNo) {
        mExecutors.networkIO().execute(() -> {
            try {
                mFirestore.collection(collectionType)
                        .document(documentNo)
                        .collection(FirestoreDatabase.Collection.REPLIES)
                        .orderBy("createdAt", Query.Direction.ASCENDING)
                        .get()
                        .addOnCompleteListener((task)-> {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                if (docs.isEmpty() == false) {
                                    List<ReplyEntity> replyList = new ArrayList<>();
                                    for (DocumentSnapshot doc : docs) {
                                        ReplyEntity entity = doc.toObject(ReplyEntity.class);
                                        entity.setDocumentId(doc.getId());
                                        replyList.add(entity);
                                    }

                                    mDownloadedReplyList.postValue(replyList);
                                }
                            } else {
                                Log.e(TAG, "Reply get failed with : " + task.getException().getMessage());
                            }
                        });
            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
                Log.e(TAG, "error occurred while get the sermon reply list : " + e.getMessage());
            }
        });
    }

    public void addReply(String collectionType, String documentId, ReplyEntity entity) {
        mExecutors.networkIO().execute(() -> {
            mFirestore.collection(collectionType)
                    .document(documentId)
                    .collection(FirestoreDatabase.Collection.REPLIES)
                    .add(entity)
                    .addOnSuccessListener((documentReference)-> {
                        Log.i(TAG, "add reply succeed : " + documentReference.getId());
                        fetch(collectionType, documentId);

                    }).addOnFailureListener((e)-> {
                        Log.e(TAG, "Add reply failed: " + e.getMessage());
                    });
        });
    }

    public LiveData<List<ReplyEntity>> getReplyEntity() {
        return mDownloadedReplyList;
    }

    public Context getContext() {
        return mContext;
    }
}
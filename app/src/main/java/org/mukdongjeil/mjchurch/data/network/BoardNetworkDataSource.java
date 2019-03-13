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
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class BoardNetworkDataSource {
    private static final String TAG = BoardNetworkDataSource.class.getSimpleName();

    private static final Object LOCK = new Object();
    private static BoardNetworkDataSource sInstance;
    private final Context mContext;
    private FirebaseFirestore mFirestore;
    private final AppExecutors mExecutors;

    private final MutableLiveData<List<BoardEntity>> mDownloadedList;

    private BoardNetworkDataSource(Context context, AppExecutors executors) {
        mContext = context;
        mExecutors = executors;
        mDownloadedList = new MutableLiveData<>();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static BoardNetworkDataSource getInstance(Context context, AppExecutors executors) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new BoardNetworkDataSource(context.getApplicationContext(), executors);
            }
        }
        return sInstance;
    }

    public void startFetchService() {
        Intent intentToFetch = new Intent(mContext, SermonSyncIntentService.class);
        intentToFetch.putExtra(SermonSyncIntentService.INTENT_KEY_FETCH_TYPE,
                SermonSyncIntentService.INTENT_VALUE_FETCH_TYPE_BOARD);
        mContext.startService(intentToFetch);
    }

    void fetch() {
        mExecutors.networkIO().execute(() -> {
            try {
                mFirestore.collection(FirestoreDatabase.Collection.BOARD)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .addOnCompleteListener((task)-> {
                            if (task.isSuccessful()) {
                                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                                if (docs.isEmpty() == false) {
                                    List<BoardEntity> boardEntityList = new ArrayList<>();
                                    for (DocumentSnapshot doc : docs) {
                                        BoardEntity entity = doc.toObject(BoardEntity.class);
                                        entity.setId(doc.getId());
                                        boardEntityList.add(entity);
                                    }

                                    mDownloadedList.postValue(boardEntityList);
                                } else {
                                    Log.i(TAG, FirestoreDatabase.Collection.BOARD + " collection is empty");
                                }
                            } else {
                                Crashlytics.log(Log.ERROR, TAG, "board get failed with : " + task.getException().getMessage());
                            }
                        });
            } catch (Exception e) {
                // Server probably invalid
                e.printStackTrace();
                Crashlytics.log(Log.ERROR, TAG, "error occurred while get the board list : " + e.getMessage());
            }
        });
    }

    public void addBoard(BoardEntity entity, OnCompleteListener listener) {
        mExecutors.networkIO().execute(() -> {
            mFirestore.collection(FirestoreDatabase.Collection.BOARD)
                .add(entity)
                .addOnSuccessListener(documentReference -> {
                    Log.i(TAG, "add board succeed : " + documentReference.getId());
                    fetch();
                    if (listener != null) {
                        listener.onCompleted(true, documentReference.getId());
                    }

                }).addOnFailureListener(e -> {
                    Crashlytics.log(Log.ERROR, TAG, "Add board failed: " + e.getMessage());
                    if (listener != null) {
                        listener.onCompleted(false, e.getMessage());
                    }
                });
        });
    }

    public LiveData<List<BoardEntity>> getBoardList() {
        return mDownloadedList;
    }

    public Context getContext() {
        return mContext;
    }

    public interface OnCompleteListener {
        void onCompleted(boolean isSucceed, @Nullable String message);
    }
}

package org.mukdongjeil.mjchurch.data.network;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.database.FirestoreDatabase;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Intent intentToFetch = new Intent(mContext, DataSyncIntentService.class);
        intentToFetch.putExtra(DataSyncIntentService.INTENT_KEY_FETCH_TYPE,
                DataSyncIntentService.INTENT_VALUE_FETCH_TYPE_BOARD);
        mContext.startService(intentToFetch);
    }

    void fetch() {
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
                    if (doc.exists() && doc.getData().get(FirestoreDatabase.Field.BOARD_SYNC_DATE).toString().equals(sdf.format(new Date()))) {
                        Log.i(TAG, "get board list from firestore");
                        mFirestore.collection(FirestoreDatabase.Collection.BOARD)
                                .orderBy("createdAt", Query.Direction.DESCENDING)
                                .get()
                                .addOnCompleteListener((task1)-> onBoardListFirestoreResult(task1));

                    } else {
                        Log.i(TAG, "get board list from web sites");
                        URL url = NetworkUtils.getBoardUrl();
                        String html = NetworkUtils.getResponseFromHttpUrl(url);
                        List<BoardEntity> boardList = new ArrayList<>();

                        BoardHtmlParser parser = new BoardHtmlParser();
                        List<String> linkList = parser.parseLinkList(html);
                        if (linkList != null && linkList.size() > 0) {
                            for (String link : linkList) {
                                String bbsNo = link.substring(link.lastIndexOf("=") + 1);
                                URL detailUrl = NetworkUtils.makeCompleteUrl(link);
                                String detailHtml = NetworkUtils.getResponseFromHttpUrl(detailUrl);

                                BoardEntity entity = parser.parse(bbsNo, detailHtml);
                                if (entity != null) {
                                    boardList.add(entity);
                                    createOrUpdateToFirebase(entity);
                                }
                            }
                        }

                        mDownloadedList.postValue(boardList);
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

    private void onBoardListFirestoreResult(Task<QuerySnapshot> task) {
        List<BoardEntity> boardEntityList = new ArrayList<>();
        if (task.isSuccessful()) {
            List<DocumentSnapshot> docs = task.getResult().getDocuments();
            if (docs.isEmpty() == false) {
                for (DocumentSnapshot doc : docs) {
                    BoardEntity entity = doc.toObject(BoardEntity.class);
                    entity.setId(doc.getId());
                    boardEntityList.add(entity);
                }
            } else {
                Log.i(TAG, FirestoreDatabase.Collection.BOARD + " collection is empty");
            }

        } else {
            Log.e(TAG, "board get failed with : " + task.getException().getMessage());
        }

        mDownloadedList.postValue(boardEntityList);
    }

    private void updateSyncDate(String today) {
        mExecutors.networkIO().execute(() -> {
            Map<String, Object> lastSyncDate = new HashMap<>();
            lastSyncDate.put(FirestoreDatabase.Field.BOARD_SYNC_DATE, today);
            mFirestore.collection(FirestoreDatabase.Collection.APP_SETTINGS)
                    .document(FirestoreDatabase.Document.LAST_SYNC_INFO)
                    .set(lastSyncDate);
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
                    Log.e(TAG, "Add board failed: " + e.getMessage());
                    if (listener != null) {
                        listener.onCompleted(false, e.getMessage());
                    }
                });
        });
    }

    private void createOrUpdateToFirebase(BoardEntity entity) {
        AppExecutors.getInstance().networkIO().execute(() -> {
            FirebaseFirestore.getInstance().collection(FirestoreDatabase.Collection.BOARD)
                    .document(entity.getId())
                    .set(entity)
                    .addOnSuccessListener((aVoid) -> Log.d(TAG, entity.getId() + " board entity has been saved to firestore"))
                    .addOnFailureListener((e)-> Log.e(TAG, "board entity could not set to firestore : " + e.getMessage()));

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
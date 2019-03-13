package org.mukdongjeil.mjchurch.ui.board_detail;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.FirestoreDatabase;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BoardDetailViewModel extends ViewModel {
    private static final String TAG = BoardDetailViewModel.class.getSimpleName();

    private final ChurchRepository mRepository;
    private final MutableLiveData<BoardEntity> mBoardEntity = new MutableLiveData<>();

    public BoardDetailViewModel(ChurchRepository repository) {
        mRepository = repository;
    }

    public LiveData<BoardEntity> getBoard(String id) {
        mRepository.getBoard(mBoardEntity, id);

        if (mBoardEntity.getValue() == null) {
            AppExecutors.getInstance().networkIO().execute(() -> {
                FirebaseFirestore.getInstance().collection(FirestoreDatabase.Collection.BOARD)
                    .document(id)
                    .get()
                    .addOnCompleteListener((task)-> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                BoardEntity entity = doc.toObject(BoardEntity.class);
                                entity.setId(doc.getId());
                                mBoardEntity.postValue(entity);

                            } else {
                                Log.i(TAG, id + " document is not exists");
                            }
                        } else {
                            Crashlytics.log(Log.ERROR, TAG, "document get failed with : " + task.getException().getMessage());
                        }
                    });
            });
        }

        return mBoardEntity;
    }
}

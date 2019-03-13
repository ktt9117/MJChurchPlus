package org.mukdongjeil.mjchurch.ui.board_detail;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mukdongjeil.mjchurch.AppExecutors;
import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.FirestoreDatabase;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BoardDetailViewModel extends ViewModel {
    private static final String TAG = BoardDetailViewModel.class.getSimpleName();

    private final ChurchRepository mRepository;
    private final MutableLiveData<BoardEntity> mBoardEntity = new MutableLiveData<>();
    private final LiveData<List<ReplyEntity>> mReplyList;
    private String boardId;

    public BoardDetailViewModel(ChurchRepository repository, String boardId) {
        mRepository = repository;
        mReplyList = repository.getBoardReplyList(boardId);
        this.boardId = boardId;
    }

    public LiveData<BoardEntity> getBoard() {
        mRepository.getBoard(mBoardEntity, boardId);

        if (mBoardEntity.getValue() == null) {
            AppExecutors.getInstance().networkIO().execute(() -> {
                FirebaseFirestore.getInstance().collection(FirestoreDatabase.Collection.BOARD)
                    .document(boardId)
                    .get()
                    .addOnCompleteListener((task)-> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                BoardEntity entity = doc.toObject(BoardEntity.class);
                                entity.setId(doc.getId());
                                mBoardEntity.postValue(entity);

                            } else {
                                Log.i(TAG, boardId + " document is not exists");
                            }
                        } else {
                            Log.e(TAG, "document get failed with : " + task.getException().getMessage());
                        }
                    });
            });
        }

        return mBoardEntity;
    }

    public LiveData<List<ReplyEntity>> getReplyList() {
        return mReplyList;
    }

    public void addReply(ReplyEntity entity) {
        mRepository.addBoardReply(boardId, entity);
    }
}

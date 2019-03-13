package org.mukdongjeil.mjchurch.ui.boards;

import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.BoardEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class BoardListViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<List<BoardEntity>> mBoardList;

    public BoardListViewModel(ChurchRepository repository) {
        mRepository = repository;
        mBoardList = mRepository.getBoardList();
    }

    public LiveData<List<BoardEntity>> getBoardList() {
        return mBoardList;
    }
}

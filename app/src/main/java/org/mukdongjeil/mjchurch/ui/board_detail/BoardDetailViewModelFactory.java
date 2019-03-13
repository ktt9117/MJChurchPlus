package org.mukdongjeil.mjchurch.ui.board_detail;

import org.mukdongjeil.mjchurch.data.ChurchRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BoardDetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChurchRepository mRepository;
    private String boardId;

    public BoardDetailViewModelFactory(ChurchRepository repository, String boardId) {
        this.mRepository = repository;
        this.boardId = boardId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new BoardDetailViewModel(mRepository, boardId);
    }
}

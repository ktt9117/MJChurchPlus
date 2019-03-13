package org.mukdongjeil.mjchurch.ui.boards;

import org.mukdongjeil.mjchurch.data.ChurchRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BoardViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChurchRepository mRepository;

    public BoardViewModelFactory(ChurchRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new BoardListViewModel(mRepository);
    }
}

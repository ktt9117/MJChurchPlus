package org.mukdongjeil.mjchurch.ui.sermons;

import org.mukdongjeil.mjchurch.data.ChurchRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SermonViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChurchRepository mRepository;

    public SermonViewModelFactory(ChurchRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new SermonListViewModel(mRepository);
    }
}

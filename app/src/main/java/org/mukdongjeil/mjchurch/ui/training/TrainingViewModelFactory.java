package org.mukdongjeil.mjchurch.ui.training;

import org.mukdongjeil.mjchurch.data.ChurchRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class TrainingViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChurchRepository mRepository;

    public TrainingViewModelFactory(ChurchRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new TrainingViewModel(mRepository);
    }
}

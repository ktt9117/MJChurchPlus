package org.mukdongjeil.mjchurch.ui.introduce;

import org.mukdongjeil.mjchurch.data.ChurchRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class IntroduceViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChurchRepository mRepository;

    public IntroduceViewModelFactory(ChurchRepository repository) {
        this.mRepository = repository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new IntroduceViewModel(mRepository);
    }
}

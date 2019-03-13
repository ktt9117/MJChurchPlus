package org.mukdongjeil.mjchurch.ui.sermon_detail;

import org.mukdongjeil.mjchurch.data.ChurchRepository;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SermonDetailActivityViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final ChurchRepository mRepository;
    private String bbsNo;

    public SermonDetailActivityViewModelFactory(ChurchRepository repository, String bbsNo) {
        this.mRepository = repository;
        this.bbsNo = bbsNo;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new SermonDetailActivityViewModel(mRepository, bbsNo);
    }
}

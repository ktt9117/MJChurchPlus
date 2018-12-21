package org.mukdongjeil.mjchurch.ui.sermons;

import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class SermonListViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<List<SermonEntity>> mSermonList;

    public SermonListViewModel(ChurchRepository repository) {
        mRepository = repository;
        mSermonList = mRepository.getSermonList();
    }

    public LiveData<List<SermonEntity>> getSermonList() {
        return mSermonList;
    }
}

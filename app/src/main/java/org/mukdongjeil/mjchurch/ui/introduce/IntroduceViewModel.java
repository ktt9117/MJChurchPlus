package org.mukdongjeil.mjchurch.ui.introduce;

import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class IntroduceViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<List<IntroduceEntity>> mIntroduceList;

    public IntroduceViewModel(ChurchRepository repository) {
        mRepository = repository;
        mIntroduceList = mRepository.getIntroduceList();
    }

    public LiveData<List<IntroduceEntity>> getIntroduceList() {
        return mIntroduceList;
    }
}

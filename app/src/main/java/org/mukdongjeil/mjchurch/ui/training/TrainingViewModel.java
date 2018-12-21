package org.mukdongjeil.mjchurch.ui.training;

import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class TrainingViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<List<TrainingEntity>> mList;

    public TrainingViewModel(ChurchRepository repository) {
        mRepository = repository;
        mList = mRepository.getTrainingList();
    }

    public LiveData<List<TrainingEntity>> getTrainingList() {
        return mList;
    }
}

package org.mukdongjeil.mjchurch.ui.training;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.ImageEntity;
import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class TrainingViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<List<TrainingEntity>> mList;
    private final List<ImageEntity> mLocalList;

    public TrainingViewModel(ChurchRepository repository) {
        mRepository = repository;
        mList = mRepository.getTrainingList();
        mLocalList = new ArrayList<>();
        setupLocalItem();
    }

    public LiveData<List<TrainingEntity>> getTrainingList() {
        return mList;
    }

    public List<ImageEntity> getLocalTrainingList() {
        return mLocalList;
    }

    private void setupLocalItem() {
        mLocalList.add(new ImageEntity("양육과훈련", R.drawable.training_overview));
        mLocalList.add(new ImageEntity("성경공부", R.drawable.training_study));
        mLocalList.add(new ImageEntity("양육", R.drawable.training_rear));
        mLocalList.add(new ImageEntity("마더 와이즈", R.drawable.training_motherwise));
        mLocalList.add(new ImageEntity("일대일제자양육", R.drawable.training_diciples));
        mLocalList.add(new ImageEntity("아와나", R.drawable.training_awana));
    }
}

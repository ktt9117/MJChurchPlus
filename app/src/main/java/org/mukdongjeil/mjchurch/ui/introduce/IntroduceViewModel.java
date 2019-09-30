package org.mukdongjeil.mjchurch.ui.introduce;

import org.mukdongjeil.mjchurch.R;
import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.ImageEntity;
import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class IntroduceViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<List<IntroduceEntity>> mIntroduceList;
    private final List<ImageEntity> mLocalIntroduceList;

    public IntroduceViewModel(ChurchRepository repository) {
        mRepository = repository;
        mIntroduceList = mRepository.getIntroduceList();
        mLocalIntroduceList = new ArrayList<>();
        setupLocalItem();
    }

    public LiveData<List<IntroduceEntity>> getIntroduceList() {
        return mIntroduceList;
    }

    public List<ImageEntity> getLocalIntroduceList() {
        return mLocalIntroduceList;
    }

    private void setupLocalItem() {
        mLocalIntroduceList.add(new ImageEntity("교회소개", R.drawable.introduce_church));
        mLocalIntroduceList.add(new ImageEntity("교회연혁", R.drawable.introduce_history));
        mLocalIntroduceList.add(new ImageEntity("찾아오시는길", R.drawable.introduce_map));
        mLocalIntroduceList.add(new ImageEntity("예배시간안내", R.drawable.introduce_timetable));
        mLocalIntroduceList.add(new ImageEntity("섬김의 동역자", R.drawable.introduce_people));
    }
}

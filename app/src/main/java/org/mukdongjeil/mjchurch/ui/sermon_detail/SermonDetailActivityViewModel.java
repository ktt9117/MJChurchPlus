package org.mukdongjeil.mjchurch.ui.sermon_detail;

import org.mukdongjeil.mjchurch.data.ChurchRepository;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.ReplyEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class SermonDetailActivityViewModel extends ViewModel {

    private final ChurchRepository mRepository;
    private final LiveData<SermonEntity> mSermonEntity;
    private final LiveData<List<ReplyEntity>> mSermonReplyList;

    public SermonDetailActivityViewModel(ChurchRepository repository, String bbsNo) {
        mRepository = repository;
        mSermonEntity = mRepository.getSermonEntity(bbsNo);
        mSermonReplyList = mRepository.getSermonReplyList(bbsNo);
    }

    public LiveData<SermonEntity> getSermonEntity() {
        return mSermonEntity;
    }

    public LiveData<List<ReplyEntity>> getSermonReplyList() {
        return mSermonReplyList;
    }

    public void addReply(String bbsNo, ReplyEntity entity) {
        mRepository.addSermonReply(bbsNo, entity);
    }
}

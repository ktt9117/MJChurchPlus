package org.mukdongjeil.mjchurch.data.database.dao;

import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonReplyEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SermonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SermonEntity... sermon);

    @Query("SELECT * FROM sermon ORDER BY bbsNo DESC")
    LiveData<List<SermonEntity>> getSermonList();

    @Query("SELECT * FROM sermon WHERE bbsNo = :bbsNo")
    LiveData<SermonEntity> getSermonEntity(int bbsNo);

    @Query("SELECT * FROM sermonReply WHERE bbsNo = :bbsNo ORDER BY date DESC")
    LiveData<List<SermonReplyEntity>> getSermonReplyList(int bbsNo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SermonReplyEntity sermonReply);

    @Delete
    void deleteReply(SermonReplyEntity entity);
}

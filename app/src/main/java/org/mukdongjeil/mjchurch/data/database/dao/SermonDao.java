package org.mukdongjeil.mjchurch.data.database.dao;

import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface SermonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSermon(SermonEntity... sermon);

    @Query("SELECT * FROM sermon ORDER BY bbsNo DESC")
    LiveData<List<SermonEntity>> getSermonList();

    @Query("SELECT * FROM sermon ORDER BY bbsNo DESC")
    List<SermonEntity> getSermonListNotLiveData();

    @Query("SELECT * FROM sermon WHERE bbsNo = :bbsNo")
    LiveData<SermonEntity> getSermonEntity(int bbsNo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIntroduce(IntroduceEntity... entity);

    @Query("SELECT * FROM introduce")
    LiveData<List<IntroduceEntity>> getIntroduceList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTraining(TrainingEntity... entity);

    @Query("SELECT * FROM training")
    LiveData<List<TrainingEntity>> getTrainingList();

}

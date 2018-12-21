package org.mukdongjeil.mjchurch.data.database;

import android.content.Context;

import org.mukdongjeil.mjchurch.data.database.dao.SermonDao;
import org.mukdongjeil.mjchurch.data.database.entity.IntroduceEntity;
import org.mukdongjeil.mjchurch.data.database.entity.SermonEntity;
import org.mukdongjeil.mjchurch.data.database.entity.TrainingEntity;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {SermonEntity.class, IntroduceEntity.class, TrainingEntity.class},
            version = 1, exportSchema = false)
public abstract class ChurchDatabase extends RoomDatabase {
    public abstract SermonDao sermonDao();

    private static final String DATABASE_NAME = "church";

    private static final Object LOCK = new Object();
    private static volatile ChurchDatabase sInstance;

    public static ChurchDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(context.getApplicationContext(),
                            ChurchDatabase.class, ChurchDatabase.DATABASE_NAME).build();
                }
            }
        }

        return sInstance;
    }

}

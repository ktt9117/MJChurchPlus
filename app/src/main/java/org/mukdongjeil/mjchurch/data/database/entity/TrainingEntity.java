package org.mukdongjeil.mjchurch.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training")
public class TrainingEntity {

    @PrimaryKey
    @NonNull
    private String title;
    private String contentUri;

    public TrainingEntity(String title, String contentUri) {
        this.title = title;
        this.contentUri = contentUri;
    }

    public String getTitle() {
        return title;
    }

    public String getContentUri() {
        return contentUri;
    }
}

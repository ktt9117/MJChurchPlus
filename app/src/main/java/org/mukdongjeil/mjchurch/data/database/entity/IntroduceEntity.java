package org.mukdongjeil.mjchurch.data.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "introduce")
public class IntroduceEntity {

    @PrimaryKey
    @NonNull
    private String title;
    private String contentUri;

    public IntroduceEntity(String title, String contentUri) {
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

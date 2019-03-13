package org.mukdongjeil.mjchurch.data.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "sermon")
public class SermonEntity {

    @PrimaryKey
    private int bbsNo;
    private int sermonType;
    private String title;
    private String writer;
    private String date;
    private int viewCount;
    private String content;
    private String videoUrl;

    @Ignore
    public SermonEntity(){}

    // Constructor used by Room to create SermonEntity
    public SermonEntity(int bbsNo, int sermonType, String title, String writer, String date,
                        int viewCount, String content, String videoUrl) {
        this.bbsNo = bbsNo;
        this.sermonType = sermonType;
        this.title = title;
        this.writer = writer;
        this.date = date;
        this.viewCount = viewCount;
        this.content = content;
        this.videoUrl = videoUrl;
    }

    public int getBbsNo() {
        return bbsNo;
    }

    public int getSermonType() {
        return sermonType;
    }

    public String getTitle() {
        return title;
    }

    public String getWriter() {
        return writer;
    }

    public String getDate() {
        return date;
    }

    public int getViewCount() {
        return viewCount;
    }

    public String getContent() {
        return content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}

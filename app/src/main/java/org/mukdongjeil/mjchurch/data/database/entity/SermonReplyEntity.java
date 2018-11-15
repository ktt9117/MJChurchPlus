package org.mukdongjeil.mjchurch.data.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sermonReply")
public class SermonReplyEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int bbsNo;
    private String content;
    private String writer;
    private String date;

    // 'id' is PrimaryKey and has autogenerate attr. so you can pass '0' value always for id variable;
    public SermonReplyEntity(int id, int bbsNo, String content, String writer, String date) {
        this.id = id;
        this.bbsNo = bbsNo;
        this.content = content;
        this.writer = writer;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public int getBbsNo() {
        return bbsNo;
    }

    public String getContent() {
        return content;
    }

    public String getWriter() {
        return writer;
    }

    public String getDate() {
        return date;
    }
}

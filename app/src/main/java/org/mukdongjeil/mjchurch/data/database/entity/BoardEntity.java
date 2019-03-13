package org.mukdongjeil.mjchurch.data.database.entity;

public class BoardEntity {

    private String id;
    private User writer;
    private long createdAt;
    private int likeCount;
    private int viewCount;
    private String content;


    public BoardEntity() {}

    public BoardEntity(String id, User writer, long timeMillis, int likeCount, int viewCount, String content) {
        this.id = id;
        this.writer = writer;
        this.createdAt = timeMillis;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.content = content;
    }

    public String getId() { return id; }

    public User getWriter() {
        return writer;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public String getContent() {
        return content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCreateAt(long timeMillis) {
        this.createdAt = timeMillis;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

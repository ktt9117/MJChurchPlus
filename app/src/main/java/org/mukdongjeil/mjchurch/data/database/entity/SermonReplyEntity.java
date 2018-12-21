package org.mukdongjeil.mjchurch.data.database.entity;

import com.google.firebase.firestore.Exclude;

public class SermonReplyEntity {

    private String documentId;
    private long createdAt;
    private String content;
    private User writer;

    public SermonReplyEntity() {}

    public SermonReplyEntity(String content, User writer, long createdAt) {
        this.content = content;
        this.writer = writer;
        this.createdAt = createdAt;
    }

    @Exclude
    public String getDocumentId() { return documentId; }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getContent() {
        return content;
    }

    public User getWriter() {
        return writer;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

package org.mukdongjeil.mjchurch.data.database.entity;

public class User {
    private String uid;
    private String displayName;
    private String avatarPath;

    public User() {}

    public User(String uid, String displayName, String avatarPath) {
        this.uid = uid;
        this.displayName = displayName;
        this.avatarPath = avatarPath;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarPath() {
        return avatarPath;
    }
}

package org.mukdongjeil.mjchurch.data.database.entity;

public class ImageEntity {

    private String title;
    private int resourceId;

    public ImageEntity(String title, int resourceId) {
        this.title = title;
        this.resourceId = resourceId;
    }

    public String getTitle() {
        return title;
    }

    public Integer getResourceId() {
        return resourceId;
    }
}

package com.mr_w.resourceplus.model;

import java.io.Serializable;

public class CommonGroup implements Serializable {

    private String image, title, allMembers, id;

    public CommonGroup() {
    }

    public CommonGroup(String image, String title, String allMembers) {
        this.image = image;
        this.title = title;
        this.allMembers = allMembers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAllMembers() {
        return allMembers;
    }

    public void setAllMembers(String allMembers) {
        this.allMembers = allMembers;
    }
}

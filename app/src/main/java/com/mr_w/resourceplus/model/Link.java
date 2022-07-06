package com.mr_w.resourceplus.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Link implements Serializable {

    String url = "";
    String title;

    public Link() {
    }

    public Link(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

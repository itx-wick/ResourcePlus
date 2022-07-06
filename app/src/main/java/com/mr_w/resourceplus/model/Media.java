package com.mr_w.resourceplus.model;

import java.io.Serializable;

public class Media implements Serializable {

    private String path, type;

    public Media() {
    }

    public Media(String path, String type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

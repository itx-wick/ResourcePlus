package com.mr_w.resourceplus.model;

public class UnreadBadge {

    int position;
    int count;

    public UnreadBadge(int position, int count) {
        this.position = position;
        this.count = count;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

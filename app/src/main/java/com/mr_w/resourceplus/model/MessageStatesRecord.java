package com.mr_w.resourceplus.model;

import java.io.Serializable;

public class MessageStatesRecord implements Serializable {

    private String phoneId;
    private boolean isDelivered;
    private boolean isSeen;

    public MessageStatesRecord(String phoneId, boolean isDelivered, boolean isSeen) {
        this.phoneId = phoneId;
        this.isDelivered = isDelivered;
        this.isSeen = isSeen;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}

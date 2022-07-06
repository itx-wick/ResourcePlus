package com.mr_w.resourceplus.model;

import com.mr_w.resourceplus.model.users.Users;

public class CallLogs {

    private String _id;
    private Users sender;
    private Users receiver;
    private String dateTime;
    private boolean isMissed;
    private String callCategory;
    private long duration;

    public CallLogs() {
        _id = "";
    }

    public CallLogs(String _id, Users sender, Users receiver, String dateTime, boolean isMissed, String callCategory, long duration) {
        this._id = _id;
        this.sender = sender;
        this.receiver = receiver;
        this.dateTime = dateTime;
        this.isMissed = isMissed;
        this.callCategory = callCategory;
        this.duration = duration;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Users getSender() {
        return sender;
    }

    public void setSender(Users sender) {
        this.sender = sender;
    }

    public Users getReceiver() {
        return receiver;
    }

    public void setReceiver(Users receiver) {
        this.receiver = receiver;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isMissed() {
        return isMissed;
    }

    public void setMissed(boolean missed) {
        isMissed = missed;
    }

    public String getCallCategory() {
        return callCategory;
    }

    public void setCallCategory(String callCategory) {
        this.callCategory = callCategory;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}

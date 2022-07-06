package com.mr_w.resourceplus.model;

import java.io.Serializable;
import java.util.List;

public class ChatList implements Serializable {
    private String phoneID;
    private String name;
    private String number;
    private String image;
    private String message;
    private String status;
    private String createdAt;
    private String type;
    private List<String> members;

    public ChatList() {
    }

    public ChatList(String phoneID, String name, String number, String image, String message, String status, String createdAt) {
        this.phoneID = phoneID;
        this.name = name;
        this.number = number;
        this.image = image;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getPhoneID() {
        return phoneID;
    }

    public void setPhoneID(String phoneID) {
        this.phoneID = phoneID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

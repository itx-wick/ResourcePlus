package com.mr_w.resourceplus.model.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {

    private String userId;
    private String phoneId;
    private String name;
    private String phoneNo;
    private String photo;
    private String about;
    private List<String> communications = new ArrayList<>();

    public Users() {
    }

    public Users(String userId, String phoneId, String name, String phoneNo, String photo, String about, List<String> communications) {
        this.userId = userId;
        this.phoneId = phoneId;
        this.name = name;
        this.phoneNo = phoneNo;
        this.photo = photo;
        this.about = about;
        this.communications = communications;
    }

    public Users(String name, String phone) {
        this.name = name;
        this.phoneNo = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<String> getCommunications() {
        return communications;
    }

    public void setCommunications(List<String> communications) {
        this.communications = communications;
    }
}


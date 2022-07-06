package com.mr_w.resourceplus.model;

import com.mr_w.resourceplus.model.users.Users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Conversation implements Serializable {

    private String _id = null;
    private String title;
    private String type;
    private String image;
    private String createdAt;
    private String description;
    private String updatedAt;
    private String __v;
    private String creator;
    private List<String> admin = new ArrayList<>();
    private List<String> removedMembers = new ArrayList<>();
    private List<Users> members = new ArrayList<>();
    private MessageData message;

    public Conversation() {
    }

    public Conversation(String _id, String title, String type, String image, String createdAt, String description, String updatedAt, String __v, List<String> admin, List<Users> members, MessageData message) {
        this._id = _id;
        this.title = title;
        this.type = type;
        this.image = image;
        this.createdAt = createdAt;
        this.description = description;
        this.updatedAt = updatedAt;
        this.__v = __v;
        this.admin = admin;
        this.members = members;
        this.message = message;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getRemovedMembers() {
        return removedMembers;
    }

    public void setRemovedMembers(List<String> removedMembers) {
        this.removedMembers = removedMembers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String get__v() {
        return __v;
    }

    public void set__v(String __v) {
        this.__v = __v;
    }

    public List<String> getAdmin() {
        return admin;
    }

    public void setAdmin(List<String> admin) {
        this.admin = admin;
    }

    public List<Users> getMembers() {
        return members;
    }

    public void setMembers(List<Users> members) {
        this.members = members;
    }

    public MessageData getMessage() {
        return message;
    }

    public void setMessage(MessageData message) {
        this.message = message;
    }

    public static Comparator<Conversation> orderedList = new Comparator<Conversation>() {

        @Override
        public int compare(Conversation conversation, Conversation t1) {

            String date1 = conversation.message != null ? conversation.message.getDateTime() : conversation.getUpdatedAt();
            String date2 = t1.message != null ? t1.message.getDateTime() : t1.getUpdatedAt();

            return date2.compareTo(date1);
        }
    };

}

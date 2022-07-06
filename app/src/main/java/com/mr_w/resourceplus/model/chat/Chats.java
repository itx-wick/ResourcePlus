package com.mr_w.resourceplus.model.chat;

public class Chats{

    private int id;
    private String textMessage;
    private String dateTime;
    private String username;
    private String phone;
    private int status;
    private boolean isGroup;
    private int conversationID;
    private int senderID;
    private int groupID;
    private int recipientID;
    private String imageFile;
    private String videoThumbnailFile;
    private String videoFile;
    private String audioFile;
    private String documentFile;
    private boolean isFileUpload;
    private boolean isFileDownLoad;
    private String FileSize;

    public Chats() {
    }

    public Chats(String textMessage) {
        this.textMessage = textMessage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public int getConversationID() {
        return conversationID;
    }

    public void setConversationID(int conversationID) {
        this.conversationID = conversationID;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getGroupID() {
        return groupID;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getRecipientID() {
        return recipientID;
    }

    public void setRecipientID(int recipientID) {
        this.recipientID = recipientID;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getVideoThumbnailFile() {
        return videoThumbnailFile;
    }

    public void setVideoThumbnailFile(String videoThumbnailFile) {
        this.videoThumbnailFile = videoThumbnailFile;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getDocumentFile() {
        return documentFile;
    }

    public void setDocumentFile(String documentFile) {
        this.documentFile = documentFile;
    }

    public boolean isFileUpload() {
        return isFileUpload;
    }

    public void setFileUpload(boolean fileUpload) {
        isFileUpload = fileUpload;
    }

    public boolean isFileDownLoad() {
        return isFileDownLoad;
    }

    public void setFileDownLoad(boolean fileDownLoad) {
        isFileDownLoad = fileDownLoad;
    }

    public String getFileSize() {
        return FileSize;
    }

    public void setFileSize(String fileSize) {
        FileSize = fileSize;
    }
}

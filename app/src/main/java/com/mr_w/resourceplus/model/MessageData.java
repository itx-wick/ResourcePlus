package com.mr_w.resourceplus.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MessageData implements Serializable {

    public static TYPE getType(String type) {
        return type.equals("TEXT") ? TYPE.TEXT :
                type.equals("PICTURE") ? TYPE.PICTURE :
                        type.equals("AUDIO") ? TYPE.AUDIO :
                                type.equals("INFO") ? TYPE.INFO :
                                        type.equals("DOCUMENTS") ? TYPE.DOCUMENTS :
                                                type.equals("VIDEO") ? TYPE.VIDEO :
                                                        type.equals("GIF") ? TYPE.GIF :
                                                                type.equals("LINK") ? TYPE.LINK :
                                                                        type.equals("CONTACT") ? TYPE.CONTACT :
                                                                                TYPE.LOCATION;
    }

    public static boolean isTextualMessage(TYPE type) {
        return type.equals(TYPE.LINK) || type.equals(TYPE.INFO) || type.equals(TYPE.TEXT) || type.equals(TYPE.CONTACT);
    }

    public static boolean isMediaMessage(TYPE type) {
        return type.equals(TYPE.DOCUMENTS) ||
                type.equals(TYPE.AUDIO) ||
                type.equals(TYPE.VIDEO) ||
                type.equals(TYPE.GIF) ||
                type.equals(TYPE.PICTURE);
    }

    public enum TYPE {
        INFO,
        TEXT,
        PICTURE,
        AUDIO,
        VIDEO,
        LINK,
        DOCUMENTS,
        GIF,
        CONTACT,
        LOCATION
    }

    private String id = null;
    private TYPE type;
    private String url;
    private String status;
    private String text;
    private String dateTime;
    private String senderPhoneId;
    private String conversationId;
    private String fileSize;
    private List<MessageStatesRecord> statesRecords = new ArrayList<>();

    public MessageData() {
    }

    public MessageData(String id, TYPE type, String url, String status, String text, String dateTime, String senderPhoneId, String conversationId, String fileSize) {
        this.id = id;
        this.type = type;
        this.url = url;
        this.status = status;
        this.text = text;
        this.dateTime = dateTime;
        this.senderPhoneId = senderPhoneId;
        this.conversationId = conversationId;
        this.fileSize = fileSize;
    }

    public List<MessageStatesRecord> getStatesRecords() {
        return statesRecords;
    }

    public void setStatesRecords(List<MessageStatesRecord> statesRecords) {
        this.statesRecords = statesRecords;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getSenderPhoneId() {
        return senderPhoneId;
    }

    public void setSenderPhoneId(String senderPhoneId) {
        this.senderPhoneId = senderPhoneId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public static Comparator<MessageData> orderedList = new Comparator<MessageData>() {

        @Override
        public int compare(MessageData m1, MessageData m2) {

            String date1 = m1.getDateTime();
            String date2 = m2.getDateTime();

            return date2.compareTo(date1);
        }
    };

}

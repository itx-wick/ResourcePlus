package com.mr_w.resourceplus.model;

public class LocalContacts {

    private String contactId;
    private String number;
    private String name;
    private String lookupKey;

    public LocalContacts() {
        number = "";
    }

    public LocalContacts(String contactId, String number, String name, String lookupKey) {
        this.contactId = contactId;
        this.number = number;
        this.name = name;
        this.lookupKey = lookupKey;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
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

    public String getLookupKey() {
        return lookupKey;
    }

    public void setLookupKey(String lookupKey) {
        this.lookupKey = lookupKey;
    }
}

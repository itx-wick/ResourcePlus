package com.mr_w.resourceplus.model.users;

public class UserNumber {
    private String cc;
    private String number;

    public UserNumber(String cc, String number) {
        this.cc = cc;
        this.number = number;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}

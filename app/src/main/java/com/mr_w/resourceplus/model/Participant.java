package com.mr_w.resourceplus.model;

import com.mr_w.resourceplus.model.users.Users;

import java.io.Serializable;
import java.util.Comparator;

public class Participant implements Serializable, Comparable<Participant> {

    private Users user;
    boolean isAdmin = false;

    public Participant() {
    }

    public Participant(Users user, boolean isAdmin) {
        this.user = user;
        this.isAdmin = isAdmin;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public static Comparator<Participant> adminOnTop = (s1, s2) -> ((Boolean) s2.isAdmin()).compareTo(s1.isAdmin());

    @Override
    public int compareTo(Participant participant) {
        return this.user.getName().compareTo(participant.user.getName());
    }
}

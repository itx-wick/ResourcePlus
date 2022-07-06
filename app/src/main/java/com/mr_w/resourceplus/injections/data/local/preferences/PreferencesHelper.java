package com.mr_w.resourceplus.injections.data.local.preferences;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.model.users.Users;

public interface PreferencesHelper {

    Users getUserDetails();

    void setUserDetails(Users userDetails);

    void saveString(final String pRef, final String value);

    String getString(final String pRef);

    void saveBoolean(final String pRef, final boolean value);

    boolean getBoolean(final String pRef);

    void saveInt(final String pRef, final int value);

    int getInt(final String pRef);

    void clear();

}
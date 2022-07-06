package com.mr_w.resourceplus.injections.data.local.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.mr_w.resourceplus.injections.di.PreferenceInfo;
import com.mr_w.resourceplus.model.users.Users;

import javax.inject.Inject;

public class AppPreferencesHelper implements PreferencesHelper {

    private static final String TAG = "AppPreferencesHelper";

    public static final String PREF_NAME = "user_prefs";
    public static final String PREF_USER_INFO = "pre_user_info";
    public static final String PREF_USER_IS_LOGIN = "pre_user_is_login";
    public static final String PREF_CONTACTS_UPLOADED = "pre_contacts_uploaded";
    public static final String PREF_CHATS_LOADED = "pre_chats_loaded";
    public static final String PREF_LOCAL_CONTACTS_COUNT = "pref_contacts_count";
    public static final String PREF_BG_SYNCING = "pref_background_syncing";

    private final SharedPreferences mPrefs;

    @Inject
    public AppPreferencesHelper(Context context, @PreferenceInfo String prefFileName) {
        mPrefs = context.getSharedPreferences(prefFileName, Context.MODE_PRIVATE);
    }

    @Override
    public Users getUserDetails() {
        Gson gson = new Gson();
        String userDetailsStr = getString(PREF_USER_INFO);
        return gson.fromJson(userDetailsStr, Users.class);
    }

    @Override
    public void setUserDetails(Users userDetails) {
        Gson gson = new Gson();
        String userStr = gson.toJson(userDetails);
        saveString(PREF_USER_INFO, userStr);
    }

    @Override
    public void saveString(String pRef, String value) {
        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(pRef, value);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    @Override
    public String getString(String pRef) {
        return mPrefs.getString(pRef, null);
    }

    @Override
    public void saveBoolean(String pRef, boolean value) {
        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putBoolean(pRef, value);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    @Override
    public boolean getBoolean(String pRef) {
        return mPrefs.getBoolean(pRef, false);
    }

    @Override
    public void saveInt(String pRef, int value) {
        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putInt(pRef, value);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    @Override
    public int getInt(String pRef) {
        return mPrefs.getInt(pRef, -1);
    }

    @Override
    public void clear() {
        mPrefs.edit().clear().apply();
    }
}
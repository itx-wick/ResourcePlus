package com.mr_w.resourceplus.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mr_w.resourceplus.model.users.Users;

import java.lang.reflect.Type;
import java.util.List;

public class UserPreferences {

    private static final String TAG = UserPreferences.class.getSimpleName();

    public static final String PREF_USER_INFO = "pre_user_info";
    public static final String PREF_USER_IS_LOGIN = "pre_user_is_login";// boolean
    public static final String PREF_CONTACTS_UPLOADED = "pre_contacts_uploaded";// boolean
    public static final String PREF_CHATS_LOADED = "pre_chats_loaded";
    public static final String PREF_LOCAL_CONTACTS_COUNT = "pref_contacts_count";
    public static final String PREF_BG_SYNCING = "pref_background_syncing";
    public static final String groupChanged = "conv_changed";

    private static UserPreferences ourInstance = null;

    private static Context mContext = null;
    private SharedPreferences pref = null;


    private UserPreferences(String prefType) {
        pref = mContext.getSharedPreferences(prefType, Context.MODE_PRIVATE);
    }

    public static UserPreferences getInstance(Activity pContext, String prefName) {
        mContext = pContext;
        if (ourInstance == null) {
            ourInstance = new UserPreferences(prefName);
        }
        return ourInstance;
    }


    public Users getUserDetails() {
        Gson gson = new Gson();
        String userDetailsStr = getString(PREF_USER_INFO);
        return gson.fromJson(userDetailsStr, Users.class);
    }

    public void setUserDetails(Users userDetails) {
        Gson gson = new Gson();
        String userStr = gson.toJson(userDetails);
        saveString(PREF_USER_INFO, userStr);
    }


    public void saveString(final String pRef, final String value) {
        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(pRef, value);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }


    public String getString(final String pRef) {
        return pref.getString(pRef, null);
    }

    public void saveBoolean(final String pRef, final boolean value) {

        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(pRef, value);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    public boolean getBoolean(final String pRef) {
        return pref.getBoolean(pRef, false);
    }

    public void saveInt(final String pRef, final int value) {

        try {
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(pRef, value);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage());
        }
    }

    public int getInt(final String pRef) {
        return pref.getInt(pRef, -1);
    }

    public void saveArrayList(final String pRef, List<Users> list) {
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(pRef, json);
        editor.apply();
    }

    public List<Users> getArrayList(String key) {
        Users users;
        Gson gson = new Gson();
        String json = pref.getString(key, null);
//        if (json!=null){
        Type type = new TypeToken<List<Users>>() {
        }.getType();
        users = gson.fromJson(json, type);
//        }
        return (List<Users>) users;
    }

    public void clear() {
        pref.edit().clear().apply();
    }
}

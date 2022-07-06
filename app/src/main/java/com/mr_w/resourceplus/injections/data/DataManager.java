package com.mr_w.resourceplus.injections.data;

import com.mr_w.resourceplus.injections.data.local.db.DbHelper;
import com.mr_w.resourceplus.injections.data.local.preferences.PreferencesHelper;
import com.mr_w.resourceplus.injections.network.remote.ApiHelper;

import org.json.JSONObject;

public interface DataManager extends DbHelper, ApiHelper, PreferencesHelper {

    void saveUserInfo(
            JSONObject jsonObject);

}

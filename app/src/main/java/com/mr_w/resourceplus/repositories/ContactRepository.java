package com.mr_w.resourceplus.repositories;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
import com.mr_w.resourceplus.async.SyncContacts;
import com.mr_w.resourceplus.callbacks.GenericCallbacks;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.injections.network.remote.ApiEndPoints;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.server_call.GenericServerCalls;
import com.mr_w.resourceplus.storage.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ContactRepository {

    private static ContactRepository instance;
    private Database db;
    private Context context;
    private Activity activity;
    private List<Users> usersList = new ArrayList<>();
    private MutableLiveData<List<Users>> userMutableLiveData = null;

    public static ContactRepository getInstance() {
        if (instance == null)
            instance = new ContactRepository();
        return instance;
    }

    public void setContext(Context context) {
        this.context = context;
        db = Database.getInstance(context);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public MutableLiveData<List<Users>> getUsersList() {
        new SyncContacts(context, new BackgroundCallbacks<List<LocalContacts>>() {
            @Override
            public void onFailure() {
            }

            @Override
            public void onCompleted(List<LocalContacts> list) {
                userMutableLiveData = setUsersList(list);
                Intent i = new Intent("contacts_sync");
                i.putExtra("data", "true");
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }
        }).execute();
        return userMutableLiveData;
    }

    public MutableLiveData<List<Users>> setUsersList(List<LocalContacts> list) {

        usersList.clear();
        UserPreferences userPrefs = UserPreferences.getInstance(activity, "UserPrefs");
        UserPreferences syncedPrefs = UserPreferences.getInstance(activity, "SyncedPrefs");

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", list.get(i).getName());
                jsonObject.put("phoneNo", list.get(i).getNumber());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("number", userPrefs.getUserDetails().getPhoneNo());
            postData.put("contactList", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        GenericServerCalls genericServerCalls = new GenericServerCalls(context,
                ApiEndPoints.ENDPOINT_CONTACTS_VALIDATION,
                requestQueue,
                Request.Method.POST,
                false);

        genericServerCalls.jsonRequest(postData, new GenericCallbacks<JSONObject>() {

            @Override
            public void onJsonSuccess(JSONObject response) {
                super.onJsonSuccess(response);

                try {
                    boolean status = response.getBoolean("success");
                    if (status) {
                        JSONArray array = response.getJSONArray("existingContactsFromNumber1");

                        for (int i = 0; i < array.length(); i++) {
                            if (!array.getJSONObject(i).getString("_id").equals(userPrefs.getUserDetails().getPhoneId())) {
                                Users users = new Users();
                                users.setUserId(String.valueOf(i + 1));
                                users.setName(array.getJSONObject(i).getString("name"));
                                users.setPhoneNo(array.getJSONObject(i).getString("phoneNo"));
                                users.setPhoneId(array.getJSONObject(i).getString("_id"));
                                users.setPhoto(array.getJSONObject(i).has("image") ? array.getJSONObject(i).getString("image") : "");

                                if (!db.isUserExists(users.getPhoneId()))
                                    db.insertUser(users);
                                else db.updateUser(users);
                            }
                        }
                        syncedPrefs.saveBoolean(UserPreferences.PREF_CONTACTS_UPLOADED, true);
                        usersList = db.getAllUsersExceptMe(userPrefs.getUserDetails().getPhoneId());
                        userMutableLiveData = new MutableLiveData<>();
                        userMutableLiveData.setValue(usersList);
                    } else {
                        Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
        return userMutableLiveData;

    }

}

package com.mr_w.resourceplus.fragments.register_fragment;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.async.SyncContacts;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class RegisterViewModel extends BaseViewModel<RegisterNavigator> {

    private static final String TAG = "RegisterViewModel";
    private final DataManager dataManager;
    private WeakReference<StartActivity> activity;

    public RegisterViewModel(DataManager dataManager,
                             SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void setActivity(WeakReference<StartActivity> activity) {
        this.activity = activity;
    }

    public void uploadFile(JSONObject postData, File file, View v) {

        getNavigator().showProgressBar();

        getCompositeDisposable().add(getDataManager()
                .doUploadFile(file)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        try {
                            boolean status = response.getBoolean("success");
                            if (status) {
                                postData.put("image", response.getString("path"));
                                signUp(v, postData);
                            } else {
                                getNavigator().showToast(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            getNavigator().hideProgressBar();
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "" + throwable);
                }));

    }

    public void signUp(final View v, JSONObject params) {

        getNavigator().showProgressBar();

        getCompositeDisposable().add(getDataManager()
                .doSignUp(params)
                .doAfterSuccess(response -> {
                    JSONObject obj = new JSONObject();
                    obj.put("id", response.getString("userId"));
                    obj.put("phoneId", response.getString("phoneId"));
                    obj.put("image", params.getString("image"));
                    obj.put("name", params.getString("name"));
                    obj.put("number", params.getString("number"));
                    obj.put("userBio", params.getString("userBio"));
                    getDataManager().saveUserInfo(obj);
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        try {
                            boolean status = response.getBoolean("success");
                            if (status) {
                                getDataManager().saveBoolean(UserPreferences.PREF_USER_IS_LOGIN, true);
                                getNavigator().navigateForward(v);
                                getNavigator().hideProgressBar();

                                initializeContactsJob();
                            }
                        } catch (JSONException e) {
                            getNavigator().hideProgressBar();
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "signUp: " + throwable);
                }));
    }

    public void initializeContactsJob() {
        new SyncContacts(activity.get(), new BackgroundCallbacks<List<LocalContacts>>() {

            @Override
            public void onPreProcessing() {
                super.onPreProcessing();
                getDataManager().saveBoolean(UserPreferences.PREF_BG_SYNCING, true);
            }

            @Override
            public void onFailure() {
                getDataManager().saveBoolean(UserPreferences.PREF_BG_SYNCING, false);
            }

            @Override
            public void onCompleted(List<LocalContacts> list) {
                getDataManager().saveInt(UserPreferences.PREF_LOCAL_CONTACTS_COUNT, list.size());
                uploadContacts(list);
            }
        }).execute();
    }

    public void uploadContacts(List<LocalContacts> list) {

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
            postData.put("number", getDataManager().getUserDetails().getPhoneNo());
            postData.put("contactList", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doValidateContacts(postData)
                .doAfterSuccess(response -> {
                    try {
                        boolean status = response.getBoolean("success");
                        if (status) {
                            JSONArray array = response.getJSONArray("existingContactsFromNumber1");

                            for (int i = 0; i < array.length(); i++) {
                                if (!array.getJSONObject(i).getString("_id").equals(getDataManager().getUserDetails().getPhoneId())) {
                                    Users users = new Users();
                                    users.setUserId(String.valueOf(i + 1));
                                    users.setName(array.getJSONObject(i).getString("name"));
                                    users.setPhoneNo(array.getJSONObject(i).getString("phoneNo"));
                                    users.setPhoneId(array.getJSONObject(i).getString("_id"));
                                    users.setPhoto(array.getJSONObject(i).has("image") ? array.getJSONObject(i).getString("image") : "");

                                    getCompositeDisposable().add(getDataManager().insertUser(users)
                                            .subscribeOn(getSchedulerProvider().io())
                                            .observeOn(getSchedulerProvider().ui())
                                            .subscribe(success -> {
                                                if (success)
                                                    Log.d(TAG, "Insert User: Success");
                                            }, throwable -> {
                                                Log.d(TAG, "Insert User: " + throwable);
                                            }));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    getDataManager().saveBoolean(UserPreferences.PREF_CONTACTS_UPLOADED, true);
                    getDataManager().saveBoolean(UserPreferences.PREF_BG_SYNCING, false);
                    Intent i = new Intent("contacts_sync");
                    i.putExtra("data", "true");
                    LocalBroadcastManager.getInstance(activity.get()).sendBroadcast(i);
                }, throwable -> Log.d(TAG, "uploadContacts: " + throwable)));

    }

}

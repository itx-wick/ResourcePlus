package com.mr_w.resourceplus.fragments.phone_login;

import android.content.Intent;
import android.util.Log;
import android.view.View;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.async.SyncContacts;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.data.local.preferences.AppPreferencesHelper;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.users.Users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

public class LoginViewModel extends BaseViewModel<LoginNavigator> {

    private static final String TAG = "LoginViewModel";
    private final DataManager dataManager;
    private WeakReference<View> view;
    private WeakReference<StartActivity> activity;

    public LoginViewModel(DataManager dataManager,
                          SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
    }

    public void setActivity(WeakReference<StartActivity> activity) {
        this.activity = activity;
    }

    public void setView(WeakReference<View> view) {
        this.view = view;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void login(JSONObject postData) {

        getNavigator().showProgressBar();

        getCompositeDisposable().add(getDataManager()
                .doSignIn(postData)
                .doOnSuccess(response -> getDataManager()
                        .saveUserInfo(response))
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response.getBoolean("success")) {
                        Navigation.findNavController(view.get()).navigate(R.id.action_phoneLoginFragment_to_mainFragment);
                        initializeContactsJob();
                    } else
                        Navigation.findNavController(view.get()).navigate(R.id.action_phoneLoginFragment_to_verifyCodeFragment);
                    getNavigator().hideProgressBar();
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "Login Error: " + throwable);
                }));

    }

    private void initializeContactsJob() {

        new SyncContacts(activity.get(), new BackgroundCallbacks<List<LocalContacts>>() {

            @Override
            public void onPreProcessing() {
                super.onPreProcessing();
                getDataManager().saveBoolean(AppPreferencesHelper.PREF_BG_SYNCING, true);
            }

            @Override
            public void onFailure() {
                getDataManager().saveBoolean(AppPreferencesHelper.PREF_BG_SYNCING, false);
            }

            @Override
            public void onCompleted(List<LocalContacts> list) {
                getDataManager().saveInt(AppPreferencesHelper.PREF_LOCAL_CONTACTS_COUNT, list.size());
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
                    getDataManager().saveBoolean(AppPreferencesHelper.PREF_CONTACTS_UPLOADED, true);
                    getDataManager().saveBoolean(AppPreferencesHelper.PREF_BG_SYNCING, false);
                    Intent i = new Intent("contacts_sync");
                    i.putExtra("data", "true");
                    LocalBroadcastManager.getInstance(activity.get()).sendBroadcast(i);
                }));

    }

}

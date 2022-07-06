package com.mr_w.resourceplus.activities.settings_activity;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.users.Users;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingsViewModel extends BaseViewModel<SettingsNavigator> {

    private static final String TAG = "SettingsViewModel";
    private final DataManager dataManager;
    private MutableLiveData<List<String>> communications;

    public SettingsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
        communications = new MutableLiveData<>();
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void uploadFile(File file) {

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
                                JSONObject obj = new JSONObject();
                                obj.put("image", response.getString("path"));
                                updateUserProfile(obj, "image");
                            } else {
                                getNavigator().showToast(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "" + throwable);
                }));

    }

    public void updateUserProfile(JSONObject body, String type) {

        getNavigator().showProgressBar();

        try {
            body.put("id", getDataManager().getUserDetails().getUserId());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doUpdateProfile(body)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        try {
                            boolean status = response.getBoolean("success");
                            if (status) {
                                if (type.equals("name")) {
                                    Users obj = getDataManager().getUserDetails();
                                    obj.setName(body.getString("name"));
                                    getDataManager().setUserDetails(obj);
                                    getNavigator().emitName(body.getString("name"));
                                } else if (type.equals("about")) {
                                    Users obj = getDataManager().getUserDetails();
                                    obj.setAbout(body.getString("userBio"));
                                    getDataManager().setUserDetails(obj);
                                    getNavigator().emitAbout(body.getString("userBio"));
                                } else {
                                    Users obj = getDataManager().getUserDetails();
                                    obj.setPhoto(body.getString("image"));
                                    getDataManager().setUserDetails(obj);
                                    getNavigator().emitImage(body.getString("image"));
                                }
                            } else {
                                getNavigator().showToast(response.getString("message"));
                            }
                            getNavigator().hideProgressBar();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            getNavigator().hideProgressBar();
                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "updateUserProfile: " + throwable);
                }));
    }

    public LiveData<List<String>> getCommunications() {

        List<String> communicatedIds = new ArrayList<>();
        getCompositeDisposable().add(getDataManager()
                .getAllConversations()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(list -> {
                    if (list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            for (Users user : list.get(i).getMembers()) {
                                if (!user.getPhoneId().equals(getDataManager().getUserDetails().getPhoneId()) &&
                                        !communicatedIds.contains(user.getPhoneId())) {
                                    communicatedIds.add(user.getPhoneId());
                                }
                            }
                        }
                    }

                    getCompositeDisposable().add(getDataManager()
                            .getAllUsers()
                            .subscribeOn(getSchedulerProvider().io())
                            .observeOn(getSchedulerProvider().ui())
                            .subscribe(contacts -> {
                                if (contacts.size() > 0) {
                                    for (int i = 0; i < contacts.size(); i++) {
                                        if (!communicatedIds.contains(contacts.get(i).getPhoneId()) && !contacts.get(i).getPhoneId().equals(getDataManager().getUserDetails().getPhoneId()))
                                            communicatedIds.add(contacts.get(i).getPhoneId());
                                    }
                                    communications.setValue(communicatedIds);
                                }
                            }, throwable -> Log.d(TAG, "getCommunications: " + throwable)));

                }, throwable -> Log.d(TAG, "getCommunications: " + throwable)));

        return communications;
    }

    public void deleteAllConversations() {

        getCompositeDisposable().add(getDataManager()
                .deleteAllConversations()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "deleteAllConversations: Success");
                }, throwable -> Log.d(TAG, "deleteAllConversations: " + throwable)));

    }

    public void deleteAllMessages() {

        getCompositeDisposable().add(getDataManager()
                .deleteAllMessages()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "deleteAllMessages: Success");
                }, throwable -> Log.d(TAG, "deleteAllMessages: " + throwable)));

    }

    public void deleteAllUsers() {

        getCompositeDisposable().add(getDataManager()
                .deleteAllUsers()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "deleteAllUsers: Success");
                }, throwable -> Log.d(TAG, "deleteAllUsers: " + throwable)));

    }
}

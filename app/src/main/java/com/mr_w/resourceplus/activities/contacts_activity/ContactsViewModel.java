package com.mr_w.resourceplus.activities.contacts_activity;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ContactsViewModel extends BaseViewModel<ContactsNavigator> {

    private static final String TAG = "ContactsViewModel";
    private final DataManager dataManager;
    private WeakReference<ContactActivity> activity;
    private MutableLiveData<List<Users>> users;

    public ContactsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
        users = new MutableLiveData<>(new ArrayList<>());
    }

    public void setActivity(WeakReference<ContactActivity> activity) {
        this.activity = activity;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void updateList(List<Users> list) {
        users.setValue(list);
    }

    public MutableLiveData<List<Users>> getUsers() {
        return users;
    }

    public void updateList() {

        if (getDataManager().getBoolean(UserPreferences.PREF_CONTACTS_UPLOADED)) {
            getCompositeDisposable().add(getDataManager()
                    .getAllUsersExceptMe(getDataManager().getUserDetails().getPhoneId())
                    .doAfterNext(data -> users.postValue(data))
                    .subscribeOn(getSchedulerProvider().io())
                    .observeOn(getSchedulerProvider().ui())
                    .subscribe(data -> {
                        Log.d(TAG, "getUsers: Success");
                    }, throwable -> Log.d(TAG, "getUsers: " + throwable)));
        } else {
            synchronizeContacts();
        }

    }

    public void synchronizeContacts() {

        getNavigator().showProgress();

        new SyncContacts(activity.get(), new BackgroundCallbacks<List<LocalContacts>>() {
            @Override
            public void onFailure() {

            }

            @Override
            public void onCompleted(List<LocalContacts> list) {
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
                            List<Users> data = new ArrayList<>();

                            for (int i = 0; i < array.length(); i++) {
                                if (!array.getJSONObject(i).getString("_id").equals(getDataManager().getUserDetails().getPhoneId())) {
                                    Users users = new Users();
                                    users.setUserId(String.valueOf(i + 1));
                                    users.setName(array.getJSONObject(i).getString("name"));
                                    users.setPhoneNo(array.getJSONObject(i).getString("phoneNo"));
                                    users.setPhoneId(array.getJSONObject(i).getString("_id"));
                                    users.setPhoto(array.getJSONObject(i).has("image") ? array.getJSONObject(i).getString("image") : "");

                                    data.add(users);

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
                            users.postValue(data);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    getDataManager().saveBoolean(UserPreferences.PREF_CONTACTS_UPLOADED, true);
                    getNavigator().hideProgress();
                }));

    }

    public void updateUser(String phoneId, Users member) {

        getCompositeDisposable().add(getDataManager()
                .getUser(phoneId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> {
                    if (data != null) {
                        data.setName(member.getName());
                        data.setAbout(member.getAbout());
                        data.setPhoto(member.getPhoto());

                        getCompositeDisposable().add(getDataManager()
                                .insertUser(data)
                                .subscribeOn(getSchedulerProvider().io())
                                .observeOn(getSchedulerProvider().ui())
                                .subscribe(success -> {
                                    if (success) {
                                        Log.d(TAG, "updateUser: Success");
                                    }
                                }, throwable -> Log.d(TAG, "updateUser: " + throwable)));

                    }
                }, throwable -> Log.d(TAG, "updateUser: " + throwable)));

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

    public void deleteAllContacts() {

        getCompositeDisposable().add(getDataManager()
                .deleteAllContacts()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "deleteAllContacts: Success");
                }, throwable -> Log.d(TAG, "deleteAllContacts: " + throwable)));

    }

    public void openChatScreen(Users userObj) {

        getCompositeDisposable().add(getDataManager()
                .isConversationExists(getDataManager().getUserDetails().getPhoneId(), userObj.getPhoneId())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> {
                    getNavigator().navigateWithConversation(data);
                }, throwable -> {
                    getNavigator().navigateWithUsers(userObj);
                    Log.d(TAG, "openChatScreen: " + throwable);
                }));

        getNavigator().finishActivity();
    }

}

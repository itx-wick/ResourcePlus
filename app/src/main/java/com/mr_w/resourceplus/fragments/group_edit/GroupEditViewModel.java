package com.mr_w.resourceplus.fragments.group_edit;

import android.util.Log;

import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Conversation;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class GroupEditViewModel extends BaseViewModel<GroupEditNavigator> {

    private static final String TAG = "GroupEditViewModel";
    private Conversation conversation;
    private String type, phoneNumber;
    private WeakReference<ChatActivity> activity;

    public GroupEditViewModel(DataManager dataManager,
                              SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setActivity(WeakReference<ChatActivity> activity) {
        this.activity = activity;
    }

    public void updateGroup(JSONObject postData) {

        getCompositeDisposable().add(getDataManager()
                .doUpdateConversation(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        if (response.getBoolean("success")) {

                            getCompositeDisposable().add(getDataManager()
                                    .insertConversation(conversation)
                                    .subscribeOn(getSchedulerProvider().io())
                                    .observeOn(getSchedulerProvider().ui())
                                    .subscribe(success -> {
                                        if (success)
                                            Log.d(TAG, "Insert Conversation: Success");
                                    }, throwable -> {
                                        Log.d(TAG, "Insert Conversation: " + throwable);
                                    }));

                            if (type.equals("subject"))
                                activity.get().sendInfoToServer(phoneNumber + "/changed group's title to '" + postData.getString("title") + "'", conversation);
                            else
                                activity.get().sendInfoToServer(phoneNumber + "/changed group's description to '" + postData.getString("description") + "'", conversation);

                            getNavigator().checkKeyboard();
                            getNavigator().goBack();

                        }
                    }
                }, throwable -> {
                    Log.d(TAG, "" + throwable);
                }));

    }
}

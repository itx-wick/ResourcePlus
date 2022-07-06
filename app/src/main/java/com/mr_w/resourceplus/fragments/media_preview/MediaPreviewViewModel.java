package com.mr_w.resourceplus.fragments.media_preview;

import android.util.Log;

import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Conversation;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;

public class MediaPreviewViewModel extends BaseViewModel<MediaPreviewNavigator> {

    private static final String TAG = "MediaPreviewViewModel";
    private Conversation conversation;
    private WeakReference<ChatActivity> activity;
    private String phoneNumber;

    public MediaPreviewViewModel(DataManager dataManager,
                                 SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setActivity(WeakReference<ChatActivity> activity) {
        this.activity = activity;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void uploadFile(File file) {

        getNavigator().showProgressBar();

        getCompositeDisposable().add(getDataManager()
                .doUploadFile(file)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        if (response.getBoolean("success")) {
                            String url = response.getString("path");

                            JSONObject jsonObject1 = new JSONObject();
                            jsonObject1.put("image", url);
                            jsonObject1.put("conversationId", conversation.get_id());

                            conversation.setImage(url);
                            updateGroup(jsonObject1);
                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "" + throwable);
                }));

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

                            activity.get().sendInfoToServer(phoneNumber + "/changed group's display picture", conversation);

                            getNavigator().hideProgressBar();
                            getNavigator().goBack();

                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "" + throwable);
                }));

    }
}

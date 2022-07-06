package com.mr_w.resourceplus.fragments.gallery_picker;

import android.util.Log;

import com.google.gson.Gson;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.fragments.create_group.CreateGroupNavigator;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.users.Users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class GalleryPickerViewModel extends BaseViewModel<CreateGroupNavigator> {

    private static final String TAG = "CreateGroupViewModel";
    private List<Users> usersList;
    private String phoneNumber;

    public GalleryPickerViewModel(DataManager dataManager,
                                  SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }

    public void setUsersList(List<Users> usersList) {
        this.usersList = usersList;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void uploadFile(JSONObject postData, File file) {

        getNavigator().showProgressBar();

        getCompositeDisposable().add(getDataManager()
                .doUploadFile(file)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        if (response.getBoolean("success")) {
                            String path = response.getString("path");
                            postData.put("image", path);
                            createGroup(postData);
                        }
                    }
                }, throwable -> {
                    Log.d(TAG, "" + throwable);
                }));

    }

    public void createGroup(JSONObject postData) {

        getCompositeDisposable().add(getDataManager()
                .doCreateConversation(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        if (response.getBoolean("success")) {

                            String id = response.getString("conversation_id");

                            JSONArray admin = postData.getJSONArray("admin");
                            List<String> admins = new ArrayList<>();
                            for (int i = 0; i < admin.length(); i++) {
                                admins.add(admin.getString(i));
                            }

                            for (Users users : usersList) {
                                users.getCommunications().add(id);
                                getCompositeDisposable().add(getDataManager()
                                        .insertUser(users)
                                        .subscribeOn(getSchedulerProvider().io())
                                        .observeOn(getSchedulerProvider().ui())
                                        .subscribe(success1 -> {
                                            if (success1)
                                                Log.d(TAG, "Insert User: Success");
                                        }, throwable -> {
                                            getNavigator().hideProgressBar();
                                            Log.d(TAG, "Insert User: " + throwable);
                                        }));
                            }

                            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            Conversation conversation = new Conversation();
                            conversation.set_id(response.getString("conversation_id"));
                            conversation.setAdmin(admins);
                            conversation.setMembers(usersList);
                            conversation.setTitle(postData.getString("title"));
                            conversation.setType("group");
                            conversation.setImage(postData.getString("image"));
                            conversation.setCreatedAt(utcFormat.format(new Date()));
                            conversation.setCreator(postData.getString("creator"));
                            conversation.setRemovedMembers(new ArrayList<>());

                            MessageData messageData = new MessageData();
                            messageData.setText(phoneNumber + "/created this group");
                            messageData.setType(MessageData.TYPE.INFO);

                            messageData.setDateTime(utcFormat.format(new Date()));
                            messageData.setStatus("send");
                            messageData.setConversationId(conversation.get_id());
                            messageData.setSenderPhoneId(postData.getString("creator"));

                            createMessage(messageData, conversation);
                        }
                    }
                }, throwable -> {
                    getNavigator().hideProgressBar();
                    Log.d(TAG, "" + throwable);
                }));

    }

    private void createMessage(MessageData message, Conversation conversation) {

        getNavigator().showProgressBar();

        JSONObject postData = new JSONObject();
        try {
            postData.put("sender", message.getSenderPhoneId());
            postData.put("type", message.getType().toString());
            postData.put("status", message.getStatus());
            postData.put("conversationId", message.getConversationId());
            if (message.getType().equals(MessageData.TYPE.TEXT) || message.getType().equals(MessageData.TYPE.INFO))
                postData.put("text", message.getText());
            else {
                postData.put("url", message.getUrl());
                postData.put("fileSize", message.getFileSize());
                if (message.getText() != null)
                    if (!message.getText().isEmpty())
                        postData.put("text", message.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doCreateMessage(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        int code = response.getInt("status");
                        if (code == 200) {

                            message.setStatus("send");
                            message.setId(response.getString("_id"));
                            message.setDateTime(response.getString("createdAt"));
                            conversation.setMessage(message);

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("conversation", new Gson().toJson(conversation));
                            jsonObject.put("type2", "new_conversation");

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);

                            getCompositeDisposable().add(getDataManager()
                                    .insertConversation(conversation)
                                    .subscribeOn(getSchedulerProvider().io())
                                    .observeOn(getSchedulerProvider().ui())
                                    .subscribe(success -> {
                                        if (success)
                                            getCompositeDisposable().add(getDataManager()
                                                    .insertMessage(message)
                                                    .subscribeOn(getSchedulerProvider().io())
                                                    .observeOn(getSchedulerProvider().ui())
                                                    .subscribe(success1 -> {
                                                        if (success1)
                                                            Log.d(TAG, "Insert Message: Success");
                                                    }, throwable -> {
                                                        getNavigator().hideProgressBar();
                                                        Log.d(TAG, "Insert Message: " + throwable);
                                                    }));
                                    }, throwable -> {
                                        getNavigator().hideProgressBar();
                                        Log.d(TAG, "Insert Conversation: " + throwable);
                                    }));

                            getNavigator().navigateToChatScreen(conversation);
                        } else {
                            getNavigator().showToast(response.getString("message"));
                        }
                    }
                }, throwable -> {
                    getNavigator().showProgressBar();
                    Log.d(TAG, "" + throwable);
                }));

    }
}

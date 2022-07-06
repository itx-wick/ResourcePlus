package com.mr_w.resourceplus.activities.chat_activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Contact;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class ChatViewModel extends BaseViewModel<ChatNavigator> {

    private static final String TAG = "ChatViewModel";
    private final DataManager dataManager;
    private MutableLiveData<List<MessageData>> messages;
    private MutableLiveData<List<MessageData>> unreadMessages;
    private MutableLiveData<Conversation> conversation;
    private WeakReference<ChatActivity> activity;
    private MessageData message = null;

    public ChatViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;

        messages = new MutableLiveData<>(new ArrayList<>());
        unreadMessages = new MutableLiveData<>(new ArrayList<>());
        conversation = new MutableLiveData<>(null);
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void setActivity(WeakReference<ChatActivity> activity) {
        this.activity = activity;
    }

    public void setMessage(MessageData message) {
        this.message = message;
    }

    public MutableLiveData<List<MessageData>> getMessages() {
        return messages;
    }

    public void getMessagesFromServer(JSONObject postData) {
        try {
            deleteAllMessages(postData.getString("conversationId"));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doGetMessages(postData)
                .doAfterSuccess(response -> {
                    if (response.getBoolean("success")) {
                        JSONArray array = response.getJSONArray("messages");
                        List<MessageData> messages = new ArrayList<>();

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject jsonObject = array.getJSONObject(i);

                            MessageData message = new MessageData();
                            message.setId(jsonObject.has("_id") ? jsonObject.getString("_id") : "");
                            message.setType(MessageData.getType(jsonObject.getString("type")));
                            message.setStatus(jsonObject.has("status") ? jsonObject.getString("status") : "");
                            message.setText(jsonObject.has("text") ? jsonObject.getString("text") : "");
                            message.setUrl(jsonObject.has("url") ? jsonObject.getString("url") : "");
                            message.setFileSize(jsonObject.has("fileSize") ? jsonObject.getString("fileSize") : "");
                            message.setSenderPhoneId(jsonObject.has("sender") ? jsonObject.getString("sender") : "");
                            message.setConversationId(jsonObject.has("conversationId") ? jsonObject.getString("conversationId") : "");
                            message.setDateTime(jsonObject.getString("createdAt"));

                            getCompositeDisposable().add(getDataManager()
                                    .insertMessage(message)
                                    .subscribeOn(getSchedulerProvider().io())
                                    .observeOn(getSchedulerProvider().ui())
                                    .subscribe(success -> {
                                        if (success)
                                            Log.d(TAG, "getMessagesFromServer: Success");
                                    }, throwable -> Log.d(TAG, "getMessagesFromServer: " + throwable)));

                            messages.add(message);
                        }

                        this.messages.postValue(messages);
                    } else {
                        getNavigator().showToast(response.getString("message"));
                    }
                })
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response.getBoolean("success"))
                        Log.d(TAG, "getMessagesFromServer: True");
                    else
                        Log.d(TAG, "getMessagesFromServer: False");
                }, throwable -> Log.d(TAG, "getMessagesFromServer: " + throwable)));
    }

    public void updateMessagesList(String id) {

        getCompositeDisposable().add(getDataManager()
                .getMessages(id)
                .doAfterNext(messages::postValue)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> {
                    Log.d(TAG, "updateMessagesList: Success");
                }, throwable -> Log.d(TAG, "updateMessagesList: " + throwable)));

    }

    public void updateMessagesList() {
        messages.setValue(new ArrayList<>());
    }

    public void addMessage(MessageData message) {
        List<MessageData> messages = getMessages().getValue();
        messages.add(message);
        getMessages().setValue(messages);
    }

    public void updateListItem(int pos) {
        List<MessageData> messages = getMessages().getValue();
        messages.set(pos, getMessage(messages.get(pos).getId()));
        getMessages().setValue(messages);
    }

    public void updateListItem(int pos, MessageData message) {
        List<MessageData> messages = getMessages().getValue();
        messages.set(pos, message);
        getMessages().setValue(messages);
    }

    public MutableLiveData<Conversation> getConversation() {
        return conversation;
    }

    public void updateList(String conversationId) {

        getCompositeDisposable().add(getDataManager()
                .getConversation(conversationId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> {
                    conversation.setValue(data);
                    updateMessagesList(data.get_id());
                }, throwable -> Log.d(TAG, "getConversation: " + throwable)));
    }

    public LiveData<List<MessageData>> getUnreadMessages() {
        return unreadMessages;
    }

    public void getAllUnseenMessages(String id, String me) {

        getCompositeDisposable().add(getDataManager()
                .getUnreadMessages(id, me)
                .doAfterNext(unreadMessages::postValue)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> {
                    Log.d(TAG, "getAllUnseenMessages: Success");
                }, throwable -> Log.d(TAG, "getAllUnseenMessages: " + throwable)));

    }

    public void updateMessage(MessageData message) {

        getCompositeDisposable().add(getDataManager()
                .insertMessage(message)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "updateMessage: Success");
                }, throwable -> Log.d(TAG, "updateMessage: " + throwable)));

    }

    public void updateUser(Users user) {

        getCompositeDisposable().add(getDataManager()
                .insertUser(user)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "updateUser: Success");
                }, throwable -> Log.d(TAG, "updateUser: " + throwable)));

    }

    public void updateContact(LocalContacts contact) {

        getCompositeDisposable().add(getDataManager()
                .insertContact(contact)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "updateContact: Success");
                }, throwable -> Log.d(TAG, "updateContact: " + throwable)));

    }

    public void deleteAllMessages(String id) {

        getCompositeDisposable().add(getDataManager()
                .deleteAllMessages(id)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "deleteAllMessages: Success");
                }, throwable -> Log.d(TAG, "deleteAllMessages: " + throwable)));

    }

    public void updateConversation(Conversation conversation) {

        getCompositeDisposable().add(getDataManager()
                .insertConversation(conversation)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "updateConversation: Success");
                }, throwable -> Log.d(TAG, "updateConversation: " + throwable)));

    }

    public void uploadMessage(int pos) {

        JSONObject postData = new JSONObject();
        try {
            postData.put("sender", message.getSenderPhoneId());
            postData.put("type", message.getType().toString());
            postData.put("status", "send");
            postData.put("conversationId", message.getConversationId());
            if (message.getType().equals(MessageData.TYPE.TEXT) || message.getType().equals(MessageData.TYPE.INFO) || message.getType().equals(MessageData.TYPE.LINK))
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
                .doUploadMessage(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response.getBoolean("success")) {

                        getCompositeDisposable().add(getDataManager()
                                .deleteMessage(message.getId())
                                .subscribeOn(getSchedulerProvider().io())
                                .observeOn(getSchedulerProvider().ui())
                                .subscribe(success -> {
                                    if (success)
                                        Log.d(TAG, "uploadMessage: Success");
                                }, throwable -> Log.d(TAG, "uploadMessage: " + throwable)));

                        message.setStatus("send");
                        message.setId(response.getString("_id"));
                        message.setDateTime(response.getString("createdAt"));
                        activity.get().conversation.setMessage(message);

                        if (activity.get().isNewThread) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("conversation", new Gson().toJson(conversation));
                            jsonObject.put("type2", "new_conversation");

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);
                            activity.get().isNewThread = false;
                        } else {

                            postData.put("_id", message.getId());
                            postData.put("dateTime", message.getDateTime());
                            postData.put("conversation", new Gson().toJson(conversation));

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", postData);
                        }

                        updateMessage(message);
                        updateConversation(activity.get().conversation);

                        Intent i = new Intent("message_sent");
                        i.putExtra("data", pos);
                        i.putExtra("success", true);
                        LocalBroadcastManager.getInstance(activity.get()).sendBroadcast(i);
                    } else {
                        getNavigator().showToast(response.getString("message"));
                    }
                }, throwable -> {
                    message.setStatus("cancelled");
                    activity.get().conversation.setMessage(message);
                    getNavigator().showToast(throwable.getMessage());

                    updateMessage(message);
                    updateConversation(activity.get().conversation);

                    Intent i = new Intent("message_failed");
                    i.putExtra("data", pos);
                    i.putExtra("success", false);
                    LocalBroadcastManager.getInstance(activity.get()).sendBroadcast(i);
                    Log.d(TAG, "uploadMessage: " + throwable);
                }));

    }

    public void uploadFile(File file, int pos) {

        getCompositeDisposable().add(getDataManager()
                .doUploadFile(file)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        try {
                            boolean status = response.getBoolean("success");
                            if (status) {
                                String path = response.getString("path");
                                double size = response.getDouble("size");

                                String fileName = path.substring(path.lastIndexOf('/') + 1);
                                String directoryPath = Utils.getDirectoryPath(fileName);
                                File directory = new File(directoryPath);
                                if (!directory.exists())
                                    directory.mkdirs();
                                File newFile = new File(directoryPath + fileName);

                                Utils.copyFileUsingStream(file, newFile);
                                message.setFileSize(String.valueOf(size));
                                message.setUrl(path);
                                uploadMessage(pos);
                            } else {
                                getNavigator().showToast(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, throwable -> {
                    Log.d(TAG, "" + throwable);
                }));

    }

    public void cancelCall() {

        if (message != null) {
            message.setStatus("cancelled");
            if (message.getId() == null || message.getId().equals(""))
                message.setId(UUID.randomUUID().toString());
            activity.get().conversation.setMessage(message);
            updateMessage(message);
            updateConversation(activity.get().conversation);
        }
    }

    public void createConversation(Users user) {

        JSONObject postData = new JSONObject();
        Users me = getDataManager().getUserDetails();
        List<Users> members = new ArrayList<>();
        members.add(me);
        members.add(user);
        List<String> admins = new ArrayList<>();
        admins.add(me.getPhoneId());

        List<String> ids = new ArrayList<>();
        for (Users users : members) {
            ids.add(users.getPhoneId());
        }
        JSONArray temp = new JSONArray(ids);

        try {
            postData.put("title", user.getName());
            postData.put("admin", new JSONArray(admins));
            postData.put("type", true);
            postData.put("image", user.getPhoto());
            postData.put("members", temp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doCreateConversation(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    int code = response.getInt("status");
                    if (response.getBoolean("success")) {

                        String id = code == 200 ? response.getString("conversation_id") : response.getString("conversationId");
                        for (Users users : members) {
                            users.getCommunications().add(id);
                            updateUser(users);
                        }

                        @SuppressLint("SimpleDateFormat") DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                        Conversation conversation = new Conversation();
                        conversation.set_id(id);
                        conversation.setTitle(user.getName());
                        conversation.setAdmin(admins);
                        conversation.setMembers(members);
                        conversation.setType("one-to-one");
                        conversation.setImage(user.getPhoto());
                        conversation.setCreatedAt(utcFormat.format(new Date()));
                        message.setConversationId(conversation.get_id());
                        getNavigator().updateAdapterItems(message);
                    } else {
                        getNavigator().showToast(response.getString("message"));
                    }
                }, throwable -> {
                    getNavigator().showToast(throwable.getMessage());
                    Log.d(TAG, "createConversation: " + throwable);
                }));

    }

    public LocalContacts getContact(String phoneNumber) {
        try {
            return getDataManager().getContact(phoneNumber).blockingFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MessageData getMessage(String messageId) {
        try {
            return getDataManager().getMessage(messageId).blockingFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

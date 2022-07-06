package com.mr_w.resourceplus.fragments.chat_list;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.mr_w.resourceplus.callbacks.GenericCallbacks;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.data.local.preferences.AppPreferencesHelper;
import com.mr_w.resourceplus.injections.network.remote.ApiEndPoints;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.server_call.GenericServerCalls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatListViewModel extends BaseViewModel<ChatListNavigator> {

    private static final String TAG = "ChatListViewModel";
    private final DataManager dataManager;
    private MutableLiveData<List<Conversation>> conversations;

    public ChatListViewModel(DataManager dataManager,
                             SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
        conversations = new MutableLiveData<>();
    }

    public LiveData<List<Conversation>> getConversations() {

        getCompositeDisposable().add(getDataManager()
                .getAllConversations()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> {
                    if (data != null) {
                        if (data.size() > 0)
                            conversations.setValue(data);
                        else if (!getDataManager().getBoolean(AppPreferencesHelper.PREF_CHATS_LOADED))
                            getChatList();
                        else
                            getNavigator().hideRecycler();
                    }
                }, throwable -> {
                    Log.d(TAG, "Get Conversations: " + throwable);
                }));

        return conversations;
    }

    public void updateList() {
        conversations.postValue(getConversations().getValue());
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void getChatList() {

        JSONObject postData = new JSONObject();
        try {
            postData.put("phoneId", getDataManager().getUserDetails().getPhoneId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doGetConversations(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    try {
                        boolean status = response.getBoolean("success");
                        if (status) {
                            List<Conversation> list = new ArrayList<>();
                            JSONArray array = response.getJSONArray("conversations");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonObject = array.getJSONObject(i);

                                Conversation conversation = new Conversation();
                                MessageData message = new MessageData();

                                conversation.set_id(jsonObject.getString("_id"));
                                conversation.setImage(jsonObject.has("image") ? jsonObject.getString("image") : null);
                                conversation.setTitle(jsonObject.getString("title"));
                                conversation.setCreatedAt(jsonObject.getString("createdAt"));
                                conversation.setUpdatedAt(jsonObject.getString("updatedAt"));
                                conversation.set__v(jsonObject.getString("__v"));
                                conversation.setType(jsonObject.getBoolean("type") ? "one-to-one" : "group");
                                conversation.setDescription(jsonObject.has("description") ? jsonObject.getString("description") : null);
                                conversation.setCreator(jsonObject.has("creator") ? jsonObject.getString("creator") : null);

                                JSONArray admins = jsonObject.getJSONArray("admin");
                                List<String> admin = new ArrayList<>();
                                for (int j = 0; j < admins.length(); j++) {
                                    admin.add(admins.getString(j));
                                }
                                conversation.setAdmin(admin);

                                if (jsonObject.has("removedMembers")) {
                                    JSONArray removedMembers = jsonObject.getJSONArray("removedMembers");
                                    List<String> removedMember = new ArrayList<>();
                                    for (int j = 0; j < removedMembers.length(); j++) {
                                        removedMember.add(removedMembers.getString(j));
                                    }
                                    conversation.setRemovedMembers(removedMember);
                                }

                                JSONArray members = jsonObject.getJSONArray("members");
                                List<Users> member = new ArrayList<>();
                                for (int j = 0; j < members.length(); j++) {
                                    JSONObject object = members.getJSONObject(j);
                                    Users obj = new Users();
                                    obj.setPhoneId(object.getString("_id"));
                                    obj.setPhoto(object.has("image") ? object.getString("image") : "");
                                    obj.setName(object.getString("name"));
                                    obj.setPhoneNo(object.getString("phoneNo"));
                                    JSONArray list1 = object.getJSONArray("communications");
                                    List<String> temp = new ArrayList<>();
                                    for (int k = 0; k < list1.length(); k++) {
                                        temp.add(list1.getString(k));
                                    }
                                    obj.setCommunications(temp);
                                    member.add(obj);
                                }
                                conversation.setMembers(member);

                                if (!jsonObject.getJSONArray("message").isNull(0)) {
                                    JSONObject msg = jsonObject.getJSONArray("message").getJSONObject(0);
                                    message.setId(msg.has("_id") ? msg.getString("_id") : "");
                                    message.setStatus(msg.getString("status"));
                                    message.setSenderPhoneId(msg.getString("sender"));
                                    message.setConversationId(msg.getString("conversationId"));
                                    message.setDateTime(msg.getString("createdAt"));
                                    message.setType(MessageData.getType(msg.getString("type")));

                                    if (MessageData.isTextualMessage(message.getType())) {
                                        message.setText(msg.getString("text"));
                                    } else if (MessageData.isMediaMessage(message.getType())) {
                                        if (msg.has("text"))
                                            message.setText(msg.getString("text"));
                                        message.setUrl(msg.getString("url"));
                                        message.setFileSize(msg.getString("fileSize"));
                                    } else
                                        message.setText(msg.getString("text"));
                                    conversation.setMessage(message);
                                }

                                getMessages(conversation.get_id());
                                list.add(conversation);

                                getCompositeDisposable().add(getDataManager()
                                        .insertConversation(conversation)
                                        .subscribeOn(getSchedulerProvider().io())
                                        .observeOn(getSchedulerProvider().ui())
                                        .subscribe(success -> {
                                            if (success)
                                                Log.d(TAG, "getChatList: Success");
                                        }, throwable -> {
                                            getNavigator().checkRefreshing();
                                            Log.d(TAG, "getChatList: " + throwable);
                                        }));
                            }
                            getDataManager().saveBoolean(AppPreferencesHelper.PREF_CHATS_LOADED, true);
                            conversations.setValue(list);
                            getNavigator().checkRefreshing();

                        } else {
                            getNavigator().showToast(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    getNavigator().checkRefreshing();
                    Log.d(TAG, "getChatList: " + throwable);
                }));
    }

    private void getMessages(String id) {

        getCompositeDisposable().add(getDataManager()
                .deleteAllMessages(id)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "getMessages: Success");
                }, throwable -> Log.d(TAG, "getMessages: " + throwable)));

        JSONObject postData = new JSONObject();
        try {
            postData.put("conversationId", id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doGetMessages(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray array = response.getJSONArray("messages");
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
                                            .subscribe(success1 -> {
                                                if (success1)
                                                    Log.d(TAG, "getMessages: Success");
                                            }, throwable -> Log.d(TAG, "getMessages: " + throwable)));
                                }
                            } else
                                getNavigator().showToast(response.getString("message"));
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }, throwable -> Log.d(TAG, "getMessages: " + throwable)));
    }
}

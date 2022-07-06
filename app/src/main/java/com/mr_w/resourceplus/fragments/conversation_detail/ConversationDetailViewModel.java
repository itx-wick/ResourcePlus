package com.mr_w.resourceplus.fragments.conversation_detail;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.CommonGroup;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.mr_w.resourceplus.ResourcePlusApplication.getContext;

public class ConversationDetailViewModel extends BaseViewModel<ConversationDetailNavigator> {

    private static final String TAG = "ConversationDetailViewM";
    private final MutableLiveData<List<Media>> mediaList;
    private final MutableLiveData<List<Participant>> participantList;
    private final MutableLiveData<List<CommonGroup>> commonGroupList;
    private final DataManager dataManager;

    private ConversationDetailFragment fragment;
    private Conversation conversation;
    private Users user, me;

    public ConversationDetailViewModel(DataManager dataManager,
                                       SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);

        this.dataManager = dataManager;
        mediaList = new MutableLiveData<>(new ArrayList<>());
        participantList = new MutableLiveData<>(new ArrayList<>());
        commonGroupList = new MutableLiveData<>(new ArrayList<>());
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void setFragment(WeakReference<ConversationDetailFragment> fragment) {
        this.fragment = fragment.get();
    }

    public void fetchCommonGroups() {
        getCompositeDisposable().add(getDataManager()
                .getAllCommonGroups(me, user)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(commonGroup -> {
                    if (commonGroup != null) {
                        commonGroupList.setValue(commonGroup);
                    }
                }, throwable -> {
                    Log.d(TAG, "medias: " + throwable);
                }));
    }

    public void fetchParticipants() {
        getCompositeDisposable().add(getDataManager()
                .getAllParticipants(conversation)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(participant -> {
                    if (participant != null) {
                        participantList.setValue(participant);
                    }
                }, throwable -> {
                    Log.d(TAG, "participants: " + throwable);
                }));
    }

    public void fetchMedia() {
        getCompositeDisposable().add(getDataManager()
                .getAllMedia(conversation.get_id())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(questionList -> {
                    if (questionList != null) {
                        mediaList.setValue(questionList);
                    }
                }, throwable -> {
                    Log.d(TAG, "medias: " + throwable);
                }));

    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public void setMe(Users me) {
        this.me = me;
    }

    public LiveData<List<Media>> getMediaList() {
        return mediaList;
    }

    public LiveData<List<Participant>> getParticipantList() {
        return participantList;
    }

    public LiveData<List<CommonGroup>> getCommonGroupList() {
        return commonGroupList;
    }

    //region Participants List Functions
    public void addParticipant(Participant participant) {
        List<Participant> participants = participantList.getValue();
        participants.add(participant);
        participantList.setValue(participants);
    }

    public void updateParticipant(Participant participant, int i) {
        List<Participant> participants = participantList.getValue();
        participants.set(i, participant);
        participantList.setValue(participants);
    }

    public void updateParticipantList(List<Participant> participant) {
        participantList.setValue(participant);
    }
    //endregion

    //region Data Setter
    public void insertConversation(Conversation conversation) {
        getCompositeDisposable().add(getDataManager()
                .insertConversation(conversation)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "insertConversation: Success");
                }, throwable -> Log.d(TAG, "insertConversation: " + throwable)));
    }

    public void insertMessage(MessageData messageData) {
        getCompositeDisposable().add(getDataManager()
                .insertMessage(messageData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "insertMessage: Success");
                }, throwable -> Log.d(TAG, "insertMessage: " + throwable)));
    }

    public void insertUser(Users user) {
        getCompositeDisposable().add(getDataManager()
                .insertUser(user)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "insertUser: Success");
                }, throwable -> Log.d(TAG, "insertUser: " + throwable)));
    }

    public void insertContact(LocalContacts contact) {
        getCompositeDisposable().add(getDataManager()
                .insertContact(contact)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(success -> {
                    if (success)
                        Log.d(TAG, "insertContact: Success");
                }, throwable -> Log.d(TAG, "insertContact: " + throwable)));
    }
    //endregion

    //region Data Getters
    public LocalContacts getContact(String phoneNumber) {
        try {
            return getDataManager().getContact(phoneNumber).blockingFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Conversation getConversation(String conversationId) {
        try {
            return getDataManager().getConversation(conversationId).blockingFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Conversation isConversationExists(String me, String other) {
        try {
            return getDataManager().isConversationExists(me, other).blockingFirst();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //endregion

    //region Navigators
    public void OnBack(View v) {
        getNavigator().goBack();
    }

    public void BackSearch(View v) {
        getNavigator().backSearch();
    }

    public void ClearSearch(View v) {
        getNavigator().clearSearch();
    }

    public void More(View v) {
        getNavigator().more(v);
    }

    public void OnEdit(View v) {
        getNavigator().edit(v);
    }

    public void OpenProfileImage(View v) {
        if (conversation.getImage() != null && !conversation.getImage().equals("null"))
            getNavigator().openImage(true);
        else if (user.getPhoto() != null && !user.getPhoto().equals("null")) {
            getNavigator().openImage(false);
        }
    }

    public void OpenInfo(View v) {
        getNavigator().openInfo();
    }

    public void OpenDescription(View v) {
        getNavigator().openDescription();
    }

    public void OpenMedia(View v) {
        getNavigator().openMedia();
    }

    public void OpenSearch(View v) {
        getNavigator().openSearch();
    }

    public void AddParticipants(View v) {
        getNavigator().addParticipants();
    }

    public void Invite(View v) {
        //TODO
    }

    public void Message(View v) {
        getNavigator().message();
    }

    public void AudioCall(View v) {
        getNavigator().audioCall();
    }

    public void VideoCall(View v) {
        getNavigator().videoCall();
    }

    public void BlockUser(View v) {
        getNavigator().blockUser();
    }
    //endregion

    //region Overriden Click Events
    public void commonGroupClick(int pos) {
        Conversation conversation = getConversation(commonGroupList.getValue().get(pos).getId());
        getNavigator().goToCommonGroup(conversation);
    }

    public void participantsClick(Participant participant, View v) {
        if (!getDataManager().getUserDetails().getPhoneId().equals(participant.getUser().getPhoneId())) {
            getNavigator().displayParticipantPopup(participant, v);
        }
    }
    //endregion

    //region API Calls
    public void shareContact(List<Users> list) {

        getNavigator().showShareDialog(list.size());

        for (Users users : list) {
            Conversation conversation = isConversationExists(me.getPhoneId(), users.getPhoneId());
            String text = user.getPhoto() + ";" + user.getPhoneNo() + ";" + user.getPhoneId();
            MessageData messageData = new MessageData();
            messageData.setText(text);
            messageData.setSenderPhoneId(me.getPhoneId());
            messageData.setType(MessageData.TYPE.CONTACT);
            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            messageData.setDateTime(utcFormat.format(new Date()));
            messageData.setStatus("sending");

            if (conversation == null) {
                fragment.isNewThread = true;
                createConversation(users, messageData, list.indexOf(users), list.size());
            } else {
                messageData.setConversationId(conversation.get_id());
                uploadMessage(messageData, list.indexOf(users), list.size());
            }
        }
    }

    public void createConversation(Users user, MessageData message, int index, int size) {

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
                            insertUser(users);
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

                        uploadMessage(message, index, size);

                    } else {
                        getNavigator().showToast(response.getString("message"));
                    }
                }, throwable -> {
                    getNavigator().showToast(throwable.getMessage());
                    Log.d(TAG, "createConversation: " + throwable);
                }));
    }

    public void uploadMessage(MessageData message, int index, int size) {

        getNavigator().updateShareDialog("Sharing contact to " + (index + 1) + " of " + size + " users");

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
                        conversation.setMessage(message);

                        if (fragment.isNewThread) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("conversation", new Gson().toJson(conversation));
                            jsonObject.put("type2", "new_conversation");

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);
                            fragment.isNewThread = false;
                        } else {

                            postData.put("_id", message.getId());
                            postData.put("dateTime", message.getDateTime());
                            postData.put("conversation", new Gson().toJson(conversation));

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", postData);
                        }

                        insertMessage(message);
                        insertConversation(conversation);

                        if (index == (size - 1))
                            getNavigator().hideShareDialog();
                    } else {
                        getNavigator().showToast(response.getString("message"));
                    }
                }, throwable -> {
                    getNavigator().showToast(throwable.getMessage());
                    Log.d(TAG, "uploadMessage: " + throwable);
                }));

    }

    public void makeParticipantAsAdmin(Participant participant) {

        getNavigator().showDialog();

        JSONObject body = new JSONObject();
        try {
            body.put("conversationId", conversation.get_id());
            body.put("newAdmin", participant.getUser().getPhoneId());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doUpdateConversation(body)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    if (response != null) {
                        if (response.getBoolean("success")) {

                            conversation.getAdmin().add(participant.getUser().getPhoneId());
                            participant.setAdmin(true);
                            updateParticipant(participant, getParticipantList().getValue().indexOf(participant));
                            insertConversation(conversation);
                            ((ChatActivity) fragment.getActivity()).sendInfoToServer(me.getPhoneNo() + "/changed group's display picture", conversation);

                            getNavigator().hideDialog();
                            getNavigator().updateViews();

                        }
                    }
                }, throwable -> {
                    getNavigator().hideDialog();
                    Log.d(TAG, "" + throwable);
                }));

    }

    public void removeAdmin(Participant participant) {

        JSONObject body = new JSONObject();
        try {
            body.put("conversationId", conversation.get_id());
            body.put("adminRemove", participant.getUser().getPhoneId());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doRemoveAdmin(body)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    try {
                        boolean status = response.getBoolean("success");
                        if (status) {
                            conversation.getAdmin().remove(participant.getUser().getPhoneId());
                            participant.setAdmin(false);
                            updateParticipant(participant, getParticipantList().getValue().indexOf(participant));
                            insertConversation(conversation);
                            ((ChatActivity) fragment.getActivity()).sendInfoToServer(me.getPhoneNo() + "/dismissed/" + participant.getUser().getPhoneNo() + "/as a group admin", conversation);
                            getNavigator().updateViews();
                        } else {
                            getNavigator().showToast(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    getNavigator().showToast(throwable.getMessage());
                    Log.d(TAG, "removeAdmin: " + throwable);
                }));

    }

    public void addMembersToConversation(List<Users> list) {

        getNavigator().showDialog();

        JSONObject postData = new JSONObject();
        List<String> memberIds = new ArrayList<>();
        for (Users users : list) {
            memberIds.add(users.getPhoneId());
        }

        try {
            postData.put("adminId", me.getPhoneId());
            postData.put("conversationId", conversation.get_id());
            postData.put("phoneId", new JSONArray(memberIds));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doAddConversationMember(postData)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    try {
                        boolean status = response.getBoolean("success");
                        if (status) {
                            StringBuilder names = new StringBuilder();
                            Toast.makeText(getContext(), list.size() + " members added to group", Toast.LENGTH_SHORT).show();
                            for (Users users : list) {
                                names.append(users.getPhoneNo());
                                if (!list.get(list.size() - 1).equals(users))
                                    names.append(",");
                                if (!conversation.getRemovedMembers().contains(users.getPhoneId()))
                                    conversation.getMembers().add(users);
                                else
                                    conversation.getRemovedMembers().remove(users.getPhoneId());
                                insertConversation(conversation);
                                Participant participant = new Participant();
                                participant.setUser(users);
                                addParticipant(participant);
                            }
                            ((ChatActivity) fragment.getActivity()).sendInfoToServer(me.getPhoneNo() + "/added/" + names.toString() + "/to group", conversation);
                            getNavigator().updateMembersTitle(conversation.getMembers().size() + " participants");

                            JSONObject obj = new JSONObject();
                            obj.put("ids", new Gson().toJson(memberIds));
                            obj.put("conversation", new Gson().toJson(conversation));

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", obj);

                        } else {
                            getNavigator().showToast(response.getString("message"));
                        }
                        getNavigator().hideDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    getNavigator().hideDialog();
                    getNavigator().showToast(throwable.getMessage());
                    Log.d(TAG, "removeAdmin: " + throwable);
                }));

    }

    public void removeMemberFromConversation(Participant participant) {

        getNavigator().showDialog();

        JSONObject body = new JSONObject();
        try {
            body.put("conversationId", conversation.get_id());
            body.put("memberRemove", participant.getUser().getPhoneId());
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        getCompositeDisposable().add(getDataManager()
                .doRemoveConversationMember(body)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(response -> {
                    try {
                        boolean status = response.getBoolean("success");
                        if (status) {
                            conversation.getAdmin().remove(participant.getUser().getPhoneId());
                            participant.setAdmin(false);
                            conversation.getRemovedMembers().add(participant.getUser().getPhoneId());
                            updateParticipant(participant, getParticipantList().getValue().indexOf(participant));
                            insertConversation(conversation);
                            ((ChatActivity) fragment.getActivity()).sendInfoToServer(me.getPhoneNo() + "/removed/" + participant.getUser().getPhoneNo(), conversation);
                            getNavigator().updateViews();
                        } else {
                            getNavigator().showToast(response.getString("message"));
                        }
                        getNavigator().hideDialog();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, throwable -> {
                    getNavigator().hideDialog();
                    getNavigator().showToast(throwable.getMessage());
                    Log.d(TAG, "removeAdmin: " + throwable);
                }));

    }
    //endregion

}

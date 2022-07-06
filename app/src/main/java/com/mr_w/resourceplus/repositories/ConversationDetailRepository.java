package com.mr_w.resourceplus.repositories;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.common.Common;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.CommonGroup;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ConversationDetailRepository {

    private static ConversationDetailRepository instance;
    private Database db;
    private Users user, me;
    private Conversation conversation;
    private final ArrayList<Media> mediaList = new ArrayList<>();
    private final ArrayList<Participant> participantList = new ArrayList<>();
    private final ArrayList<CommonGroup> commonGroups = new ArrayList<>();

    public void setContext(Context context) {
        db = Database.getInstance(context);
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

    public static ConversationDetailRepository getInstance() {
        if (instance == null)
            instance = new ConversationDetailRepository();
        return instance;
    }

    public MutableLiveData<List<Media>> getMediaList() {
        setMedia();
        MutableLiveData<List<Media>> mediaMutableLiveData = new MutableLiveData<>();
        mediaMutableLiveData.setValue(mediaList);
        return mediaMutableLiveData;
    }

    public MutableLiveData<List<Participant>> getParticipantList() {
        setParticipants();
        MutableLiveData<List<Participant>> participantMutableLiveData = new MutableLiveData<>();
        participantMutableLiveData.setValue(participantList);
        return participantMutableLiveData;
    }

    public MutableLiveData<List<CommonGroup>> getCommonGroups() {
        setCommonGroups();
        MutableLiveData<List<CommonGroup>> commonGroupMutableLiveData = new MutableLiveData<>();
        commonGroupMutableLiveData.setValue(commonGroups);
        return commonGroupMutableLiveData;
    }

    public void setMedia() {
        mediaList.clear();
        if (conversation == null)
            return;
        List<MessageData> messages = db.getAllMediaMessages(conversation.get_id());
        if (messages.size() > 0) {

            for (MessageData message : messages) {
                Media media = new Media(message.getUrl(), message.getType().toString());
                if (media.getType().equals("VIDEO") || media.getType().equals("AUDIO") || media.getType().equals("DOCUMENTS")) {
                    if (Utils.isFilePresent(message.getUrl())) {
                        mediaList.add(media);
                    }
                } else {
                    mediaList.add(media);
                }
            }
        }
    }

    public void setParticipants() {
        participantList.clear();
        if (conversation == null)
            return;
        for (Users user : conversation.getMembers()) {
            Participant participant = new Participant();
            participant.setUser(user);

            if (conversation.getAdmin().contains(user.getPhoneId())) {
                participant.setAdmin(true);
            }

            participantList.add(participant);
        }
    }

    public void setCommonGroups() {
        commonGroups.clear();
        List<String> communications = new ArrayList<>(user.getCommunications());
        List<String> myCommunications = new ArrayList<>(me.getCommunications());
        myCommunications.retainAll(communications);

        if (user.getCommunications() != null && me.getCommunications() != null) {
            if (myCommunications.size() > 0) {
                List<Conversation> upList = db.getAllGroupConversations();
                List<Conversation> list = new ArrayList<>();
                int i = 0;
                for (Conversation conversation : upList) {
                    for (Users users : conversation.getMembers()) {
                        if (users.getPhoneId().equals(me.getPhoneId()) || users.getPhoneId().equals(user.getPhoneId())) {
                            i++;
                        }
                    }
                    if (i == 2) {
                        list.add(conversation);
                    }
                }
                if (list.size() > 0) {
                    commonGroups.clear();
                    if (list.size() > 0) {
                        for (Conversation obj : list) {
                            CommonGroup common = new CommonGroup();
                            common.setId(obj.get_id());
                            common.setTitle(obj.getTitle());
                            common.setImage(obj.getImage());
                            StringBuilder allMembers = new StringBuilder();

                            for (Users users : obj.getMembers()) {
                                allMembers.append(users.getName());
                                if (users != obj.getMembers().get(obj.getMembers().size() - 1))
                                    allMembers.append(", ");
                            }
                            common.setAllMembers(allMembers.toString());
                            commonGroups.add(common);
                        }
                    }
                }
            }
        }
    }

}

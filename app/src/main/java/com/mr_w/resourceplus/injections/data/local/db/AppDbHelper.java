package com.mr_w.resourceplus.injections.data.local.db;

import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.CommonGroup;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

@Singleton
public class AppDbHelper implements DbHelper {

    private final Database mAppDatabase;

    @Inject
    public AppDbHelper(Database appDatabase) {
        this.mAppDatabase = appDatabase;
    }

    @Override
    public Observable<Conversation> isConversationExists(String id, String other) {
        return Observable.fromCallable(new Callable<Conversation>() {
            @Override
            public Conversation call() throws Exception {
                return mAppDatabase.isConversationExists(id, other);
            }
        });
    }

    @Override
    public Observable<List<Conversation>> getAllConversations() {
        return Observable.fromCallable(new Callable<List<Conversation>>() {
            @Override
            public List<Conversation> call() throws Exception {
                return mAppDatabase.getAllConversations();
            }
        });
    }

    @Override
    public Observable<List<MessageData>> getUnreadMessages(String id, String me) {
        return Observable.fromCallable(new Callable<List<MessageData>>() {
            @Override
            public List<MessageData> call() throws Exception {
                return mAppDatabase.getUnreadMessages(id, me);
            }
        });
    }

    @Override
    public Observable<List<Users>> getAllUsers() {
        return Observable.fromCallable(new Callable<List<Users>>() {
            @Override
            public List<Users> call() throws Exception {
                return mAppDatabase.getAllUsers();
            }
        });
    }

    @Override
    public Observable<List<Users>> getAllUsersExceptMe(String phoneId) {
        return Observable.fromCallable(new Callable<List<Users>>() {
            @Override
            public List<Users> call() throws Exception {
                return mAppDatabase.getAllUsersExceptMe(phoneId);
            }
        });
    }

    @Override
    public Observable<List<Media>> getAllMedia(String id) {
        return Observable.fromCallable(new Callable<List<Media>>() {
            @Override
            public List<Media> call() throws Exception {
                List<Media> mediaList = new ArrayList<>();
                List<MessageData> messages = mAppDatabase.getAllMediaMessages(id);
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
                return mediaList;
            }
        });
    }

    @Override
    public Observable<List<Participant>> getAllParticipants(Conversation conversation) {
        return Observable.fromCallable(new Callable<List<Participant>>() {
            @Override
            public List<Participant> call() throws Exception {
                List<Participant> participantList = new ArrayList<>();
                for (Users user : conversation.getMembers()) {
                    Participant participant = new Participant();
                    participant.setUser(user);

                    if (conversation.getAdmin().contains(user.getPhoneId())) {
                        participant.setAdmin(true);
                    }

                    participantList.add(participant);
                }
                return participantList;
            }
        });
    }

    @Override
    public Observable<List<CommonGroup>> getAllCommonGroups(Users me, Users user) {
        return Observable.fromCallable(new Callable<List<CommonGroup>>() {
            @Override
            public List<CommonGroup> call() throws Exception {

                List<CommonGroup> commonGroups = new ArrayList<>();

                List<String> communications = new ArrayList<>(user.getCommunications());
                List<String> myCommunications = new ArrayList<>(me.getCommunications());
                myCommunications.retainAll(communications);

                if (user.getCommunications() != null && me.getCommunications() != null) {
                    if (myCommunications.size() > 0) {
                        List<Conversation> upList = mAppDatabase.getAllGroupConversations();
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
                return commonGroups;
            }
        });
    }

    @Override
    public Observable<List<Media>> getAllDocuments(String conversationId) {
        return Observable.fromCallable(new Callable<List<Media>>() {
            @Override
            public List<Media> call() throws Exception {
                List<Media> mediaList = new ArrayList<>();
                List<MessageData> messages = mAppDatabase.getAllDocumentsMessages(conversationId);
                if (messages.size() > 0) {

                    for (MessageData message : messages) {
                        Media media = new Media(message.getUrl(), message.getType().toString());
                        if (Utils.isFilePresent(message.getUrl())) {
                            media.setPath(message.getUrl() + ";" + message.getUrl().substring(message.getUrl().lastIndexOf('/') + 1) + ";" +
                                    message.getDateTime() + ";" + message.getFileSize());
                            mediaList.add(media);
                        }
                    }
                }
                return mediaList;
            }
        });
    }

    @Override
    public Observable<List<String>> getAllLinks(String conversationId) {
        return Observable.fromCallable(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                List<String> linkList = new ArrayList<>();
                List<MessageData> messages = mAppDatabase.getAllLinksMessages(conversationId);
                if (messages.size() > 0) {
                    for (MessageData message : messages) {
                        linkList.add(message.getText() + ";" + message.getDateTime());
                    }
                }
                return linkList;
            }
        });
    }

    @Override
    public Observable<List<Media>> getAllMultiMedia(String conversationId) {
        return Observable.fromCallable(new Callable<List<Media>>() {
            @Override
            public List<Media> call() throws Exception {
                List<Media> mediaList = new ArrayList<>();
                List<MessageData> messages = mAppDatabase.getAllMultiMediaMessages(conversationId);
                if (messages.size() > 0) {
                    for (MessageData message : messages) {
                        Media media = new Media(message.getUrl(), message.getType().toString());
                        if (media.getType().equals("VIDEO") || media.getType().equals("AUDIO")) {
                            if (Utils.isFilePresent(message.getUrl())) {
                                mediaList.add(media);
                            }
                        } else
                            mediaList.add(media);
                    }
                }
                return mediaList;
            }
        });
    }

    @Override
    public Observable<Conversation> getConversation(String conversationId) {
        return Observable.fromCallable(new Callable<Conversation>() {
            @Override
            public Conversation call() throws Exception {
                return mAppDatabase.getConversation(conversationId);
            }
        });
    }

    @Override
    public Observable<List<MessageData>> getMessages(String conversationId) {
        return Observable.fromCallable(new Callable<List<MessageData>>() {
            @Override
            public List<MessageData> call() throws Exception {
                return mAppDatabase.getAllMessages(conversationId);
            }
        });
    }

    @Override
    public Observable<MessageData> getMessage(String messageId) {
        return Observable.fromCallable(new Callable<MessageData>() {
            @Override
            public MessageData call() throws Exception {
                return mAppDatabase.getMessage(messageId);
            }
        });
    }

    @Override
    public Observable<Users> getUser(String phoneId) {
        return Observable.fromCallable(new Callable<Users>() {
            @Override
            public Users call() throws Exception {
                return mAppDatabase.getUser(phoneId);
            }
        });
    }

    @Override
    public Observable<LocalContacts> getContact(String phoneNumber) {
        return Observable.fromCallable(new Callable<LocalContacts>() {
            @Override
            public LocalContacts call() throws Exception {
                return mAppDatabase.getContact(phoneNumber);
            }
        });
    }

    @Override
    public Observable<Boolean> insertUser(Users user) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (mAppDatabase.isUserExists(user.getPhoneId()))
                    mAppDatabase.updateUser(user);
                else
                    mAppDatabase.insertUser(user);
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> insertContact(LocalContacts contact) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (mAppDatabase.isContactExists(contact.getNumber())) {
                    mAppDatabase.updateContact(contact);
                } else if (contact.getContactId() != null) {
                    if (mAppDatabase.isContactExists(contact.getContactId())) {
                        mAppDatabase.updateContact(contact);
                    }
                } else
                    mAppDatabase.insertContact(contact);
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> insertConversation(Conversation conversation) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (mAppDatabase.isConversationExists(conversation.get_id()))
                    mAppDatabase.updateConversation(conversation);
                else
                    mAppDatabase.insertConversation(conversation);
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> insertMessage(MessageData messageData) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (mAppDatabase.isMessageExists(messageData.getId()))
                    mAppDatabase.updateMessage(messageData);
                else
                    mAppDatabase.insertMessage(messageData);
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAllConversations() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.deleteAllConversations();
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAllMessages(String conversationId) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.deleteAllMessages(conversationId);
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteMessage(String messageId) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.deleteMessage(messageId);
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAllMessages() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.deleteAllMessages();
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAllUsers() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.deleteAllUsers();
                return true;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteAllContacts() {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.deleteAllContacts();
                return true;
            }
        });
    }
}
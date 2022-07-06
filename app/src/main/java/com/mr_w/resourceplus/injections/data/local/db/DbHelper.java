package com.mr_w.resourceplus.injections.data.local.db;

import com.mr_w.resourceplus.model.CommonGroup;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;

import java.util.List;

import io.reactivex.Observable;

public interface DbHelper {

    Observable<Conversation> isConversationExists(String id, String others);

    Observable<List<Conversation>> getAllConversations();

    Observable<List<MessageData>> getUnreadMessages(String id, String me);

    Observable<List<Users>> getAllUsers();

    Observable<List<Users>> getAllUsersExceptMe(String phoneId);

    Observable<List<Media>> getAllMedia(String conversationId);

    Observable<List<Participant>> getAllParticipants(Conversation conversation);

    Observable<List<CommonGroup>> getAllCommonGroups(Users me, Users user);

    Observable<List<Media>> getAllDocuments(String conversationId);

    Observable<List<String>> getAllLinks(String conversationId);

    Observable<List<Media>> getAllMultiMedia(String conversationId);

    Observable<Conversation> getConversation(String conversationId);

    Observable<List<MessageData>> getMessages(String conversationId);

    Observable<MessageData> getMessage(String messageId);

    Observable<Users> getUser(String userId);

    Observable<LocalContacts> getContact(String phoneNumber);

    Observable<Boolean> insertUser(Users user);

    Observable<Boolean> insertContact(LocalContacts contact);

    Observable<Boolean> insertConversation(Conversation conversation);

    Observable<Boolean> insertMessage(MessageData messageData);

    Observable<Boolean> deleteAllConversations();

    Observable<Boolean> deleteAllMessages(String conversationId);

    Observable<Boolean> deleteMessage(String messageId);

    Observable<Boolean> deleteAllMessages();

    Observable<Boolean> deleteAllUsers();

    Observable<Boolean> deleteAllContacts();

}
package com.mr_w.resourceplus.injections.data;

import com.mr_w.resourceplus.injections.data.local.db.DbHelper;
import com.mr_w.resourceplus.injections.data.local.preferences.PreferencesHelper;
import com.mr_w.resourceplus.injections.network.remote.ApiHelper;
import com.mr_w.resourceplus.model.CommonGroup;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

@Singleton
public class AppDataManager implements DataManager {

    private static final String TAG = "AppDataManager";

    private final ApiHelper mApiHelper;

    private final DbHelper mDbHelper;

    private final PreferencesHelper mPreferencesHelper;

    @Inject
    public AppDataManager(DbHelper dbHelper, PreferencesHelper preferencesHelper, ApiHelper apiHelper) {
        mDbHelper = dbHelper;
        mApiHelper = apiHelper;
        mPreferencesHelper = preferencesHelper;
    }

    @Override
    public Single<JSONObject> doSignIn(JSONObject jsonObject) {
        return mApiHelper.doSignIn(jsonObject);
    }

    @Override
    public Single<JSONObject> doSignUp(JSONObject jsonObject) {
        return mApiHelper.doSignUp(jsonObject);
    }

    @Override
    public Single<JSONObject> doUploadFile(File file) {
        return mApiHelper.doUploadFile(file);
    }

    @Override
    public Single<JSONObject> doCreateConversation(JSONObject jsonObject) {
        return mApiHelper.doCreateConversation(jsonObject);
    }

    @Override
    public Single<JSONObject> doCreateMessage(JSONObject jsonObject) {
        return mApiHelper.doCreateMessage(jsonObject);
    }

    @Override
    public Single<JSONObject> doUpdateConversation(JSONObject jsonObject) {
        return mApiHelper.doUpdateConversation(jsonObject);
    }

    @Override
    public Single<JSONObject> doUpdateProfile(JSONObject jsonObject) {
        return mApiHelper.doUpdateProfile(jsonObject);
    }

    @Override
    public Single<JSONObject> doValidateContacts(JSONObject jsonObject) {
        return mApiHelper.doValidateContacts(jsonObject);
    }

    @Override
    public Single<JSONObject> doGetConversations(JSONObject jsonObject) {
        return mApiHelper.doGetConversations(jsonObject);
    }

    @Override
    public Single<JSONObject> doGetMessages(JSONObject jsonObject) {
        return mApiHelper.doGetMessages(jsonObject);
    }

    @Override
    public Single<JSONObject> doUploadMessage(JSONObject jsonObject) {
        return mApiHelper.doUploadMessage(jsonObject);
    }

    @Override
    public Single<JSONObject> doRemoveAdmin(JSONObject jsonObject) {
        return mApiHelper.doRemoveAdmin(jsonObject);
    }

    @Override
    public Single<JSONObject> doAddConversationMember(JSONObject jsonObject) {
        return mApiHelper.doAddConversationMember(jsonObject);
    }

    @Override
    public Single<JSONObject> doRemoveConversationMember(JSONObject jsonObject) {
        return mApiHelper.doRemoveConversationMember(jsonObject);
    }

    @Override
    public Observable<Conversation> isConversationExists(String id, String others) {
        return mDbHelper.isConversationExists(id, others);
    }

    @Override
    public Observable<List<Conversation>> getAllConversations() {
        return mDbHelper.getAllConversations();
    }

    @Override
    public Observable<List<MessageData>> getUnreadMessages(String id, String me) {
        return mDbHelper.getUnreadMessages(id, me);
    }

    @Override
    public Observable<List<Users>> getAllUsers() {
        return mDbHelper.getAllUsers();
    }

    @Override
    public Observable<List<Users>> getAllUsersExceptMe(String phoneId) {
        return mDbHelper.getAllUsersExceptMe(phoneId);
    }

    @Override
    public Observable<List<Media>> getAllMedia(String conversationId) {
        return mDbHelper.getAllMedia(conversationId);
    }

    @Override
    public Observable<List<Participant>> getAllParticipants(Conversation conversation) {
        return mDbHelper.getAllParticipants(conversation);
    }

    @Override
    public Observable<List<CommonGroup>> getAllCommonGroups(Users me, Users user) {
        return mDbHelper.getAllCommonGroups(me, user);
    }

    @Override
    public Observable<List<Media>> getAllDocuments(String conversationId) {
        return mDbHelper.getAllDocuments(conversationId);
    }

    @Override
    public Observable<List<String>> getAllLinks(String conversationId) {
        return mDbHelper.getAllLinks(conversationId);
    }

    @Override
    public Observable<List<Media>> getAllMultiMedia(String conversationId) {
        return mDbHelper.getAllMultiMedia(conversationId);
    }

    @Override
    public Observable<Conversation> getConversation(String conversationId) {
        return mDbHelper.getConversation(conversationId);
    }

    @Override
    public Observable<List<MessageData>> getMessages(String conversationId) {
        return mDbHelper.getMessages(conversationId);
    }

    @Override
    public Observable<MessageData> getMessage(String messageId) {
        return mDbHelper.getMessage(messageId);
    }

    @Override
    public Observable<Users> getUser(String userId) {
        return mDbHelper.getUser(userId);
    }

    @Override
    public Observable<LocalContacts> getContact(String phoneNumber) {
        return mDbHelper.getContact(phoneNumber);
    }

    @Override
    public Observable<Boolean> insertUser(Users user) {
        return mDbHelper.insertUser(user);
    }

    @Override
    public Observable<Boolean> insertContact(LocalContacts contact) {
        return mDbHelper.insertContact(contact);
    }

    @Override
    public Observable<Boolean> insertConversation(Conversation conversation) {
        return mDbHelper.insertConversation(conversation);
    }

    @Override
    public Observable<Boolean> insertMessage(MessageData messageData) {
        return mDbHelper.insertMessage(messageData);
    }

    @Override
    public Observable<Boolean> deleteAllConversations() {
        return mDbHelper.deleteAllConversations();
    }

    @Override
    public Observable<Boolean> deleteAllMessages(String conversationId) {
        return mDbHelper.deleteAllMessages(conversationId);
    }

    @Override
    public Observable<Boolean> deleteMessage(String messageId) {
        return mDbHelper.deleteMessage(messageId);
    }

    @Override
    public Observable<Boolean> deleteAllMessages() {
        return mDbHelper.deleteAllMessages();
    }

    @Override
    public Observable<Boolean> deleteAllUsers() {
        return mDbHelper.deleteAllUsers();
    }

    @Override
    public Observable<Boolean> deleteAllContacts() {
        return mDbHelper.deleteAllContacts();
    }

    @Override
    public void saveUserInfo(JSONObject json) {
        Users users = new Users();
        try {
            users.setName(json.getString("name"));
            users.setPhoneId(json.getString("phoneId"));
            users.setUserId(json.getString("id"));
            users.setPhoneNo(json.getString("number"));
            users.setPhoto(json.has("image") ? json.getString("image") : "");
            users.setAbout(json.has("userBio") ? json.getString("userBio") : "");
            setUserDetails(users);
            saveBoolean(UserPreferences.PREF_USER_IS_LOGIN, true);
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Users getUserDetails() {
        return mPreferencesHelper.getUserDetails();
    }

    @Override
    public void setUserDetails(Users userDetails) {
        mPreferencesHelper.setUserDetails(userDetails);
    }

    @Override
    public void saveString(String pRef, String value) {
        mPreferencesHelper.saveString(pRef, value);
    }

    @Override
    public String getString(String pRef) {
        return mPreferencesHelper.getString(pRef);
    }

    @Override
    public void saveBoolean(String pRef, boolean value) {
        mPreferencesHelper.saveBoolean(pRef, value);
    }

    @Override
    public boolean getBoolean(String pRef) {
        return mPreferencesHelper.getBoolean(pRef);
    }

    @Override
    public void saveInt(String pRef, int value) {
        mPreferencesHelper.saveInt(pRef, value);
    }

    @Override
    public int getInt(String pRef) {
        return mPreferencesHelper.getInt(pRef);
    }

    @Override
    public void clear() {
        mPreferencesHelper.clear();
    }
}
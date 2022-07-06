package com.mr_w.resourceplus.injections.network.remote;

import org.json.JSONObject;

import java.io.File;

import io.reactivex.Single;

public interface ApiHelper {

    Single<JSONObject> doSignIn(JSONObject jsonObject);

    Single<JSONObject> doSignUp(JSONObject jsonObject);

    Single<JSONObject> doUploadFile(File file);

    Single<JSONObject> doCreateConversation(JSONObject jsonObject);

    Single<JSONObject> doCreateMessage(JSONObject jsonObject);

    Single<JSONObject> doUpdateConversation(JSONObject jsonObject);

    Single<JSONObject> doUpdateProfile(JSONObject jsonObject);

    Single<JSONObject> doValidateContacts(JSONObject jsonObject);

    Single<JSONObject> doGetConversations(JSONObject jsonObject);

    Single<JSONObject> doGetMessages(JSONObject jsonObject);

    Single<JSONObject> doUploadMessage(JSONObject jsonObject);

    Single<JSONObject> doRemoveAdmin(JSONObject jsonObject);

    Single<JSONObject> doAddConversationMember(JSONObject jsonObject);

    Single<JSONObject> doRemoveConversationMember(JSONObject jsonObject);

}

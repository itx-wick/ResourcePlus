package com.mr_w.resourceplus.injections.network.remote;

import com.rx2androidnetworking.Rx2AndroidNetworking;

import org.json.JSONObject;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class AppApiHelper implements ApiHelper {

    @Inject
    public AppApiHelper() {
    }

    @Override
    public Single<JSONObject> doSignIn(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_SIGN_IN)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doSignUp(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_SIGN_UP)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doUploadFile(File file) {
        return Rx2AndroidNetworking.upload(ApiEndPoints.ENDPOINT_FILE_UPLOAD)
                .addMultipartFile("file", file)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doCreateConversation(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_CONVERSATION_CREATE)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doCreateMessage(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_MESSAGES_CREATE)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doUpdateConversation(JSONObject jsonObject) {
        return Rx2AndroidNetworking.patch(ApiEndPoints.ENDPOINT_CONVERSATION_UPDATE)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doUpdateProfile(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_UPDATE_PROFILE)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doValidateContacts(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_CONTACTS_VALIDATION)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doGetConversations(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_CONVERSATION_LIST)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doGetMessages(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_MESSAGES_LIST)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doUploadMessage(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_MESSAGES_CREATE)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doRemoveAdmin(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_CONVERSATION_REMOVE_MEMBER)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doAddConversationMember(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_CONVERSATION_ADD_MEMBER)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }

    @Override
    public Single<JSONObject> doRemoveConversationMember(JSONObject jsonObject) {
        return Rx2AndroidNetworking.post(ApiEndPoints.ENDPOINT_CONVERSATION_REMOVE_MEMBER)
                .addJSONObjectBody(jsonObject)
                .build()
                .getJSONObjectSingle();
    }


}

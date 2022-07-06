package com.mr_w.resourceplus.fragments.create_group;

import com.androidnetworking.error.ANError;
import com.mr_w.resourceplus.model.Conversation;

public interface CreateGroupNavigator {

    void navigateToChatScreen(Conversation conversation);

    void showToast(String message);

    void handleError(ANError error);

    void showProgressBar();

    void hideProgressBar();

}

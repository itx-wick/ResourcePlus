package com.mr_w.resourceplus.activities.contacts_activity;

import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.users.Users;

public interface ContactsNavigator {

    void showProgress();

    void hideProgress();

    void navigateWithConversation(Conversation obj);

    void navigateWithUsers(Users obj);

    void finishActivity();

}

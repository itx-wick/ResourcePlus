package com.mr_w.resourceplus.fragments.conversation_detail;

import android.view.View;

import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.Participant;

public interface ConversationDetailNavigator {

    void goBack();

    void backSearch();

    void clearSearch();

    void more(View v);

    void edit(View v);

    void openImage(boolean type);

    void openInfo();

    void openDescription();

    void openMedia();

    void openSearch();

    void addParticipants();

    void message();

    void audioCall();

    void videoCall();

    void blockUser();

    void goToCommonGroup(Conversation conversation);

    void displayParticipantPopup(Participant participant, View v);

    void showShareDialog(int size);

    void updateShareDialog(String message);

    void hideShareDialog();

    void showDialog();

    void hideDialog();

    void showToast(String message);

    void updateViews();

    void updateMembersTitle(String message);
}

package com.mr_w.resourceplus.fragments.gallery_picker;

import com.androidnetworking.error.ANError;
import com.mr_w.resourceplus.model.Conversation;

public interface GalleryPickerNavigator {

    void navigateToChatScreen(Conversation conversation);

    void showToast(String message);

    void handleError(ANError error);

    void showProgressBar();

    void hideProgressBar();

}

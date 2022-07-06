package com.mr_w.resourceplus.activities.chat_activity;

import com.mr_w.resourceplus.model.MessageData;

public interface ChatNavigator {

    void showToast(String message);

    void updateAdapterItems(MessageData messageData);

}

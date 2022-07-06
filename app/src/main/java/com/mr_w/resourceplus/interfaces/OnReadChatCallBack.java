package com.mr_w.resourceplus.interfaces;

import com.mr_w.resourceplus.model.ChatList;

import java.util.List;

public interface OnReadChatCallBack {
    void onReadSuccess(List<ChatList> list);
    void onReadFailed();
}


package com.mr_w.resourceplus.activities.settings_activity;

public interface SettingsNavigator {

    void emitName(String name);

    void emitAbout(String about);

    void emitImage(String image);

    void showToast(String message);

    void showProgressBar();

    void hideProgressBar();

}

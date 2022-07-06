package com.mr_w.resourceplus.fragments.start_fragment;

import android.view.View;

public interface StartNavigator {

    void passToLogin(View v);

    void passToDashboard(View v);

    void showToast(String message);

}

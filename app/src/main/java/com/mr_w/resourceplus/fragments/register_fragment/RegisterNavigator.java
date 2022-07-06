package com.mr_w.resourceplus.fragments.register_fragment;

import android.view.View;

public interface RegisterNavigator {

    void showProgressBar();

    void hideProgressBar();

    void showToast(String message);

    void navigateForward(View v);
}

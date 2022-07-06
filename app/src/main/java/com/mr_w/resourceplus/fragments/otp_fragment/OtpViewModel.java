package com.mr_w.resourceplus.fragments.otp_fragment;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

public class OtpViewModel extends BaseViewModel<OtpNavigator> {

    private static final String TAG = "OtpViewModel";

    public OtpViewModel(DataManager dataManager,
                        SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}

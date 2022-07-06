package com.mr_w.resourceplus.fragments.camera_fragment;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

public class CameraViewModel extends BaseViewModel<CameraNavigator> {

    private static final String TAG = "CameraViewModel";

    public CameraViewModel(DataManager dataManager,
                           SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}

package com.mr_w.resourceplus.activities.start_activity;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

public class StartViewModel extends BaseViewModel<StartNavigator> {

    private static final String TAG = "StartViewModel";

    public StartViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}

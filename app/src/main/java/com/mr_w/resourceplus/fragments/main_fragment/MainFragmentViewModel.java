package com.mr_w.resourceplus.fragments.main_fragment;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

public class MainFragmentViewModel extends BaseViewModel<MainFragmentNavigator> {

    private static final String TAG = "MainFragmentViewModel";

    public MainFragmentViewModel(DataManager dataManager,
                                 SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}

package com.mr_w.resourceplus.fragments.start_fragment;

import android.view.View;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

public class StartViewModel extends BaseViewModel<StartNavigator> {

    private static final String TAG = "StartViewModel";
    private final DataManager dataManager;

    public StartViewModel(DataManager dataManager,
                          SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }

    public void OnClickSubmit(View v) {
        getNavigator().passToLogin(v);
    }

    public void OnClickTermsNConditions(View v) {
        getNavigator().showToast("Terms and Conditions");
    }
}

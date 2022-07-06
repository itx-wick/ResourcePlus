package com.mr_w.resourceplus.fragments.call_list;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

public class CallListViewModel extends BaseViewModel<CallListNavigator> {

    private static final String TAG = "CallListViewModel";
    private final DataManager dataManager;

    public CallListViewModel(DataManager dataManager,
                             SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        this.dataManager = dataManager;
    }

    @Override
    public DataManager getDataManager() {
        return dataManager;
    }
}

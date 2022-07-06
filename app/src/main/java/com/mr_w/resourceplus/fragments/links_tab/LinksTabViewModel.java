package com.mr_w.resourceplus.fragments.links_tab;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.fragments.create_group.CreateGroupNavigator;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

import java.util.List;

public class LinksTabViewModel extends BaseViewModel<LinksTabNavigator> {

    private static final String TAG = "LinksTabViewModel";
    private MutableLiveData<List<String>> links;
    private String conversationId;

    public LinksTabViewModel(DataManager dataManager,
                             SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        links = new MutableLiveData<>();
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public LiveData<List<String>> getLinks() {

        getCompositeDisposable().add(getDataManager()
                .getAllLinks(conversationId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(commonGroup -> {
                    if (commonGroup != null) {
                        Log.d(TAG, "common group: " + commonGroup.size());
                        links.setValue(commonGroup);
                    }
                }, throwable -> {
                    Log.d(TAG, "medias: " + throwable);
                }));

        return links;
    }
}

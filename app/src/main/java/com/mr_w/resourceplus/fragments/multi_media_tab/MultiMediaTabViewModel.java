package com.mr_w.resourceplus.fragments.multi_media_tab;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Media;

import java.util.List;

public class MultiMediaTabViewModel extends BaseViewModel<MultiMediaTabNavigator> {

    private static final String TAG = "MultiMediaTabViewModel";
    private MutableLiveData<List<Media>> multiMedia;
    private String conversationId;

    public MultiMediaTabViewModel(DataManager dataManager,
                                  SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        multiMedia = new MutableLiveData<>();
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public LiveData<List<Media>> getMultiMedia() {

        getCompositeDisposable().add(getDataManager()
                .getAllMultiMedia(conversationId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(commonGroup -> {
                    if (commonGroup != null) {
                        Log.d(TAG, "common group: " + commonGroup.size());
                        multiMedia.setValue(commonGroup);
                    }
                }, throwable -> {
                    Log.d(TAG, "medias: " + throwable);
                }));

        return multiMedia;
    }
}

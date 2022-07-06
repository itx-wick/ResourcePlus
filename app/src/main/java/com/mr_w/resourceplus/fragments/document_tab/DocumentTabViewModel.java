package com.mr_w.resourceplus.fragments.document_tab;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Media;

import java.util.List;

public class DocumentTabViewModel extends BaseViewModel<DocumentTabNavigator> {

    private static final String TAG = "DocumentTabViewModel";
    private MutableLiveData<List<Media>> documents;
    private String conversationId;

    public DocumentTabViewModel(DataManager dataManager,
                                SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        documents = new MutableLiveData<>();
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public LiveData<List<Media>> getDocuments() {

        getCompositeDisposable().add(getDataManager()
                .getAllDocuments(conversationId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(commonGroup -> {
                    if (commonGroup != null) {
                        Log.d(TAG, "common group: " + commonGroup.size());
                        documents.setValue(commonGroup);
                    }
                }, throwable -> {
                    Log.d(TAG, "medias: " + throwable);
                }));

        return documents;
    }
}

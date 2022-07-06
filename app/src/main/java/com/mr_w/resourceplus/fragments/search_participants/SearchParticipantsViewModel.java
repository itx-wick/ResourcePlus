package com.mr_w.resourceplus.fragments.search_participants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Participant;

import java.util.List;

public class SearchParticipantsViewModel extends BaseViewModel<SearchParticipantsNavigator> {

    private static final String TAG = "SearchParticipantsViewM";
    private final MutableLiveData<List<Participant>> participants;

    public SearchParticipantsViewModel(DataManager dataManager,
                                       SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
        participants = new MutableLiveData<>();
    }

    public LiveData<List<Participant>> getParticipants() {
        return participants;
    }

    public void addAllToList(List<Participant> participantList) {
        participants.setValue(participantList);
    }
}

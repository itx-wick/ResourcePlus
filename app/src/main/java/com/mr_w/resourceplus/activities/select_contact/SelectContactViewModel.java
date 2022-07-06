package com.mr_w.resourceplus.activities.select_contact;

import android.util.Log;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.users.Users;

import java.util.ArrayList;
import java.util.List;

public class SelectContactViewModel extends BaseViewModel<SelectContactNavigator> {

    private static final String TAG = "SelectContactViewModel";
    private final MutableLiveData<List<Users>> users;
    private final MutableLiveData<List<Users>> selectedUsers;

    public SelectContactViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);

        users = new MutableLiveData<>();
        updateList();
        selectedUsers = new MutableLiveData<>(new ArrayList<>());
    }

    public LiveData<List<Users>> getUsers() {
        return users;
    }

    public void updateList() {

        getCompositeDisposable().add(getDataManager()
                .getAllUsersExceptMe(getDataManager().getUserDetails().getPhoneId())
                .doAfterNext(users::postValue)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(data -> Log.d(TAG, "SelectContactViewModel: Success"), throwable -> Log.d(TAG, "getUsers: " + throwable)));

    }

    public void search(String text) {
        List<Users> temp = new ArrayList<>();
        for (Users user : users.getValue()) {
            if (user.getName().toLowerCase().contains(text)) {
                temp.add(user);
            }
        }
        users.postValue(temp);
    }

    public LiveData<List<Users>> getSelectedUsers() {
        return selectedUsers;
    }

    public void updateSelectedUsers(Users user) {
        List<Users> temp = selectedUsers.getValue();
        temp.add(user);
        selectedUsers.postValue(temp);
    }

    public void removeSelectedUser(int position) {
        List<Users> temp = selectedUsers.getValue();
        temp.remove(position);
        selectedUsers.postValue(temp);
    }

    public void goBack(View v) {
        getNavigator().goBack();
    }

    public void search(View v) {
        getNavigator().search();
    }

    public void clear(View v) {
        getNavigator().clear();
    }

    public void submit(View v) {
        getNavigator().submit();
    }

}

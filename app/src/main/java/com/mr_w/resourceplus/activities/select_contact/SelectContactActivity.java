package com.mr_w.resourceplus.activities.select_contact;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.adapter.ContactsAdapter;
import com.mr_w.resourceplus.adapter.CreateGroupMembersToGroupAdapter;
import com.mr_w.resourceplus.databinding.ActivitySelectContactBinding;
import com.mr_w.resourceplus.fragments.create_group.CreateGroupFragment;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Utils;

import java.util.List;

public class SelectContactActivity extends BaseActivity<ActivitySelectContactBinding, SelectContactViewModel>
        implements ContactsAdapter.SendData,
        CreateGroupMembersToGroupAdapter.RemoveItem,
        ContactsAdapter.Click,
        SelectContactNavigator {

    ActivitySelectContactBinding mBinding;
    boolean isExisted = false;
    private Conversation conversation;
    private boolean isKeyBoardShowing = false;

    private final ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = mBinding.root.getRootView().getHeight() - mBinding.root.getHeight();
            isKeyBoardShowing = heightDiff > Utils.dpToPx(128);
        }
    };
    private String share;
    private String group;
    private BroadcastReceiver contactsSyncReceiver;
    private String hide_contact;

    @Override
    public int getBindingVariable() {
        return BR.select_contact;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_select_contact;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        performDataBinding();
        mBinding = getViewDataBinding();
        viewModel.setNavigator(this);

        conversation = getIntent().hasExtra("conversation") ? (Conversation) getIntent().getSerializableExtra("conversation") : null;
        share = getIntent().hasExtra("share") ? getIntent().getStringExtra("share") : null;
        if (share != null)
            hide_contact = getIntent().hasExtra("hide_contact") ? getIntent().getStringExtra("hide_contact") : null;
        group = getIntent().hasExtra("group") ? getIntent().getStringExtra("group") : null;
        mBinding.root.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        if (share != null) {
            mBinding.title.setText("Select contacts");
            mBinding.counter.setVisibility(View.VISIBLE);
        } else if (group != null) {
            mBinding.title.setText("New Group");
            mBinding.subTitle.setText("Add participants");
            mBinding.subTitle.setVisibility(View.VISIBLE);
        } else {
            mBinding.title.setText("Add participants");
        }

        mBinding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mBinding.searchBar.length() > 0) {
                    if (mBinding.clear.getVisibility() == View.GONE)
                        mBinding.clear.setVisibility(View.VISIBLE);
                    performSearch();
                } else {
                    mBinding.clear.setVisibility(View.GONE);
                    viewModel.updateList();
                }
            }
        });

        registerReceivers();
        initializeObservers();

    }

    private void initializeObservers() {
        viewModel.getUsers().observe(this, new Observer<List<Users>>() {
            @Override
            public void onChanged(List<Users> list) {
                setUsers(list);
            }
        });

        viewModel.getSelectedUsers().observe(this, new Observer<List<Users>>() {
            @Override
            public void onChanged(List<Users> list) {
                setMembers(list);
            }
        });
    }

    private void performSearch() {
        viewModel.search(mBinding.searchBar.getText().toString().toLowerCase());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactsSyncReceiver);
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    public void performClick(Users userObj, int pos) {
        if (!userObj.getUserId().equals("Already added to group"))
            selectContact(userObj);
    }

    @Override
    public void updateView(int position) {
        viewModel.removeSelectedUser(position);
    }

    @Override
    public void unClick(int pos) {
        String id = viewModel.getUsers().getValue().get(pos).getPhoneId();

        for (int i = 0; i < viewModel.getSelectedUsers().getValue().size(); i++) {
            if (id.equals(viewModel.getSelectedUsers().getValue().get(i).getPhoneId())) {
                viewModel.removeSelectedUser(i);
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isKeyBoardShowing && mBinding.layoutSearch.getVisibility() == View.VISIBLE) {
            mBinding.searchBar.setText("");
            mBinding.layoutSearch.setVisibility(View.GONE);
            mBinding.title.setVisibility(View.VISIBLE);
            mBinding.search.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        }
    }

    private void registerReceivers() {

        contactsSyncReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data");
                if (data != null) {
                    viewModel.updateList();
                }
            }
        };
        registerReceiver("contacts_sync", contactsSyncReceiver);

    }

    public void registerReceiver(String filterAction, BroadcastReceiver broadcastReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(filterAction);
        LocalBroadcastManager.getInstance(SelectContactActivity.this).registerReceiver(broadcastReceiver, filter);
    }

    private void closeKeyboard() {
        View view = getCurrentFocus();

        if (view == null) {
            mBinding.searchBar.requestFocus();
            view = getCurrentFocus();
        }

        InputMethodManager manager
                = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void setUsers(List<Users> list) {

        if (list.size() == 0) {
            mBinding.lnNotFound.setVisibility(View.VISIBLE);
            mBinding.contacts.setVisibility(View.GONE);
        } else {
            mBinding.lnNotFound.setVisibility(View.GONE);
            mBinding.contacts.setVisibility(View.VISIBLE);
        }

        if (conversation != null) {
            for (Users obj : list) {
                for (Users obj1 : conversation.getMembers()) {
                    if (obj.getPhoneId().equals(obj1.getPhoneId()) && !conversation.getRemovedMembers().contains(obj1.getPhoneId())) {
                        obj.setUserId("Already added to group");
                    }
                }
            }
        }

        if (share != null) {
            int index = -1;
            for (Users user : list) {
                if (user.getPhoneNo().equals(hide_contact)) {
                    index = list.indexOf(user);
                    break;
                }
            }
            if (index != -1)
                list.remove(index);
        }

        ContactsAdapter mAdapter = new ContactsAdapter(this, list, SelectContactActivity.this);
        mAdapter.setMe(viewModel.getDataManager().getUserDetails());
        mAdapter.setClick(this);
        mAdapter.setSendData(this);
        mAdapter.setSelectedUsers(viewModel.getSelectedUsers().getValue());
        mBinding.contacts.setLayoutManager(new LinearLayoutManager(this));
        mBinding.contacts.setHasFixedSize(true);
        mBinding.contacts.setAdapter(mAdapter);
    }

    private void selectContact(Users userObj) {
        isExisted = false;
        for (Users user : viewModel.getSelectedUsers().getValue()) {
            if (user.getPhoneId().equals(userObj.getPhoneId())) {
                isExisted = true;
                break;
            }
        }

        if (!isExisted) {
            viewModel.updateSelectedUsers(userObj);
            mBinding.selectedParticipantsList.smoothScrollToPosition(viewModel.getSelectedUsers().getValue().size() - 1);
        }
    }

    private void setMembers(List<Users> list) {

        if (list.size() == 0) {
            mBinding.done.setVisibility(View.GONE);
            mBinding.selectedParticipantsList.setVisibility(View.GONE);
            mBinding.div2.setVisibility(View.GONE);
        } else if (list.size() == 1) {
            mBinding.selectedParticipantsList.setVisibility(View.VISIBLE);
            mBinding.div2.setVisibility(View.VISIBLE);
            mBinding.done.setVisibility(View.VISIBLE);
        }
        if (share != null) {
            mBinding.counter.setText(list.size() + " selected");
        }

        CreateGroupMembersToGroupAdapter membersToGroupAdapter = new CreateGroupMembersToGroupAdapter(list, SelectContactActivity.this);
        membersToGroupAdapter.setRemoveItem(this);
        mBinding.selectedParticipantsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mBinding.selectedParticipantsList.setHasFixedSize(true);
        mBinding.selectedParticipantsList.setAdapter(membersToGroupAdapter);
    }

    @Override
    public void goBack() {
        if (mBinding.layoutSearch.getVisibility() == View.VISIBLE) {
            closeKeyboard();
            isKeyBoardShowing = false;
            mBinding.searchBar.setText("");
            mBinding.layoutSearch.setVisibility(View.GONE);
            mBinding.title.setVisibility(View.VISIBLE);
            mBinding.subTitle.setVisibility(View.VISIBLE);
            mBinding.search.setVisibility(View.VISIBLE);
        } else {
            Intent intent = new Intent();
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
    }

    @Override
    public void search() {
        Utils.showKeyboard(this);
        mBinding.searchBar.requestFocus();
        mBinding.layoutSearch.setVisibility(View.VISIBLE);
        mBinding.title.setVisibility(View.GONE);
        mBinding.subTitle.setVisibility(View.GONE);
        mBinding.search.setVisibility(View.GONE);
        isKeyBoardShowing = true;
    }

    @Override
    public void clear() {
        mBinding.clear.setVisibility(View.GONE);
        mBinding.searchBar.setText("");
    }

    @Override
    public void submit() {
        if (group != null) {

            if (viewModel.getSelectedUsers().getValue().size() == 0) {
                Toast.makeText(this, "Atleast select 1 contact", Toast.LENGTH_SHORT).show();
                return;
            }

            Fragment fragment = new CreateGroupFragment();
            Bundle bundle = new Bundle();
            bundle.putString("participants", new Gson().toJson(viewModel.getSelectedUsers().getValue()));
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().add(R.id.fraggg, fragment).addToBackStack(null).commit();

        } else {
            Intent intent = new Intent();
            intent.putExtra("users", new Gson().toJson(viewModel.getSelectedUsers().getValue()));
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }
}
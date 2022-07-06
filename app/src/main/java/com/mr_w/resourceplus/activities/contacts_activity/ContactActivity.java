package com.mr_w.resourceplus.activities.contacts_activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.adapter.ContactsAdapter;
import com.mr_w.resourceplus.databinding.ActivityContactBinding;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.mr_w.resourceplus.R.id.action_delete_all;
import static com.mr_w.resourceplus.R.id.action_refresh_contacts;

public class ContactActivity extends BaseActivity<ActivityContactBinding, ContactsViewModel> implements ContactsAdapter.SendData,
        ContactsNavigator
//        , NetworkStateReceiver.NetworkStateReceiverListener
{
    private ActivityContactBinding mBinding;

    private final String TAG = this.getClass().getSimpleName();
    private List<LocalContacts> localContactsList = new ArrayList<>();
    private List<Users> usersList = new ArrayList<>();
    private ContactsAdapter mAdapter;

    ConnectivityManager connectivityManager;
    boolean connected = false;
    boolean isKeyBoardShowing = false;
    private final ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int heightDiff = mBinding.root.getRootView().getHeight() - mBinding.root.getHeight();
            isKeyBoardShowing = heightDiff > Utils.dpToPx(128);
        }
    };
    private BroadcastReceiver newMessageReceiver;
    private BroadcastReceiver contactsSyncReceiver;

//    private Snackbar snackbar = null;
//    private boolean isConnected;
//    private NetworkStateReceiver networkStateReceiver = null;

//    private void setNetworkStateReceiver() {
//        networkStateReceiver = new NetworkStateReceiver(this);
//        networkStateReceiver.addListener(this);
//        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//
//    }
//
//    @Override
//    public void onNetworkAvailable() {
//        if (!isConnected) {
//            if (!checkContactsPermission()) {
//                boolean isSynced = syncedPrefs.getBoolean(UserPreferences.PREF_CONTACTS_UPLOADED);
//                if (isSynced) {
//                    getDataFromLocalDB();
//                } else {
//                    new SyncContacts(contactList).execute();
//                }
//            } else requestContactsPermission(121);
//        }
//        isConnected = true;
//        snackbar.dismiss();
//    }
//
//    @Override
//    public void onNetworkUnavailable() {
//        isConnected = false;
//        snackbar.show();
//    }

    @Override
    public int getBindingVariable() {
        return BR.contacts;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_contact;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setNavigator(this);
        viewModel.setActivity(new WeakReference<>(this));
        performDataBinding();
        mBinding = getViewDataBinding();
        mBinding.root.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        viewModel.getUsers().observe(this, new Observer<List<Users>>() {
            @Override
            public void onChanged(List<Users> list) {
                setData(list);
            }
        });

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
                } else {
                    mBinding.clear.setVisibility(View.GONE);
                }
                mAdapter.getFilter().filter(mBinding.searchBar.getText().toString());
            }
        });

        mBinding.clear.setOnClickListener(v -> {
            mBinding.clear.setVisibility(View.GONE);
            mBinding.searchBar.setText("");
        });

        mBinding.search.setOnClickListener(v -> {
            Utils.showKeyboard(this);
            isKeyBoardShowing = true;
            mBinding.searchBar.requestFocus();
            mBinding.layoutSearch.setVisibility(View.VISIBLE);
            mBinding.title.setVisibility(View.GONE);
            mBinding.search.setVisibility(View.GONE);
        });

        mBinding.topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            switch (id) {
                case action_refresh_contacts:
                    viewModel.synchronizeContacts();
                    break;
                case action_delete_all:
                    deleteAll();
                    viewModel.getDataManager().saveBoolean(UserPreferences.PREF_CONTACTS_UPLOADED, false);
                    viewModel.updateList(new ArrayList<>());
                    break;
            }
            return true;
        });

        mBinding.btnBack.setOnClickListener(v -> {
            if (mBinding.layoutSearch.getVisibility() == View.VISIBLE) {
                closeKeyboard();
                isKeyBoardShowing = false;
                mBinding.searchBar.setText("");
                mBinding.layoutSearch.setVisibility(View.GONE);
                mBinding.title.setVisibility(View.VISIBLE);
                mBinding.search.setVisibility(View.VISIBLE);
            } else {
                onBackPressed();
                overridePendingTransition(0, 0);
            }
        });

        setData(usersList);
        if (viewModel.getDataManager().getBoolean(UserPreferences.PREF_BG_SYNCING))
            mBinding.progress.setVisibility(View.VISIBLE);

        if (!checkContactsPermission()) {
            viewModel.updateList();
        } else requestContactsPermission(121);

        registerReceivers();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        isConnected = Utils.checkInternet(this);
//        snackbar = Snackbar.make(findViewById(R.id.root), getString(R.string.connection_unavailable), Snackbar.LENGTH_INDEFINITE);
//        setNetworkStateReceiver();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(contactsSyncReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(newMessageReceiver);
//        networkStateReceiver.removeListener(this);
        ActiveActivitiesTracker.activityStopped();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 121) {
            if (hasAllPermissionsGranted(grantResults)) {
                viewModel.updateList();
            } else {
                requestContactsPermission(121);
            }
        }
    }

    @Override
    public void performClick(Users userObj, int pos) {
        closeKeyboard();
        openChatScreen(userObj);
    }

    private void registerReceivers() {

        contactsSyncReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data");
                if (data != null) {
                    viewModel.updateList();
                }
                mBinding.progress.setVisibility(View.GONE);
            }
        };
        registerReceiver("contacts_sync", contactsSyncReceiver);

        newMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);

                        if (obj.has("toUsers")) {
                            String phoneId = obj.getString("phoneId");
                            List<String> communicatedIds = new Gson().fromJson(obj.getString("toUsers"), new TypeToken<List<String>>() {
                            }.getType());
                            if (communicatedIds.contains(viewModel.getDataManager().getUserDetails().getPhoneId())) {
                                if (obj.has("name")) {
                                    String name = obj.getString("name");
                                    for (int i = 0; i < usersList.size(); i++) {
                                        if (usersList.get(i).getPhoneId().equals(phoneId)) {
                                            usersList.get(i).setName(name);
                                            mAdapter.notifyItemChanged(i);
                                            viewModel.updateUser(phoneId, usersList.get(i));
                                        }
                                    }
                                } else if (obj.has("about")) {
                                    String about = obj.getString("about");
                                    for (int i = 0; i < usersList.size(); i++) {
                                        if (usersList.get(i).getPhoneId().equals(phoneId)) {
                                            usersList.get(i).setAbout(about);
                                            mAdapter.notifyItemChanged(i);
                                            viewModel.updateUser(phoneId, usersList.get(i));
                                        }
                                    }
                                } else if (obj.has("image")) {
                                    String image = obj.getString("image");
                                    for (int i = 0; i < usersList.size(); i++) {
                                        if (usersList.get(i).getPhoneId().equals(phoneId)) {
                                            usersList.get(i).setPhoto(image);
                                            mAdapter.notifyItemChanged(i);
                                            viewModel.updateUser(phoneId, usersList.get(i));
                                        }
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        registerReceiver("getMessage", newMessageReceiver);

    }

    public void registerReceiver(String filterAction, BroadcastReceiver broadcastReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(filterAction);
        LocalBroadcastManager.getInstance(ContactActivity.this).registerReceiver(broadcastReceiver, filter);
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

    private boolean checkContactsPermission() {
        int read_result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        return read_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestContactsPermission(int reqCode) {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_CONTACTS
        }, reqCode);
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void setData(List<Users> list) {

        if (list.size() == 0) {
            mBinding.lnNotFound.setVisibility(View.VISIBLE);
            mBinding.contactsRecyclerView.setVisibility(View.GONE);
        } else {
            mBinding.lnNotFound.setVisibility(View.GONE);
            mBinding.contactsRecyclerView.setVisibility(View.VISIBLE);
        }

        mAdapter = new ContactsAdapter(this, list, ContactActivity.this);
        mAdapter.setMe(viewModel.getDataManager().getUserDetails());
        mAdapter.setSendData(this);
        mBinding.contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBinding.contactsRecyclerView.setHasFixedSize(true);
        mBinding.contactsRecyclerView.setAdapter(mAdapter);
    }

    private void deleteAll() {
        viewModel.deleteAllUsers();
        viewModel.deleteAllContacts();
    }

    private void openChatScreen(Users userObj) {
        viewModel.openChatScreen(userObj);
    }

    @Override
    public void showProgress() {
        mBinding.progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        mBinding.progress.setVisibility(View.GONE);
    }

    @Override
    public void navigateWithConversation(Conversation obj) {
        startActivity(new Intent(ContactActivity.this, ChatActivity.class).putExtra("conversation", obj));
    }

    @Override
    public void navigateWithUsers(Users obj) {
        startActivity(new Intent(ContactActivity.this, ChatActivity.class).putExtra("user", obj));
    }

    @Override
    public void finishActivity() {
        finish();
    }
}
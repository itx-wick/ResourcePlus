package com.mr_w.resourceplus.activities.chat_activity;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.devlomi.record_view.OnBasketAnimationEnd;
import com.devlomi.record_view.OnRecordListener;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.activities.AudioCallActivity;
import com.mr_w.resourceplus.activities.VideoCallActivity;
import com.mr_w.resourceplus.activities.document_pick.DocumentPickActivity;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.adapter.ChatAdapter;
import com.mr_w.resourceplus.databinding.ActivityChatBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.fragments.conversation_detail.ConversationDetailFragment;
import com.mr_w.resourceplus.fragments.media_gallery.MediaGalleryFragment;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.MessageStatesRecord;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.receivers.NetworkStateReceiver;
import com.mr_w.resourceplus.supernova_emoji.actions.EmojIconActions;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Constant;
import com.mr_w.resourceplus.utils.DocumentFile;
import com.mr_w.resourceplus.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class ChatActivity
        extends
        BaseActivity<ActivityChatBinding, ChatViewModel>
        implements
        ChatAdapter.ImageViewer,
        ChatAdapter.GetProfile,
        ChatAdapter.UploadMessage,
        ChatAdapter.ScrollToLast,
        ChatAdapter.CancelSendingFiles,
        NetworkStateReceiver.NetworkStateReceiverListener,
        ChatNavigator {

    //region Declarations
    private static final String TAG = "ChatActivity";
    private static final int REQUEST_CORD_PERMISSION = 332;
    private String audio_path;
    private boolean isActive = false;
    private boolean isConnected;
    public boolean isNewThread = false;
    private int count = 0;
    private int currentPosition;

    //Non-primitive Declarations
    public ActivityChatBinding mBinding;
    private DataManager preferences;
    private RecyclerView mRecyclerView;
    public ChatAdapter mChatAdapter;
    private EditText textMessage;
    private MediaRecorder mediaRecorder;
    public Conversation conversation;
    private File file;
    private Users user;
    private NetworkStateReceiver networkStateReceiver = null;
    private Snackbar snackbar = null;
    private List<Integer> positions;

    //Contacts
    private LocalContacts contact;
    private LocalContacts recentContactToAdd;

    //Broadcast Receivers
    private BroadcastReceiver typingReceiver;
    private BroadcastReceiver messageReceiver;
    private BroadcastReceiver seenReceiver;
    private BroadcastReceiver deliveredReceiver;
    private BroadcastReceiver sentReceiver;
    private BroadcastReceiver failedReceiver;
    //endregion

    private void setNetworkStateReceiver() {
        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void onNetworkAvailable() {
        isConnected = true;
        snackbar.dismiss();
    }

    @Override
    public void onNetworkUnavailable() {
        isConnected = false;
        snackbar.show();
    }

    @Override
    public int getBindingVariable() {
        return BR.chat;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_chat;
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
        preferences = viewModel.getDataManager();
        viewModel.setNavigator(this);
        viewModel.setActivity(new WeakReference<>(this));
        getSupportFragmentManager().addOnBackStackChangedListener(getListener());

        user = getIntent().hasExtra("user") ?
                (Users) getIntent().getSerializableExtra("user") :
                null;
        conversation = getIntent().hasExtra("conversation") ?
                (Conversation) getIntent().getSerializableExtra("conversation") :
                null;

        if (conversation != null) {
            if (user == null) {
                for (Users member : conversation.getMembers()) {
                    if (!member.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                        user = member;
                        break;
                    }
                }
            }
            viewModel.updateList(conversation.get_id());
        } else {
            isNewThread = true;
        }

        initialize();
        initBtnClick();
        registerReceivers();
        initializeObservers();

    }

    private void initializeObservers() {

        viewModel.getConversation().observe(this, new Observer<Conversation>() {
            @Override
            public void onChanged(Conversation conv) {
                if (conv != null) {
                    conversation = conv;
                    invalidateViews();
                }
            }
        });

        viewModel.getUnreadMessages().observe(this, new Observer<List<MessageData>>() {
            @Override
            public void onChanged(List<MessageData> messageData) {
                seenAllDeliveredMessages(messageData);
            }
        });

        viewModel.getMessages().observe(this, new Observer<List<MessageData>>() {
            @Override
            public void onChanged(List<MessageData> messageData) {
                setRecyclerView(messageData);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
        isConnected = Utils.checkInternet(this);
        snackbar = Snackbar.make(mBinding.root, getString(R.string.connection_unavailable), Snackbar.LENGTH_INDEFINITE);
        setNetworkStateReceiver();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null) {
            fragment.onResume();
        } else {
            if (checkAllPermission()) {
                requestAllPermission();
            } else {
                if (conversation != null) {
                    viewModel.updateList(conversation.get_id());
                    viewModel.getAllUnseenMessages(conversation.get_id(), preferences.getUserDetails().getPhoneId());
                } else {
                    invalidateViews();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(typingReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(seenReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(deliveredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sentReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(failedReceiver);
        isActive = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        networkStateReceiver.removeListener(this);
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (conversation != null) {
            if (conversation.getType().equals("one-to-one")) {
                inflater.inflate(R.menu.menu_chat_one, menu);

                contact = viewModel.getContact(user.getPhoneNo());
                if (contact != null) {
                    menu.findItem(R.id.view_contact).setVisible(true);
                    menu.findItem(R.id.add_to_contacts).setVisible(false);
                } else {
                    menu.findItem(R.id.view_contact).setVisible(false);
                    menu.findItem(R.id.add_to_contacts).setVisible(true);
                }
            } else
                inflater.inflate(R.menu.menu_chat_group, menu);
        } else
            inflater.inflate(R.menu.menu_chat_one, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.video_call) {
            if (conversation != null) {
                for (Users users : conversation.getMembers()) {
                    if (conversation.getType().equals("one-to-one")) {
                        if (!users.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                            startActivity(new Intent(this, VideoCallActivity.class)
                                    .putExtra("to", users));
                            break;
                        }
                    }
                }
            } else {
                startActivity(new Intent(this, VideoCallActivity.class)
                        .putExtra("to", user));
            }
        } else if (item.getItemId() == R.id.audio_call) {
            if (conversation != null) {
                for (Users users : conversation.getMembers()) {
                    if (conversation.getType().equals("one-to-one")) {
                        if (!users.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                            startActivity(new Intent(this, AudioCallActivity.class)
                                    .putExtra("to", users));
                            break;
                        }
                    }
                }
            } else {
                startActivity(new Intent(this, AudioCallActivity.class)
                        .putExtra("to", user));
            }
        } else if (item.getItemId() == R.id.view_contact) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.getContactId());
            intent.setData(uri);
            startActivity(intent);
        } else if (item.getItemId() == R.id.add_to_contacts) {
            recentContactToAdd = new LocalContacts();
            recentContactToAdd.setName(user.getName());
            recentContactToAdd.setNumber(user.getPhoneNo());

            Intent i = new Intent(Intent.ACTION_INSERT);
            i.setType(ContactsContract.Contacts.CONTENT_TYPE);
            i.putExtra(ContactsContract.Intents.Insert.NAME, user.getName());
            i.putExtra(ContactsContract.Intents.Insert.PHONE, user.getPhoneNo());
            i.putExtra("finishActivityOnSaveCompleted", true);
            addContactToContacts.launch(i);
        } else if (item.getItemId() == R.id.medias || item.getItemId() == R.id.group_media) {
            Fragment fragment = new MediaGalleryFragment();
            Bundle bundle = new Bundle();
            if (conversation == null) {
                conversation.setTitle(user.getName());
                conversation.setType("one-to-one");
            }
            bundle.putSerializable("conversation", conversation);
            fragment.setArguments(bundle);
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
            transaction.add(R.id.fragment, fragment, "media_gallery");
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (item.getItemId() == R.id.search || item.getItemId() == R.id.search_group) {
            Utils.showKeyboard(ChatActivity.this);
            mBinding.lnAction.setVisibility(View.GONE);
            mBinding.btnSend.setVisibility(View.GONE);
            mBinding.recordLayout.setVisibility(View.GONE);

            mBinding.SearchLayout.setVisibility(View.VISIBLE);
            mBinding.topAppBar.setVisibility(View.GONE);

            mBinding.btnBackSearch.setOnClickListener(v -> {
                mBinding.topAppBar.setVisibility(View.VISIBLE);
                mBinding.SearchLayout.setVisibility(View.GONE);
                mBinding.lnAction.setVisibility(View.VISIBLE);
                mBinding.btnSend.setVisibility(View.VISIBLE);
                mBinding.recordLayout.setVisibility(View.VISIBLE);
                mBinding.searchBar.setText("");

                closeKeyboard();
                mBinding.recyclerViewChat.setOnScrollListener(null);
                mBinding.recyclerViewChat.scrollToPosition(viewModel.getMessages().getValue().size() - 1);
            });

            mBinding.searchBar.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            });

            mBinding.btnUp.setOnClickListener(v -> {
                if (positions.size() > 0) {
                    if (currentPosition > 0) {
                        int pos = positions.get(--currentPosition);
                        mChatAdapter.setPerformBackgroundTransition(pos);
                        mBinding.recyclerViewChat.scrollToPosition(pos);
                        mChatAdapter.notifyItemChanged(pos);
                    } else {
                        Toast.makeText(this, "No more match", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            mBinding.btnDown.setOnClickListener(v -> {
                if (positions.size() > 0) {
                    if (currentPosition < positions.size() - 1) {
                        int pos = positions.get(++currentPosition);
                        mChatAdapter.setPerformBackgroundTransition(pos);
                        mBinding.recyclerViewChat.scrollToPosition(pos);
                        mChatAdapter.notifyItemChanged(pos);
                    } else {
                        Toast.makeText(this, "No more match", Toast.LENGTH_SHORT).show();
                    }
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
                    if (editable.length() == 0)
                        positions = new ArrayList<>();
                }
            });

            mBinding.clear.setOnClickListener(v -> {
                mBinding.searchBar.setText("");
            });

            mBinding.recyclerViewChat.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    mChatAdapter.showBackgroundTransition(currentPosition);
                }
            });
        } else if (item.getItemId() == R.id.block) {

        } else if (item.getItemId() == R.id.clear_chat || item.getItemId() == R.id.clear_chat_group) {
            conversation.setUpdatedAt(conversation.getMessage().getDateTime());
            conversation.setMessage(null);
            viewModel.updateMessagesList();
            viewModel.deleteAllMessages(conversation.get_id());
            viewModel.updateConversation(conversation);
        } else if (item.getItemId() == R.id.export_chat) {

        } else if (item.getItemId() == R.id.report) {

        } else if (item.getItemId() == R.id.refresh || item.getItemId() == R.id.refresh_group) {
            JSONObject postData = new JSONObject();
            try {
                postData.put("conversationId", conversation.get_id());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            viewModel.getMessagesFromServer(postData);
        } else if (item.getItemId() == R.id.group_info) {
            Fragment fragment = new ConversationDetailFragment();
            Bundle bundle = new Bundle();
            if (conversation != null) {
                if (conversation.getType().equals("one-to-one")) {
                    for (Users users : conversation.getMembers()) {
                        if (!users.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                            bundle.putSerializable("object", users);
                            bundle.putSerializable("conversation", conversation);
                            fragment.setArguments(bundle);
                            break;
                        }
                    }
                } else {
                    bundle.putSerializable("conversation", conversation);
                    fragment.setArguments(bundle);
                }
            } else {
                bundle.putSerializable("object", user);
                fragment.setArguments(bundle);
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.add(R.id.fragment, fragment, "conversation_detail");
            transaction.addToBackStack(null);
            transaction.commit();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (hasAllPermissionsGranted(grantResults)) {
                openGallery();
            } else {
                requestCameraPermission(111);
            }
        } else if (requestCode == 222) {
            if (hasAllPermissionsGranted(grantResults)) {
                openFilePicker();
            } else {
                requestCameraPermission(222);
            }
        } else if (requestCode == 333) {
            if (hasAllPermissionsGranted(grantResults)) {
                openAudioPicker();
            } else {
                requestCameraPermission(333);
            }
        } else if (requestCode == 444) {
            if (hasAllPermissionsGranted(grantResults)) {
                openCamera();
            } else {
                requestCameraPermission(444);
            }
        } else if (requestCode == 555) {
            if (hasAllPermissionsGranted(grantResults)) {
                if (conversation != null)
                    viewModel.updateList(conversation.get_id());
                else {
                    invalidateViews();
                }
            } else {
                requestAllPermission();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reviewImage(bitmap, resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    @Override
    public void viewImage(String url) {
        new DialogReviewSendMedia(this, null, false).show(url);
    }

    @Override
    public Users getUser(String phoneId) {
        Users user = null;
        for (Users users : conversation.getMembers()) {
            if (users.getPhoneId().equals(phoneId)) {
                user = users;
                break;
            }
        }
        return user;
    }

    @Override
    public void uploadMessage(int pos, MessageData messageData) {
        if (!isConnected) {
            Toast.makeText(this, "Kindly check internet connection", Toast.LENGTH_SHORT).show();
            onCancel(pos);
            return;
        }
        messageData = viewModel.getMessages().getValue().get(pos);
        conversation.setMessage(messageData);
        viewModel.updateMessage(messageData);
        viewModel.updateConversation(conversation);
        viewModel.setMessage(messageData);
        try {
            URL url = new URL(messageData.getUrl());
            viewModel.uploadMessage(pos);
        } catch (Exception e) {
            File file = new File(messageData.getUrl());
            viewModel.uploadFile(file, pos);
        }
    }

    @Override
    public void saveMessage(int pos, MessageData messageData) {
        if (!isConnected) {
            Toast.makeText(this, "Kindly check internet connection", Toast.LENGTH_SHORT).show();
            onCancel(pos);
            return;
        }
        messageData = viewModel.getMessages().getValue().get(pos);
        conversation.setMessage(messageData);
        viewModel.updateMessage(messageData);
        viewModel.updateConversation(conversation);
        viewModel.setMessage(messageData);
        viewModel.uploadMessage(pos);
    }

    @Override
    public void scroll() {
        mBinding.recyclerViewChat.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
    }

    private void saveContact(Uri uri) {
        ContentResolver contentResolver = getContentResolver();

        Cursor cursor =
                contentResolver.query(
                        uri,
                        new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                ContactsContract.PhoneLookup._ID,
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.Data.LOOKUP_KEY},
                        null,
                        null,
                        null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                recentContactToAdd.setContactId(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)));
                recentContactToAdd.setName(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)));
                recentContactToAdd.setLookupKey(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY)));
                recentContactToAdd.setNumber(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                viewModel.updateContact(recentContactToAdd);
                viewModel.updateUser(user);
            }
            cursor.close();
        }
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

    private void performSearch() {
        String searchedText = mBinding.searchBar.getText().toString().trim().toLowerCase();
        positions = new ArrayList<>();
        List<MessageData> messages = viewModel.getMessages().getValue();
        for (MessageData msg : messages) {
            if (msg.getText() != null) {
                if (msg.getText().contains(searchedText)) {
                    positions.add(messages.indexOf(msg));
                }
            }
        }

        if (positions.size() > 0) {
            mBinding.recyclerViewChat.scrollToPosition(positions.get(0));
            mChatAdapter.showBackgroundTransition(positions.get(0));
            currentPosition = 0;
        }
    }

    private void seenAllDeliveredMessages(List<MessageData> messageData) {
        getAllUnseenMessages(messageData);
    }

    private void registerReceivers() {

        failedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isActive)
                    return;

                if (!intent.getBooleanExtra("success", false)) {
                    int data = intent.getIntExtra("data", -1);
                    if (data != -1) {
                        viewModel.updateListItem(data);
                        mChatAdapter.notifyItemChanged(data);
                    }
                }
            }
        };
        registerReceiver("message_failed", failedReceiver);

        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isActive)
                    return;

                if (intent.getBooleanExtra("success", false)) {
                    int data = intent.getIntExtra("data", -1);
                    if (data != -1)
                        mChatAdapter.notifyItemChanged(data);
                }
            }
        };
        registerReceiver("message_sent", sentReceiver);

        typingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isActive)
                    return;
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        boolean isPresent = false;
                        JSONObject obj = new JSONObject(data);
                        Users typer = new Gson().fromJson(obj.getString("typer"), Users.class);
                        String id = obj.getString("conversationId");
                        String phoneId = preferences.getUserDetails().getPhoneId();
                        List<Users> usersList = new Gson().fromJson(obj.getString("to"), new TypeToken<List<Users>>() {
                        }.getType());
                        for (int i = 0; i < usersList.size(); i++) {
                            if (usersList.get(i).getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                                isPresent = true;
                                break;
                            }
                        }
                        if (isPresent && !phoneId.equals(typer.getPhoneId()) && conversation.get_id().equals(id)) {
                            int length = Integer.parseInt(obj.getString("length"));
                            if (length > 0) {
                                mBinding.tvStatus.setVisibility(View.VISIBLE);
                                if (conversation.getType().equals("group")) {
                                    if (viewModel.getContact(typer.getPhoneNo()) != null)
                                        mBinding.tvStatus.setText(viewModel.getContact(typer.getPhoneNo()).getName() + " is typing...");
                                    else
                                        mBinding.tvStatus.setText(typer.getPhoneNo() + " is typing...");
                                } else
                                    mBinding.tvStatus.setText("typing...");
                            } else {
                                mBinding.tvStatus.setVisibility(View.GONE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        registerReceiver("typing", typingReceiver);

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isActive)
                    return;
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);

                        if (obj.has("cancel")) {
                            String phoneId = obj.getString("to");
                            String cancel = obj.getString("cancel");
                            if (preferences.getUserDetails().getPhoneId().equals(phoneId)) {
                                if (cancel.equals("pre")) {
                                    if (mBinding.callLayout.getVisibility() == View.VISIBLE)
                                        mBinding.callLayout.setVisibility(View.GONE);
                                }
                            }
                        } else if (obj.has("call")) {

                            if (count > 0)
                                return;

                            Users caller = new Gson().fromJson(obj.getString("caller"), Users.class);
                            String roomId = obj.getString("room_id");
                            count = 0;

                            if (obj.getString("to").equals(preferences.getUserDetails().getPhoneId())) {
                                count++;
                                if (obj.getString("mode").equals("video")) {
                                    startActivity(new Intent(ChatActivity.this, VideoCallActivity.class)
                                            .putExtra("caller_id", roomId)
                                            .putExtra("to", caller)
                                            .putExtra("incoming", true));
                                } else {
                                    startActivity(new Intent(ChatActivity.this, AudioCallActivity.class)
                                            .putExtra("caller_id", roomId)
                                            .putExtra("to", caller)
                                            .putExtra("incoming", true));
                                }
                            }
                        } else if (obj.has("toUsers")) {
                            String phoneId = obj.getString("phoneId");
                            List<String> communicatedIds = new Gson().fromJson(obj.getString("toUsers"), new TypeToken<List<String>>() {
                            }.getType());
                            if (communicatedIds.contains(preferences.getUserDetails().getPhoneId())) {
                                if (obj.has("name")) {
                                    String name = obj.getString("name");
                                    for (int j = 0; j < conversation.getMembers().size(); j++) {
                                        if (conversation.getMembers().get(j).getPhoneId().equals(phoneId)) {
                                            conversation.getMembers().get(j).setName(name);
                                            mBinding.tvUsername.setText(name);
                                            viewModel.updateConversation(conversation);
                                            viewModel.updateUser(conversation.getMembers().get(j));
                                        }
                                    }
                                } else if (obj.has("about")) {
                                    String about = obj.getString("about");
                                    for (int j = 0; j < conversation.getMembers().size(); j++) {
                                        if (conversation.getMembers().get(j).getPhoneId().equals(phoneId)) {
                                            conversation.getMembers().get(j).setAbout(about);
                                            viewModel.updateConversation(conversation);
                                            viewModel.updateUser(conversation.getMembers().get(j));
                                        }
                                    }
                                } else if (obj.has("image")) {
                                    String image = obj.getString("image");
                                    for (int j = 0; j < conversation.getMembers().size(); j++) {
                                        if (conversation.getMembers().get(j).getPhoneId().equals(phoneId)) {
                                            conversation.getMembers().get(j).setPhoto(image);
                                            Glide.with(ChatActivity.this).load(image).into(mBinding.imageProfile);
                                            viewModel.updateConversation(conversation);
                                            viewModel.updateUser(conversation.getMembers().get(j));
                                        }
                                    }
                                }
                            }
                        } else if (!obj.has("type2") && !obj.has("answer")) {

                            boolean isPresent = false;
                            String senderPhoneId = obj.getString("sender");
                            Conversation temp = new Gson().fromJson(obj.getString("conversation"), Conversation.class);
                            String phoneId = preferences.getUserDetails().getPhoneId();
                            List<Users> usersList = temp.getMembers();
                            for (int i = 0; i < usersList.size(); i++) {
                                if (usersList.get(i).getPhoneId().equals(preferences.getUserDetails().getPhoneId()) && !temp.getRemovedMembers().contains(usersList.get(i).getPhoneId())) {
                                    isPresent = true;
                                    break;
                                }
                            }
                            if (!phoneId.equals(senderPhoneId)) {
                                List<MessageData> messages = viewModel.getMessages().getValue();
                                for (int j = 0; j < messages.size(); j++) {
                                    if (messages.get(j).getId().equals(obj.getString("_id"))) {
                                        return;
                                    }
                                }
                            }
                            if (isPresent && !phoneId.equals(senderPhoneId) && conversation.get_id().equals(temp.get_id())) {

                                MessageData message = new MessageData();
                                message.setId(obj.getString("_id"));
                                message.setType(MessageData.getType(obj.getString("type")));
                                message.setStatus(obj.has("status") ? obj.getString("status") : "");
                                message.setText(obj.has("text") ? obj.getString("text") : "");
                                message.setUrl(obj.has("url") ? obj.getString("url") : "");
                                message.setFileSize(obj.has("fileSize") ? obj.getString("fileSize") : "");
                                message.setSenderPhoneId(obj.has("sender") ? obj.getString("sender") : "");
                                message.setConversationId(temp.get_id());
                                message.setDateTime(obj.has("dateTime") ? obj.getString("dateTime") : "");

                                viewModel.addMessage(message);

                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("messageId", message.getId());
                                jsonObject.put("to", message.getSenderPhoneId());
                                jsonObject.put("from", preferences.getUserDetails().getPhoneId());
                                jsonObject.put("conversationId", conversation.get_id());

                                if (!ResourcePlusApplication.mSocket.connected())
                                    ResourcePlusApplication.mSocket.connect();
                                ResourcePlusApplication.mSocket.emit("seen", jsonObject);
                                message.setStatus("seen");
                                conversation.setType(temp.getType());
                                conversation.setTitle(temp.getTitle());
                                conversation.setImage(temp.getImage());
                                conversation.setDescription(temp.getDescription());
                                conversation.setMembers(temp.getMembers());
                                conversation.setMessage(message);
                                viewModel.updateMessage(message);
                                viewModel.updateConversation(conversation);
                                invalidateViews();

                                viewModel.getAllUnseenMessages(conversation.get_id(), preferences.getUserDetails().getPhoneId());
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        registerReceiver("getMessage", messageReceiver);

        seenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isActive)
                    return;
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);
                        String messageId = obj.getString("messageId");
                        String id = obj.getString("to");
                        String from = obj.getString("from");
                        if (preferences.getUserDetails().getPhoneId().equals(id)) {
                            getMessage(from, messageId, "seen");
                        }
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }

                }
            }
        };
        registerReceiver("seen", seenReceiver);

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!isActive)
                    return;
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);
                        String messageId = obj.getString("messageId");
                        String id = obj.getString("to");
                        String from = obj.getString("from");
                        if (preferences.getUserDetails().getPhoneId().equals(id)) {
                            getMessage(from, messageId, "delivered");
                        }
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }

                }
            }
        };
        registerReceiver("delivered", deliveredReceiver);

    }

    public void registerReceiver(String filterAction, BroadcastReceiver broadcastReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(filterAction);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    private void performCrop(Uri picUri) {

        CropImage.activity(picUri)
                .start(this);

    }

    public void setRecyclerView(List<MessageData> list) {
        mRecyclerView = mBinding.recyclerViewChat;
        mChatAdapter = new ChatAdapter(list, ChatActivity.this);
        mChatAdapter.setPreferences(viewModel.getDataManager());
        mChatAdapter.setUploadMessage(ChatActivity.this);
        mChatAdapter.setScrollToLast(this);
        if (conversation != null)
            mChatAdapter.setConversationType(conversation.getType());
        else
            mChatAdapter.setConversationType("one-to-one");
        mChatAdapter.setCancelSendingFiles(ChatActivity.this);
        mChatAdapter.setImageViewer(ChatActivity.this);
        mChatAdapter.setGetProfile(ChatActivity.this);
        mChatAdapter.setHasStableIds(true);
        LinearLayoutManager manager = new LinearLayoutManager(this);
//        mRecyclerView.setItemViewCacheSize(20);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mChatAdapter);
        mBinding.recyclerViewChat.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
    }

    public void sendInfoToServer(String message, Conversation conversation) {

        this.conversation = conversation;
        MessageData messageData = new MessageData();
        messageData.setText(message);
        messageData.setType(MessageData.TYPE.INFO);

        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        messageData.setDateTime(utcFormat.format(new Date()));
        messageData.setStatus("sending");
        messageData.setConversationId(conversation.get_id());
        messageData.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

        mChatAdapter.getmMessageData().add(messageData);
        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);

        int pos = mChatAdapter.getmMessageData().size() - 1;
        if (!isConnected) {
            Toast.makeText(this, "Kindly check internet connection", Toast.LENGTH_SHORT).show();
            onCancel(pos);
            return;
        }
        if (messageData.getId() == null || messageData.getId().equals(""))
            messageData.setId(UUID.randomUUID().toString());
        conversation.setMessage(messageData);
        viewModel.updateMessage(messageData);
        viewModel.updateConversation(conversation);
        viewModel.uploadMessage(pos);
    }

    private void initialize() {

        setSupportActionBar(mBinding.topAppBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textMessage = mBinding.edMessage;

        EmojIconActions emojIcon = new EmojIconActions(this, mBinding.getRoot(), mBinding.edMessage, mBinding.btnEmoji);
        emojIcon.setUseSystemEmoji(true);
        mBinding.edMessage.setUseSystemDefault(true);
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);

        mBinding.edMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBinding.recyclerViewChat.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBinding.recyclerViewChat.smoothScrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                    }
                }, 300);
            }
        });
        mBinding.edMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(mBinding.edMessage.getText().toString())) {
                    mBinding.btnSend.setVisibility(View.INVISIBLE);
                    mBinding.recordButton.setVisibility(View.VISIBLE);
                } else {
                    mBinding.btnSend.setVisibility(View.VISIBLE);
                    mBinding.recordButton.setVisibility(View.INVISIBLE);
                }
                if (conversation != null) {
                    JSONObject object = new JSONObject();
                    try {
                        object.put("typer", new Gson().toJson(preferences.getUserDetails()));
                        object.put("to", new Gson().toJson(conversation.getMembers()));
                        object.put("conversationId", conversation.get_id());
                        object.put("length", s.length());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (!ResourcePlusApplication.mSocket.connected())
                        ResourcePlusApplication.mSocket.connect();
                    ResourcePlusApplication.mSocket.emit("userTyping", object);
                }
            }
        });

        mBinding.recordButton.setRecordView(mBinding.recordView);
        mBinding.recordView.setSoundEnabled(false);
        mBinding.recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {

                if (!checkAudioPermission()) {
                    mBinding.btnEmoji.setVisibility(View.INVISIBLE);
                    mBinding.btnFile.setVisibility(View.INVISIBLE);
                    mBinding.btnCamera.setVisibility(View.INVISIBLE);
                    mBinding.edMessage.setVisibility(View.INVISIBLE);

                    startRecord();
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(250);
                    }
                } else
                    requestAudioPermission();

            }

            @Override
            public void onCancel() {
                try {
                    mediaRecorder.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(long recordTime) {
                mBinding.btnEmoji.setVisibility(View.VISIBLE);
                mBinding.btnFile.setVisibility(View.VISIBLE);
                mBinding.btnCamera.setVisibility(View.VISIBLE);
                mBinding.edMessage.setVisibility(View.VISIBLE);

                try {
                    stopRecord();

                    if (isFileBiggerThan10MB(new File(audio_path))) {
                        Toast.makeText(ChatActivity.this, "Upload limit exceed. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                    } else {

                        MessageData message = new MessageData();
                        message.setType(MessageData.TYPE.AUDIO);
                        message.setUrl(audio_path);
                        message.setStatus("sending");

                        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                        message.setDateTime(utcFormat.format(new Date()));
                        message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                        message.setConversationId(conversation.get_id());

                        mChatAdapter.getmMessageData().add(message);
                        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLessThanSecond() {
                mBinding.btnEmoji.setVisibility(View.VISIBLE);
                mBinding.btnFile.setVisibility(View.VISIBLE);
                mBinding.btnCamera.setVisibility(View.VISIBLE);
                mBinding.edMessage.setVisibility(View.VISIBLE);
            }
        });
        mBinding.recordView.setOnBasketAnimationEndListener(new OnBasketAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                mBinding.btnEmoji.setVisibility(View.VISIBLE);
                mBinding.btnFile.setVisibility(View.VISIBLE);
                mBinding.btnCamera.setVisibility(View.VISIBLE);
                mBinding.edMessage.setVisibility(View.VISIBLE);
            }
        });

    }

    private void initBtnClick() {
        mBinding.normalToolbarView.setOnClickListener(v -> {
            Fragment fragment = new ConversationDetailFragment();
            Bundle bundle = new Bundle();
            if (conversation != null) {
                if (conversation.getType().equals("one-to-one")) {
                    for (Users users : conversation.getMembers()) {
                        if (!users.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                            bundle.putSerializable("object", users);
                            bundle.putSerializable("conversation", conversation);
                            fragment.setArguments(bundle);
                            break;
                        }
                    }
                } else {
                    bundle.putSerializable("conversation", conversation);
                    fragment.setArguments(bundle);
                }
            } else {
                bundle.putSerializable("object", user);
                fragment.setArguments(bundle);
            }
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.add(R.id.fragment, fragment, "conversation_detail");
            transaction.addToBackStack(null);
            transaction.commit();
        });
        mBinding.detail.setOnClickListener(v -> {
            Fragment fragment = new ConversationDetailFragment();
            Bundle bundle = new Bundle();
            if (conversation != null) {
                if (conversation.getType().equals("one-to-one")) {
                    for (Users users : conversation.getMembers()) {
                        if (!users.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                            bundle.putSerializable("object", users);
                            bundle.putSerializable("conversation", conversation);
                            fragment.setArguments(bundle);
                            break;
                        }
                    }
                } else {
                    bundle.putSerializable("conversation", conversation);
                    fragment.setArguments(bundle);
                }
            } else {
                bundle.putSerializable("object", user);
                fragment.setArguments(bundle);
            }
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.add(R.id.fragment, fragment, "conversation_detail");
            transaction.addToBackStack(null);
            transaction.commit();
        });

        mBinding.btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mBinding.edMessage.getText().toString())) {
                    String messageSend = textMessage.getText().toString();
                    if (messageSend.length() > 0) {

                        MessageData messageData = new MessageData();
                        messageData.setId(UUID.randomUUID().toString());
                        messageData.setText(messageSend);
                        messageData.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                        if (Patterns.WEB_URL.matcher(textMessage.getText().toString()).matches())
                            messageData.setType(MessageData.TYPE.LINK);
                        else
                            messageData.setType(MessageData.TYPE.TEXT);

                        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                        messageData.setDateTime(utcFormat.format(new Date()));
                        messageData.setStatus("sending");

                        if (conversation == null) {
                            viewModel.setMessage(messageData);
                            viewModel.createConversation(user);
                        } else {
                            messageData.setConversationId(conversation.get_id());
                            mChatAdapter.getmMessageData().add(messageData);
                            mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                            mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                        }
                    }
                    mBinding.edMessage.setText("");
                }
            }
        });

        mBinding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mBinding.backing.setOnClickListener(v -> {
            onBackPressed();
        });
        mBinding.imageProfile.setOnClickListener(v -> {
            onBackPressed();
        });

        mBinding.btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomDialog();
            }
        });

        mBinding.btnCamera.setOnClickListener(v -> {
            if (checkCameraPermission()) {
                openCamera();
            } else {
                requestCameraPermission(444);
            }
        });
    }

    private void showBottomDialog() {

        final Dialog d = new Dialog(this);
        LinearLayout btnGallery, btnDoc, btnAudio, btnCameraX, btnContact, btnLocation;
        d.setContentView(R.layout.dialog_layout_actions);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        d.setCancelable(true);

        btnGallery = d.findViewById(R.id.btn_gallery);
        btnDoc = d.findViewById(R.id.btn_doc);
        btnAudio = d.findViewById(R.id.btn_audio);
        btnCameraX = d.findViewById(R.id.btn_camera_x);
        btnContact = d.findViewById(R.id.btn_contact);
        btnLocation = d.findViewById(R.id.btn_location);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(d.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lWindowParams.gravity = Gravity.BOTTOM;
        lWindowParams.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        d.show();
        d.getWindow().setAttributes(lWindowParams);

        btnGallery.setOnClickListener(v -> {
            if (checkCameraPermission())
                openGallery();
            else requestCameraPermission(111);
            d.dismiss();
        });

        btnDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraPermission())
                    openFilePicker();
                else requestCameraPermission(222);
                d.dismiss();
            }
        });

        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraPermission())
                    openAudioPicker();
                else requestCameraPermission(333);
                d.dismiss();
            }
        });

        btnCameraX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraPermission()) {
                    openCamera();
                } else {
                    requestCameraPermission(444);
                }
                d.dismiss();
            }
        });

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChatActivity.this, SelectContactActivity.class);
                i.putExtra("share", "share");
                if (conversation != null) {
                    if (conversation.getType().equals("one-to-one"))
                        i.putExtra("hide_contact", user.getPhoneNo());
                }
                shareContact.launch(i);

                d.dismiss();
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ChatActivity.this, "Feature not available", Toast.LENGTH_SHORT).show();
                d.dismiss();
            }
        });

    }

    public void loadGif(ArrayList<String> gif_list) {
        for (int i = 0; i < gif_list.size(); i++) {
            String image_uri = gif_list.get(i);
            Uri uri = Uri.fromFile(new File(image_uri));
            file = new File(uri.getPath());
            new DialogReviewSendMedia(this, true, uri, null).loadGif(new DialogReviewSendMedia.OnCallBack() {
                @Override
                public void onButtonSendClick() {
                    MessageData message = new MessageData();
                    message.setType(MessageData.TYPE.GIF);
                    message.setStatus("sending");
                    message.setUrl(file.getPath());
                    message.setText(null);

                    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    message.setDateTime(utcFormat.format(new Date()));
                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                    message.setConversationId(conversation.get_id());

                    mChatAdapter.getmMessageData().add(message);
                    mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                    mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                }
            });
        }
    }

    private void openCamera() {
        Options options = Options.init()
                .setCount(1)
                .setFrontfacing(true)
                .setExcludeVideos(false)
                .setVideoDurationLimitinSeconds(30)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = new Intent(ChatActivity.this, Pix.class);
        intent.putExtra("options", options);

        cameraRequest.launch(intent);

    }

    private void openAudioPicker() {
        Intent intent = new Intent(ChatActivity.this, DocumentPickActivity.class);
        intent.putExtra(Constant.MAX_NUMBER, 1);
        intent.putExtra(DocumentPickActivity.SUFFIX, new String[]{".flac", ".aac", ".wma", ".wav", ".3gp", ".mp3", ".m4a"});
        audioRequest.launch(intent);
    }

    private void openFilePicker() {

        Intent intent = new Intent(ChatActivity.this, DocumentPickActivity.class);
        intent.putExtra(Constant.MAX_NUMBER, 1);
        intent.putExtra(DocumentPickActivity.SUFFIX, new String[]{".xlsx", ".xls", ".doc", ".docx", ".ppt", ".pptx", ".pdf", ".xlsm", ".xlsb", ".pptm", ".txt"});
        documentRequest.launch(intent);

    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/* video/*");
        photoPickerIntent.setType("image/jpeg, image/jpg, image/png, video/*");
        galleryRequest.launch(photoPickerIntent);
    }

    private boolean checkAllPermission() {
        int camera_result = ContextCompat.checkSelfPermission(this, permission.CAMERA);
        int write_external_strorage_result = ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, permission.RECORD_AUDIO);
        int modify_audio_result = ContextCompat.checkSelfPermission(this, permission.MODIFY_AUDIO_SETTINGS);
        int write_contact = ContextCompat.checkSelfPermission(this, permission.WRITE_CONTACTS);
        return camera_result == PackageManager.PERMISSION_DENIED ||
                write_external_strorage_result == PackageManager.PERMISSION_DENIED ||
                record_audio_result == PackageManager.PERMISSION_DENIED ||
                modify_audio_result == PackageManager.PERMISSION_DENIED ||
                write_contact == PackageManager.PERMISSION_DENIED;
    }

    private boolean checkAudioPermission() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_DENIED ||
                record_audio_result == PackageManager.PERMISSION_DENIED;
    }

    private boolean checkCameraPermission() {
        int camera_result = ContextCompat.checkSelfPermission(this, permission.CAMERA);
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE);
        return camera_result == PackageManager.PERMISSION_DENIED || write_external_storage_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                permission.WRITE_EXTERNAL_STORAGE,
                permission.RECORD_AUDIO
        }, REQUEST_CORD_PERMISSION);
    }

    private void requestCameraPermission(int reqCode) {
        ActivityCompat.requestPermissions(this, new String[]{
                permission.WRITE_EXTERNAL_STORAGE,
                permission.CAMERA
        }, reqCode);
    }

    private void requestAllPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                permission.WRITE_EXTERNAL_STORAGE,
                permission.CAMERA,
                permission.RECORD_AUDIO,
                permission.MODIFY_AUDIO_SETTINGS,
                permission.WRITE_CONTACTS,
        }, 555);
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void startRecord() {
        setUpMediaRecorder();
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording Error\nPlease restart your app ", Toast.LENGTH_LONG).show();
        }

    }

    private void stopRecord() {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpMediaRecorder() {
        File cachePath = new File(ResourcePlusApplication.getContext().getCacheDir(), "audios");
        if (!cachePath.exists())
            cachePath.mkdirs();
        file = new File(cachePath + "/" + "temp.wav");
        audio_path = file.getAbsolutePath();

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(audio_path);
        } catch (Exception e) {
            Log.d(TAG, "setUpMediaRecord: " + e.getMessage());
        }
    }

    private void reviewImage(Bitmap bitmap, Uri uri) {
        new DialogReviewSendMedia(this, bitmap, true).show(() -> {
            if (uri != null) {
                String imagePath;
                Cursor cursor = null;
                try {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(uri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    imagePath = cursor.getString(column_index);
                } catch (Exception e) {
                    imagePath = uri.getPath();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                File file = new File(imagePath);
                if (isFileBiggerThan10MB(file)) {
                    Toast.makeText(ChatActivity.this, "Upload limit exceeded. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                } else {
                    MessageData message = new MessageData();
                    message.setId(UUID.randomUUID().toString());
                    message.setType(MessageData.TYPE.PICTURE);
                    message.setUrl(imagePath);
                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                    message.setStatus("sending");
                    message.setText(null);

                    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    message.setDateTime(utcFormat.format(new Date()));
                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                    if (conversation == null) {
                        viewModel.setMessage(message);
                        viewModel.createConversation(user);
                    } else {
                        message.setConversationId(conversation.get_id());
                        mChatAdapter.getmMessageData().add(message);
                        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                    }
                }
            }

        });
    }

    private void getMessage(String phoneId, String messageId, String status) {
        MessageData message = viewModel.getMessage(messageId);
        if (message != null) {
            if (conversation.getType().equals("group")) {
                List<MessageStatesRecord> records = message.getStatesRecords();
                boolean isFound = false;
                if (records.size() > 0) {
                    for (MessageStatesRecord record : records) {
                        if (record.getPhoneId().equals(phoneId)) {
                            records.get(records.indexOf(record)).setSeen(status.equals("seen"));
                            records.get(records.indexOf(record)).setDelivered(status.equals("delivered"));
                            isFound = true;
                            break;
                        }
                    }
                }
                if (!isFound) {
                    MessageStatesRecord record = new MessageStatesRecord(phoneId, status.equals("delivered"), status.equals("seen"));
                    records.add(record);
                }
                int count = 0;
                for (MessageStatesRecord record1 : records) {
                    if (record1.isSeen())
                        count++;
                }
                if (count >= conversation.getMembers().size() - 1) {
                    message.setStatus("seen");
                } else
                    message.setStatus("delivered");
            } else
                message.setStatus(status);
            viewModel.updateMessage(message);
            int index = mChatAdapter.getPositionForMessage(messageId);
            viewModel.updateListItem(index, message);
        }
    }

    private void invalidateViews() {
        if (conversation != null) {
            if (conversation.getType().equals("one-to-one")) {
                for (Users member : conversation.getMembers()) {
                    if (!member.getPhoneId().equals(preferences.getUserDetails().getPhoneId())) {
                        user = member;
                        contact = viewModel.getContact(member.getPhoneNo());
                        if (contact != null)
                            mBinding.tvUsername.setText(contact.getName());
                        else
                            mBinding.tvUsername.setText(member.getPhoneNo());

                        if (member.getPhoto() != null && !member.getPhoto().equals("") && !member.getPhoto().equals("null")) {
                            Glide.with(this).load(member.getPhoto()).placeholder(R.drawable.no_pic).into(mBinding.imageProfile);
                        } else if (conversation.getImage() != null && !conversation.getImage().equals("") && !conversation.getImage().equals("null"))
                            Glide.with(this).load(conversation.getImage()).placeholder(R.drawable.no_pic).into(mBinding.imageProfile);
                        else
                            mBinding.imageProfile.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.no_pic));

                        break;
                    }
                }
            } else {
                mBinding.tvUsername.setText(conversation.getTitle());

                if (conversation.getImage() != null && !conversation.getImage().equals("") && !conversation.getImage().equals("null")) {
                    Glide.with(this).load(conversation.getImage()).into(mBinding.imageProfile);
                } else
                    mBinding.imageProfile.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.no_pic));
            }
        } else {
            contact = viewModel.getContact(user.getPhoneNo());
            if (contact != null)
                mBinding.tvUsername.setText(contact.getName());
            else
                mBinding.tvUsername.setText(user.getPhoneNo());

            if (user.getPhoto() != null && !user.getPhoto().equals("") && !user.getPhoto().equals("null")) {
                Glide.with(ChatActivity.this).load(user.getPhoto()).placeholder(R.drawable.no_pic).into(mBinding.imageProfile);
            } else if (conversation.getImage() != null && !conversation.getImage().equals("") && !conversation.getImage().equals("null"))
                Glide.with(ChatActivity.this).load(conversation.getImage()).placeholder(R.drawable.no_pic).into(mBinding.imageProfile);
            else
                mBinding.imageProfile.setImageDrawable(ContextCompat.getDrawable(ChatActivity.this, R.drawable.no_pic));
        }
    }

    public static Boolean isFileBiggerThan10MB(File file) {
        long fileSizeInBytes = file.length();
        double fileSizeInKB = fileSizeInBytes / 1024.0;
        double fileSizeInMB = fileSizeInKB / 1024.0;

        return fileSizeInMB > 10.0;
    }

    private void getAllUnseenMessages(List<MessageData> unreadMessages) {
        if (unreadMessages.size() > 0) {
            for (MessageData message : unreadMessages) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("messageId", message.getId());
                    jsonObject.put("to", message.getSenderPhoneId());
                    jsonObject.put("from", preferences.getUserDetails().getPhoneId());
                    jsonObject.put("conversationId", conversation.get_id());
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }

                if (!ResourcePlusApplication.mSocket.connected())
                    ResourcePlusApplication.mSocket.connect();
                ResourcePlusApplication.mSocket.emit("seen", jsonObject);

                message.setStatus("seen");
                viewModel.updateMessage(message);
            }
        }
    }

    private FragmentManager.OnBackStackChangedListener getListener() {

        return () -> {
            FragmentManager manager = getSupportFragmentManager();
            Fragment currFrag = manager.findFragmentById(R.id.fragment);

            if (currFrag != null)
                currFrag.onResume();
            else {
                if (conversation != null)
                    viewModel.updateList(conversation.get_id());
                else
                    invalidateViews();
            }
        };
    }

    @Override
    public void onCancel(int pos) {
        viewModel.cancelCall();
    }

    ActivityResultLauncher<Intent> addContactToContacts = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    saveContact(result.getData().getData());
                }
            });

    ActivityResultLauncher<Intent> documentRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            results -> {
                if (results.getResultCode() == Activity.RESULT_OK && results.getData() != null) {
                    ArrayList<DocumentFile> list = results.getData().getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                    if (list.size() > 0) {
                        File tempFile = null;
                        for (DocumentFile file : list) {
                            tempFile = new File(file.getPath());
                        }

                        if (isFileBiggerThan10MB(tempFile)) {
                            Toast.makeText(ChatActivity.this, "Upload limit exceed. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                        } else {

                            MessageData message = new MessageData();
                            message.setId(UUID.randomUUID().toString());
                            message.setType(MessageData.TYPE.DOCUMENTS);
                            message.setUrl(tempFile.getAbsolutePath());
                            message.setStatus("sending");

                            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            message.setDateTime(utcFormat.format(new Date()));
                            message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                            if (conversation == null) {
                                viewModel.setMessage(message);
                                viewModel.createConversation(user);
                            } else {
                                message.setConversationId(conversation.get_id());
                                mChatAdapter.getmMessageData().add(message);
                                mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                                mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                            }
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> audioRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            results -> {
                if (results.getResultCode() == Activity.RESULT_OK && results.getData() != null) {
                    ArrayList<DocumentFile> list = results.getData().getParcelableArrayListExtra(Constant.RESULT_PICK_FILE);
                    if (list.size() > 0) {
                        File tempFile = null;
                        for (DocumentFile file : list) {
                            tempFile = new File(file.getPath());
                        }

                        if (isFileBiggerThan10MB(tempFile)) {
                            Toast.makeText(ChatActivity.this, "Upload limit exceed. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                        } else {

                            MessageData message = new MessageData();
                            message.setId(UUID.randomUUID().toString());
                            message.setType(MessageData.TYPE.AUDIO);
                            message.setUrl(tempFile.getAbsolutePath());
                            message.setStatus("sending");

                            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                            message.setDateTime(utcFormat.format(new Date()));
                            message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                            if (conversation == null) {
                                viewModel.setMessage(message);
                                viewModel.createConversation(user);
                            } else {
                                message.setConversationId(conversation.get_id());
                                mChatAdapter.getmMessageData().add(message);
                                mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                                mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                            }
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> cameraRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            results -> {
                if (results.getResultCode() == Activity.RESULT_OK && results.getData() != null) {
                    ArrayList<Uri> uriList = new ArrayList<>();
                    ArrayList<String> mResults = results.getData().getStringArrayListExtra(Pix.IMAGE_RESULTS);
                    assert mResults != null;
                    Uri uri;
                    for (String result : mResults) {
                        if (result.contains(".jpeg") || result.contains(".jpg") || result.contains(".png"))
                            uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(result));
                        else
                            uri = Uri.fromFile(new File(result));
                        uriList.add(uri);
                    }
                    if (uriList.size() > 0) {
                        if (uriList.get(0).getPath().contains(".jpeg") || uriList.get(0).getPath().contains(".jpg") || uriList.get(0).getPath().contains(".png")) {
                            performCrop(uriList.get(0));
                        } else {
                            String imagePath;
                            Cursor cursor = null;
                            try {
                                String[] proj = {MediaStore.Images.Media.DATA};
                                cursor = getContentResolver().query(uriList.get(0), proj, null, null, null);
                                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                                cursor.moveToFirst();
                                imagePath = cursor.getString(column_index);
                            } catch (Exception e) {
                                imagePath = uriList.get(0).getPath();
                            } finally {
                                if (cursor != null) {
                                    cursor.close();
                                }
                            }

                            if (imagePath.contains(".gif")) {
                                Uri gifUri = Uri.fromFile(new File(imagePath));
                                String finalImagePath1 = imagePath;
                                new DialogReviewSendMedia(this, true, gifUri, null).loadGif(new DialogReviewSendMedia.OnCallBack() {
                                    @Override
                                    public void onButtonSendClick() {
                                        MessageData message = new MessageData();
                                        message.setId(UUID.randomUUID().toString());
                                        message.setType(MessageData.TYPE.GIF);
                                        message.setStatus("sending");
                                        message.setUrl(finalImagePath1);
                                        message.setText(null);

                                        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                                        message.setDateTime(utcFormat.format(new Date()));
                                        message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                                        if (conversation == null) {
                                            viewModel.setMessage(message);
                                            viewModel.createConversation(user);
                                        } else {
                                            message.setConversationId(conversation.get_id());
                                            mChatAdapter.getmMessageData().add(message);
                                            mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                                            mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                                        }
                                    }
                                });
                            } else {
                                String finalImagePath = imagePath;
                                new DialogReviewSendMedia(ChatActivity.this).show(imagePath, () -> {

                                    if (isFileBiggerThan10MB(new File(finalImagePath))) {
                                        Toast.makeText(ChatActivity.this, "Upload limit exceed. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                                    } else {

                                        MessageData message = new MessageData();
                                        message.setId(UUID.randomUUID().toString());
                                        message.setType(MessageData.TYPE.VIDEO);
                                        message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                                        message.setStatus("sending");
                                        message.setUrl(finalImagePath);
                                        message.setText(null);

                                        DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                        utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                                        message.setDateTime(utcFormat.format(new Date()));
                                        message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                                        if (conversation == null) {
                                            viewModel.setMessage(message);
                                            viewModel.createConversation(user);
                                        } else {
                                            message.setConversationId(conversation.get_id());
                                            mChatAdapter.getmMessageData().add(message);
                                            mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                                            mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> galleryRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    String imagePath;
                    Cursor cursor = null;
                    try {
                        String[] proj = {MediaStore.Images.Media.DATA};
                        cursor = getContentResolver().query(imageUri, proj, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        imagePath = cursor.getString(column_index);
                    } catch (Exception e) {
                        imagePath = imageUri.getPath();
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    if (imageUri.toString().contains("image")) {
                        if (imagePath.contains("gif")) {
                            Uri gifUri = Uri.fromFile(new File(imagePath));
                            String finalImagePath1 = imagePath;
                            new DialogReviewSendMedia(this, true, gifUri, null).loadGif(new DialogReviewSendMedia.OnCallBack() {
                                @Override
                                public void onButtonSendClick() {
                                    MessageData message = new MessageData();
                                    message.setId(UUID.randomUUID().toString());
                                    message.setType(MessageData.TYPE.GIF);
                                    message.setStatus("sending");
                                    message.setUrl(finalImagePath1);
                                    message.setText(null);

                                    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                                    message.setDateTime(utcFormat.format(new Date()));
                                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                                    if (conversation == null) {
                                        viewModel.setMessage(message);
                                        viewModel.createConversation(user);
                                    } else {
                                        message.setConversationId(conversation.get_id());
                                        mChatAdapter.getmMessageData().add(message);
                                        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                                        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                                    }
                                }
                            });
                        } else
                            performCrop(imageUri);
                    } else {
                        String finalImagePath = imagePath;
                        new DialogReviewSendMedia(ChatActivity.this).show(imagePath, new DialogReviewSendMedia.OnCallBack() {
                            @Override
                            public void onButtonSendClick() {

                                if (isFileBiggerThan10MB(new File(finalImagePath))) {
                                    Toast.makeText(ChatActivity.this, "Upload limit exceed. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                                } else {

                                    MessageData message = new MessageData();
                                    message.setId(UUID.randomUUID().toString());
                                    message.setType(MessageData.TYPE.VIDEO);
                                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                                    message.setStatus("sending");
                                    message.setUrl(finalImagePath);
                                    message.setText(null);

                                    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                                    message.setDateTime(utcFormat.format(new Date()));
                                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());

                                    if (conversation == null) {
                                        viewModel.setMessage(message);
                                        viewModel.createConversation(user);
                                    } else {
                                        message.setConversationId(conversation.get_id());
                                        mChatAdapter.getmMessageData().add(message);
                                        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
                                        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
                                    }
                                }
                            }
                        });
                    }
                }
            });

    ActivityResultLauncher<Intent> shareContact = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<Users> list = new Gson().fromJson(result.getData().getStringExtra("users"), new TypeToken<List<Users>>() {
                    }.getType());
                    if (list.size() > 0) {
                        addMessagesToConversation(list);
                    }
                }
            });

    private void addMessagesToConversation(List<Users> list) {

        for (Users users : list) {
            String text = users.getPhoto() + ";" + users.getPhoneNo() + ";" + users.getPhoneId();
            MessageData messageData = new MessageData();
            messageData.setId(UUID.randomUUID().toString());
            messageData.setText(text);
            messageData.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
            messageData.setType(MessageData.TYPE.CONTACT);
            DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            messageData.setDateTime(utcFormat.format(new Date()));
            messageData.setStatus("sending");

            messageData.setConversationId(conversation.get_id());
            mChatAdapter.getmMessageData().add(messageData);
            mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
            mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateAdapterItems(MessageData messageData) {
        mChatAdapter.getmMessageData().add(messageData);
        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
    }
}
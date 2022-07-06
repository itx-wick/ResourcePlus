package com.mr_w.resourceplus.fragments.conversation_detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.adapter.CommonGroupAdapter;
import com.mr_w.resourceplus.adapter.MediaAdapter;
import com.mr_w.resourceplus.adapter.ParticipantsAdapter;
import com.mr_w.resourceplus.async.SyncContacts;
import com.mr_w.resourceplus.callbacks.GenericCallbacks;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.databinding.FragmentConversationDetailBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.fragments.group_edit.GroupEditFragment;
import com.mr_w.resourceplus.fragments.media_gallery.MediaGalleryFragment;
import com.mr_w.resourceplus.fragments.media_preview.MediaPreviewFragment;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.network.remote.ApiEndPoints;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.CommonGroup;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.server_call.GenericServerCalls;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationDetailFragment extends BaseFragment<FragmentConversationDetailBinding, ConversationDetailViewModel> implements CommonGroupAdapter.ClickListener,
        ParticipantsAdapter.ClickListener,
        ConversationDetailNavigator {

    private Users user = null;
    private Conversation conversation = null;
    private FragmentConversationDetailBinding mBinding;
    private MediaAdapter mediaAdapter;
    private Users me;
    private ParticipantsAdapter adapter;
    private DataManager preferences;
    private StringBuilder names;
    private PopupMenu popup;
    private LocalContacts recentContactToAdd;
    private LocalContacts contact;
    public boolean isNewThread;
    private ProgressDialog progressDialog;
    private boolean isEdited = false;
    private RequestQueue requestQueue;
    private RequestOptions requestOptions = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();

        preferences = viewModel.getDataManager();
        user = getArguments().containsKey("object") ? (Users) getArguments().getSerializable("object") : null;
        conversation = getArguments().containsKey("conversation") ? (Conversation) getArguments().getSerializable("conversation") : null;
        me = preferences.getUserDetails();

        viewModel.setConversation(conversation);
        viewModel.setMe(me);
        viewModel.setUser(user);
        viewModel.setFragment(new WeakReference<>(this));

        observers();

    }

    private void observers() {

        viewModel.getMediaList().observe(getViewLifecycleOwner(), new Observer<List<Media>>() {
            @Override
            public void onChanged(List<Media> media) {
                setMediaRecyclerView();
            }
        });

        viewModel.getCommonGroupList().observe(getViewLifecycleOwner(), new Observer<List<CommonGroup>>() {
            @Override
            public void onChanged(List<CommonGroup> commonGroups) {
                setCommonGroupAdapter();
            }
        });

        viewModel.getParticipantList().observe(getViewLifecycleOwner(), new Observer<List<Participant>>() {
            @Override
            public void onChanged(List<Participant> participants) {
                setParticipantRecyclerView();
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                mBinding.participants.setLayoutManager(layoutManager);
                mBinding.participants.setItemAnimator(new DefaultItemAnimator());
                mBinding.participants.setAdapter(adapter);
                mBinding.totalParticipants.setText(viewModel.getParticipantList().getValue().size() + " participants");
            }
        });
    }

    @Override
    public void onResume() {

        if (conversation != null)
            renderViews();
        else
            populateOneToOneData();

        super.onResume();
    }

    @Override
    public int getBindingVariable() {
        return BR.conv_detail;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_conversation_detail;
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onClick(int pos) {
        viewModel.commonGroupClick(pos);
    }

    @Override
    public void onClick(int pos, Participant participant, View v) {
        viewModel.participantsClick(participant, v);
    }

    private void populateOneToOneData() {

        viewModel.fetchCommonGroups();

        contact = viewModel.getContact(user.getPhoneNo());
        if (isEdited) {
            if (contact != null)
                updateContact(contact.getContactId());
            isEdited = false;
        }
        if (user != null) {
            if (user.getPhoto() != null && !user.getPhoto().equals("null")) {
                Glide.with(getActivity()).load(user.getPhoto()).into(mBinding.profileImage);
            }
        }

        if (contact != null)
            mBinding.title.setText(contact.getName());
        else mBinding.title.setText(user.getPhoneNo());
        if (conversation != null)
            conversation.setTitle(user.getName());
        mBinding.status.setText(user.getAbout() != null ? user.getAbout() : "Hey there! I am using ResourcePlus");
        mBinding.number.setText(user.getPhoneNo());

    }

    private void populateGroupData() {

        mBinding.title.setText(conversation.getTitle());
        if (conversation.getDescription() != null && !conversation.getDescription().equals("")) {
            mBinding.groupDescription.setText(conversation.getDescription());
            mBinding.addDescription.setVisibility(View.GONE);
            mBinding.setDescription.setVisibility(View.VISIBLE);
        }

        if (!conversation.getAdmin().contains(me.getPhoneId())) {
            mBinding.edit.setVisibility(View.GONE);
            mBinding.info.setVisibility(View.VISIBLE);
            mBinding.addMember.setVisibility(View.GONE);
            mBinding.addparticipants.setVisibility(View.GONE);
            mBinding.invite.setVisibility(View.GONE);
            mBinding.div1.setVisibility(View.GONE);
            mBinding.div2.setVisibility(View.GONE);
        }

        viewModel.fetchParticipants();
    }

    private void performSearch(String toString) {
        List<Participant> participants = viewModel.getParticipantList().getValue();
        List<Participant> temp = new ArrayList<>();
        for (Participant participant : participants) {
            LocalContacts contact = viewModel.getContact(participant.getUser().getPhoneNo());
            if (contact != null) {
                if (contact.getName().toLowerCase().contains(toString.toLowerCase())) {
                    temp.add(participant);
                }
            }
        }
        viewModel.updateParticipantList(temp);
    }

    private void setCommonGroupAdapter() {
        if (viewModel.getCommonGroupList().getValue().size() == 0) {
            mBinding.commonGroups.setVisibility(View.GONE);
        } else {

            mBinding.commonGroups.setVisibility(View.VISIBLE);

            CommonGroupAdapter adapter = new CommonGroupAdapter(getContext(), viewModel.getCommonGroupList().getValue());
            adapter.setClickListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            mBinding.groupList.setLayoutManager(layoutManager);
            mBinding.groupList.setItemAnimator(new DefaultItemAnimator());
            mBinding.groupList.setAdapter(adapter);
        }
    }

    private void setParticipantRecyclerView() {

        if (viewModel.getParticipantList().getValue().size() == 0) {
            mBinding.participantsLayout.setVisibility(View.GONE);
        } else {
            mBinding.participantsLayout.setVisibility(View.VISIBLE);
        }

        List<Participant> participants = viewModel.getParticipantList().getValue();
        List<Participant> temp = new ArrayList<>();
        for (Participant p : participants) {
            if (conversation.getRemovedMembers().contains(p.getUser().getPhoneId()))
                temp.add(p);
        }
        participants.removeAll(temp);
        Collections.sort(participants, Participant.adminOnTop);
        adapter = new ParticipantsAdapter(getActivity(), participants, this);
        adapter.setMe(preferences.getUserDetails());
        adapter.setCreator(conversation.getCreator());
    }

    private void setMediaRecyclerView() {

        if (viewModel.getMediaList().getValue().size() == 0) {
            mBinding.medias.setVisibility(View.GONE);
            return;
        } else {
            mBinding.totalMedias.setText(String.valueOf(viewModel.getMediaList().getValue().size()));
            mBinding.medias.setVisibility(View.VISIBLE);
        }

        mediaAdapter = new MediaAdapter(getContext(), viewModel.getMediaList().getValue());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mBinding.mediaList.setLayoutManager(layoutManager);
        mBinding.mediaList.setItemAnimator(new DefaultItemAnimator());
        mBinding.mediaList.setAdapter(mediaAdapter);
        mBinding.mediaList.smoothScrollToPosition(mediaAdapter.getMediaList().size() - 1);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager manager
                    = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveContact(LocalContacts recentContactToAdd) {
        ContentResolver contentResolver = getActivity().getContentResolver();

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(recentContactToAdd.getNumber()));

        Cursor cursor =
                contentResolver.query(
                        uri,
                        new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME,
                                ContactsContract.PhoneLookup._ID,
                                ContactsContract.Data.LOOKUP_KEY
                        },
                        null,
                        null,
                        null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    recentContactToAdd.setName(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)));
                    recentContactToAdd.setContactId(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)));
                    recentContactToAdd.setLookupKey(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Data.LOOKUP_KEY)));
                    viewModel.insertContact(recentContactToAdd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            populateOneToOneData();
//            initializeContactsJob();
        }
    }

    private void updateContact(String contactId) {
        ContentResolver contentResolver = getActivity().getContentResolver();

        Cursor cursor = contentResolver.query(
                CommonDataKinds.Phone.CONTENT_URI,
                null,
                CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId}, null);

        while (cursor.moveToNext()) {
            recentContactToAdd.setNumber(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)));
            recentContactToAdd.setName(cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME)));

            String phoneNo = recentContactToAdd.getNumber().replaceAll("[^0-9+]", "");
            if (phoneNo.length() >= 11 && phoneNo.length() <= 15) {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try {
                    if (phoneNo.startsWith("00")) {
                        phoneNo = phoneNo.replace(phoneNo.substring(0, 2), "+");
                    } else if (phoneNo.startsWith("0")) {
                        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
                        String countryCodeValue = tm.getNetworkCountryIso();
                        phoneNo = phoneUtil.format(phoneUtil.parse(phoneNo, countryCodeValue.toUpperCase()), PhoneNumberUtil.PhoneNumberFormat.E164);
                    }

                    recentContactToAdd.setNumber(phoneNo);
                    viewModel.insertContact(recentContactToAdd);

                } catch (NumberParseException e) {
                    e.printStackTrace();
                }
            }
        }
        cursor.close();
    }

    private void findContactByNumber(Participant participant, View v) {
        LocalContacts contact = viewModel.getContact(participant.getUser().getPhoneNo());
        boolean isAdmin = false;
        if (conversation.getAdmin().contains(preferences.getUserDetails().getPhoneId()))
            isAdmin = true;
        popup = new PopupMenu(getContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_participants, popup.getMenu());

        if (contact != null) {
            popup.getMenu().findItem(R.id.message).setTitle("Message " + contact.getName());
            popup.getMenu().findItem(R.id.view).setVisible(true);
            popup.getMenu().findItem(R.id.add_to_contacts).setVisible(false);

            if (isAdmin) {
                popup.getMenu().findItem(R.id.view_detail).setVisible(true);
                popup.getMenu().findItem(R.id.view_detail).setTitle("View " + participant.getUser().getName());
                popup.getMenu().findItem(R.id.admin).setVisible(!participant.isAdmin());
                popup.getMenu().findItem(R.id.dismiss).setVisible(participant.isAdmin());
                popup.getMenu().findItem(R.id.remove).setVisible(true);
                popup.getMenu().findItem(R.id.remove).setTitle("Remove " + contact.getName());
            }
        } else {
            popup.getMenu().findItem(R.id.message).setTitle("Message " + participant.getUser().getPhoneNo());
            popup.getMenu().findItem(R.id.view).setVisible(false);
            popup.getMenu().findItem(R.id.add_to_contacts).setVisible(true);

            if (isAdmin) {
                popup.getMenu().findItem(R.id.view_detail).setVisible(true);
                popup.getMenu().findItem(R.id.view_detail).setTitle("View " + participant.getUser().getPhoneNo());
                popup.getMenu().findItem(R.id.admin).setVisible(!participant.isAdmin());
                popup.getMenu().findItem(R.id.dismiss).setVisible(participant.isAdmin());
                popup.getMenu().findItem(R.id.remove).setVisible(true);
                popup.getMenu().findItem(R.id.remove).setTitle("Remove " + participant.getUser().getPhoneNo());
            }
        }

        popup.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.message) {

                openChatScreen(participant.getUser());

            } else if (item.getItemId() == R.id.view_detail) {

                String me = preferences.getUserDetails().getPhoneId();
                Conversation temp = viewModel.isConversationExists(me, participant.getUser().getPhoneId());

                Fragment fragment = new ConversationDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("object", participant.getUser());
                if (temp != null) {
                    bundle.putSerializable("conversation", temp);
                }
                fragment.setArguments(bundle);
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
                ft.add(R.id.fragment, fragment, "detail");
                ft.addToBackStack(null);
                ft.commit();

            } else if (item.getItemId() == R.id.admin) {

                viewModel.makeParticipantAsAdmin(participant);

            } else if (item.getItemId() == R.id.dismiss) {

                if (!conversation.getCreator().equals(participant.getUser().getPhoneId()))
                    viewModel.removeAdmin(participant);
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.info_theme)).create();
                    alertDialog.setMessage("you can not dismiss the creator from administration");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);

                    alertDialog.show();

                    final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                    positiveButtonLL.gravity = Gravity.END;
                    positiveButton.setLayoutParams(positiveButtonLL);
                }

            } else if (item.getItemId() == R.id.remove) {

                if (!conversation.getCreator().equals(participant.getUser().getPhoneId()))
                    viewModel.removeMemberFromConversation(participant);
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.info_theme)).create();
                    alertDialog.setMessage("you can not remove the creator from group");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setCancelable(true);
                    alertDialog.setCanceledOnTouchOutside(true);

                    alertDialog.show();

                    final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
                    positiveButtonLL.gravity = Gravity.END;
                    positiveButton.setLayoutParams(positiveButtonLL);
                }

            } else if (item.getItemId() == R.id.view) {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.getContactId());
                intent.setData(uri);
                getActivity().startActivity(intent);

            } else if (item.getItemId() == R.id.add_to_contacts) {

                recentContactToAdd = new LocalContacts();
                recentContactToAdd.setName(participant.getUser().getName());
                recentContactToAdd.setNumber(participant.getUser().getPhoneNo());

                Intent i = new Intent(Intent.ACTION_INSERT);
                i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                i.putExtra(ContactsContract.Intents.Insert.NAME, participant.getUser().getName());
                i.putExtra(ContactsContract.Intents.Insert.PHONE, participant.getUser().getPhoneNo());
                i.putExtra("finishActivityOnSaveCompleted", true);
                addContactToContacts.launch(i);

            }

            return true;
        });

        popup.show();
    }

    private void renderViews() {
        conversation = viewModel.getConversation(conversation.get_id());
        if (conversation != null) {
            viewModel.fetchMedia();

            if (!conversation.getType().equals("one-to-one")) {
                mBinding.userLayout.setVisibility(View.GONE);
                mBinding.addDescription.setVisibility(View.VISIBLE);
                mBinding.participantsLayout.setVisibility(View.VISIBLE);
                mBinding.edit.setVisibility(View.VISIBLE);
                mBinding.addMember.setVisibility(View.VISIBLE);
                mBinding.more.setVisibility(View.GONE);

                populateGroupData();
            } else
                populateOneToOneData();

            if (conversation.getType().equals("group")) {
                requestOptions = new RequestOptions()
                        .placeholder(R.drawable.group_icon)
                        .error(R.drawable.group_icon);
            } else {
                requestOptions = new RequestOptions()
                        .placeholder(R.drawable.icon_male_ph)
                        .error(R.drawable.icon_male_ph);
            }

            if (user != null) {
                if (user.getPhoto() != null && !user.getPhoto().equals("null")) {
                    Glide.with(getActivity()).setDefaultRequestOptions(requestOptions).load(user.getPhoto()).into(mBinding.profileImage);
                }
            } else {
                if (conversation.getImage() != null && !conversation.getImage().equals("null")) {
                    Glide.with(getActivity()).setDefaultRequestOptions(requestOptions).load(conversation.getImage()).into(mBinding.profileImage);
                }
            }
        }
    }

    private void openChatScreen(Users user) {
        String me = preferences.getUserDetails().getPhoneId();
        Conversation temp = viewModel.isConversationExists(me, user.getPhoneId());
        if (temp != null) {
            startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("conversation", temp));
        } else {
            startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("user", user));
        }
        getActivity().finish();
    }

    public void initializeContactsJob() {
        new SyncContacts(getActivity(), new BackgroundCallbacks<List<LocalContacts>>() {

            @Override
            public void onPreProcessing() {
                super.onPreProcessing();
                preferences.saveBoolean(UserPreferences.PREF_BG_SYNCING, true);
            }

            @Override
            public void onFailure() {
                preferences.saveBoolean(UserPreferences.PREF_BG_SYNCING, false);
            }

            @Override
            public void onCompleted(List<LocalContacts> list) {
                UserPreferences.getInstance(getActivity(), "UserPrefs").saveInt(UserPreferences.PREF_LOCAL_CONTACTS_COUNT, list.size());
                uploadContacts(list, getActivity());
            }
        }).execute();
    }

    public void uploadContacts(List<LocalContacts> list, Activity activity) {

        UserPreferences userPrefs = UserPreferences.getInstance(activity, "UserPrefs");
        UserPreferences syncedPrefs = UserPreferences.getInstance(activity, "SyncedPrefs");
        Database db = Database.getInstance(activity);

        String number = userPrefs.getUserDetails().getPhoneNo();

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", list.get(i).getName());
                jsonObject.put("phoneNo", list.get(i).getNumber());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }

        JSONObject postData = new JSONObject();
        try {
            postData.put("number", number);
            postData.put("contactList", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        requestQueue = Volley.newRequestQueue(activity);
        GenericServerCalls genericServerCalls = new GenericServerCalls(activity,
                ApiEndPoints.ENDPOINT_CONTACTS_VALIDATION,
                requestQueue,
                Request.Method.POST,
                false);

        genericServerCalls.jsonRequest(postData, new GenericCallbacks<JSONObject>() {

            @Override
            public void onJsonSuccess(JSONObject response) {
                super.onJsonSuccess(response);

                try {
                    boolean status = response.getBoolean("success");
                    if (status) {
                        JSONArray array = response.getJSONArray("existingContactsFromNumber1");

                        for (int i = 0; i < array.length(); i++) {
                            if (!array.getJSONObject(i).getString("_id").equals(userPrefs.getUserDetails().getPhoneId())) {
                                Users users = new Users();
                                users.setUserId(String.valueOf(i + 1));
                                users.setName(array.getJSONObject(i).getString("name"));
                                users.setPhoneNo(array.getJSONObject(i).getString("phoneNo"));
                                users.setPhoneId(array.getJSONObject(i).getString("_id"));
                                users.setPhoto(array.getJSONObject(i).has("image") ? array.getJSONObject(i).getString("image") : "");

                                if (!db.isUserExists(users.getPhoneId()))
                                    db.insertUser(users);
                            }
                        }
                        syncedPrefs.saveBoolean(UserPreferences.PREF_CONTACTS_UPLOADED, true);
                        userPrefs.saveBoolean(UserPreferences.PREF_BG_SYNCING, false);
                        Intent i = new Intent("contacts_sync");
                        i.putExtra("data", "true");
                        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
                    } else {
                        Toast.makeText(activity, response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        });

    }

    ActivityResultLauncher<Intent> addMemberToGroup = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<Users> list = new Gson().fromJson(result.getData().getStringExtra("users"), new TypeToken<List<Users>>() {
                    }.getType());
                    if (list.size() > 0)
                        viewModel.addMembersToConversation(list);
                }
            });

    ActivityResultLauncher<Intent> addContactToContacts = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    saveContact(recentContactToAdd);
                }
            });

    ActivityResultLauncher<Intent> shareContact = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    List<Users> list = new Gson().fromJson(result.getData().getStringExtra("users"), new TypeToken<List<Users>>() {
                    }.getType());
                    if (list.size() > 0) {
                        viewModel.shareContact(list);
                    }
                }
            });

    @Override
    public void goBack() {
        getActivity().onBackPressed();
    }

    @Override
    public void backSearch() {
        mBinding.clear.performClick();
        closeKeyboard();
        mBinding.primaryLayout.setVisibility(View.VISIBLE);
        mBinding.secondaryLayout.setVisibility(View.GONE);

        viewModel.getParticipantList().removeObservers(getViewLifecycleOwner());
        viewModel.getCommonGroupList().removeObservers(getViewLifecycleOwner());
        viewModel.getMediaList().removeObservers(getViewLifecycleOwner());
        observers();
    }

    @Override
    public void clearSearch() {
        mBinding.clear.setVisibility(View.GONE);
        mBinding.searchBar.setText("");
    }

    @Override
    public void more(View v) {
        contact = viewModel.getContact(user.getPhoneNo());
        popup = new PopupMenu(getContext(), v);
        popup.getMenuInflater().inflate(R.menu.menu_detail, popup.getMenu());
        if (contact != null) {
            popup.getMenu().findItem(R.id.share).setVisible(true);
            popup.getMenu().findItem(R.id.edit).setVisible(true);
            popup.getMenu().findItem(R.id.view).setVisible(true);
            popup.getMenu().findItem(R.id.add_to_contacts).setVisible(false);
        } else {
            popup.getMenu().findItem(R.id.share).setVisible(false);
            popup.getMenu().findItem(R.id.edit).setVisible(false);
            popup.getMenu().findItem(R.id.view).setVisible(false);
            popup.getMenu().findItem(R.id.add_to_contacts).setVisible(true);
        }

        popup.setOnMenuItemClickListener(item -> {

            switch (item.getItemId()) {
                case R.id.share: {
                    Intent i = new Intent(getActivity(), SelectContactActivity.class);
                    i.putExtra("share", "share");
                    if (conversation != null)
                        if (conversation.getType().equals("one-to-one"))
                            i.putExtra("hide_contact", user.getPhoneNo());
                    shareContact.launch(i);
                    break;
                }
                case R.id.edit:

                    Uri selectedContactUri = ContactsContract.Contacts.getLookupUri(Long.parseLong(contact.getContactId()), contact.getLookupKey());
                    recentContactToAdd = contact;

                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setDataAndType(selectedContactUri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    intent.putExtra("finishActivityOnSaveCompleted", true);
                    startActivity(intent);
                    isEdited = true;

                    break;
                case R.id.view:
                    Intent intents = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.getContactId());
                    intents.setData(uri);
                    getActivity().startActivity(intents);
                    break;
                case R.id.add_to_contacts:

                    recentContactToAdd = new LocalContacts();
                    recentContactToAdd.setName(user.getName());
                    recentContactToAdd.setNumber(user.getPhoneNo());

                    Intent i = new Intent(Intent.ACTION_INSERT);
                    i.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    i.putExtra(ContactsContract.Intents.Insert.NAME, user.getName());
                    i.putExtra(ContactsContract.Intents.Insert.PHONE, user.getPhoneNo());
                    i.putExtra("finishActivityOnSaveCompleted", true);
                    addContactToContacts.launch(i);
                    break;
            }
            return true;
        });

        popup.show();
    }

    @Override
    public void edit(View v) {
        Fragment fragment = new GroupEditFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("conversation", conversation);
        bundle.putString("type", "subject");
        fragment.setArguments(bundle);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
        transaction.add(R.id.fragment, fragment, "subject");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void openImage(boolean type) {
        if (type) {
            Fragment fragment = new MediaPreviewFragment();
            Bundle bundle = new Bundle();
            bundle.putString("uri", conversation.getImage());
            bundle.putSerializable("conversation", conversation);
            fragment.setArguments(bundle);
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
            ft.add(R.id.fragment, fragment, "media_preview");
            ft.addToBackStack(null);
            ft.commit();
        } else
            new DialogReviewSendMedia(getContext(), null, false).show(user.getPhoto(), user.getName(), "image");
    }

    @Override
    public void openInfo() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.info_theme)).create();
        alertDialog.setMessage("Only admins can edit this group's info");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.show();

        final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.END;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    @Override
    public void openDescription() {
        Fragment fragment = new GroupEditFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("conversation", conversation);
        bundle.putString("type", "description");
        fragment.setArguments(bundle);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment, fragment, "description");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void openMedia() {
        Fragment fragment = new MediaGalleryFragment();
        Bundle bundle = new Bundle();
        if (conversation == null) {
            conversation.setTitle(user.getName());
            conversation.setType("one-to-one");
        }
        bundle.putSerializable("conversation", conversation);
        fragment.setArguments(bundle);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right);
        transaction.add(R.id.fragment, fragment, "media_gallery");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void openSearch() {
        viewModel.getParticipantList().removeObservers(getViewLifecycleOwner());
        viewModel.getParticipantList().observe(getViewLifecycleOwner(), new Observer<List<Participant>>() {
            @Override
            public void onChanged(List<Participant> participants) {
                setParticipantRecyclerView();
                mBinding.participantsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                mBinding.participantsRecycler.setAdapter(adapter);
            }
        });

        mBinding.secondaryLayout.setVisibility(View.VISIBLE);
        mBinding.primaryLayout.setVisibility(View.GONE);

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
                    performSearch(mBinding.searchBar.getText().toString());
                } else {
                    mBinding.clear.setVisibility(View.GONE);
                    viewModel.fetchParticipants();
                }
//                    adapter.getFilter().filter(mBinding.searchBar.getText().toString());
            }
        });

        mBinding.searchBar.requestFocus();
        Utils.showKeyboard(getActivity());
    }

    @Override
    public void addParticipants() {
        addMemberToGroup.launch(new Intent(getActivity(), SelectContactActivity.class)
                .putExtra("conversation", conversation));
    }

    @Override
    public void message() {
        goBack();
    }

    @Override
    public void audioCall() {

    }

    @Override
    public void videoCall() {

    }

    @Override
    public void blockUser() {

    }

    @Override
    public void goToCommonGroup(Conversation conversation) {
        startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("conversation", conversation));
        getActivity().finish();
    }

    @Override
    public void displayParticipantPopup(Participant participant, View v) {
        findContactByNumber(participant, v);
    }

    @Override
    public void showShareDialog(int size) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Sharing contact");
        progressDialog.setMessage("Sharing contact to 1 of " + size + " users");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    @Override
    public void updateShareDialog(String message) {
        progressDialog.setMessage(message);
    }

    @Override
    public void hideShareDialog() {
        progressDialog.dismiss();
    }

    @Override
    public void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getContext());
            progressDialog.show();
            progressDialog.setContentView(R.layout.progress_dialog);
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        if (!progressDialog.isShowing())
            progressDialog.show();
    }

    @Override
    public void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void updateViews() {
        renderViews();
    }

    @Override
    public void updateMembersTitle(String message) {
        mBinding.totalParticipants.setText(message);
    }
}
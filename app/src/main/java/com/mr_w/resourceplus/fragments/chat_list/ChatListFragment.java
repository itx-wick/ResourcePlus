package com.mr_w.resourceplus.fragments.chat_list;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.activities.AudioCallActivity;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.adapter.ChatListAdapter;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.databinding.FragmentChatListBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.fragments.conversation_detail.ConversationDetailFragment;
import com.mr_w.resourceplus.fragments.main_fragment.MainFragment;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.MessageStatesRecord;
import com.mr_w.resourceplus.model.UnreadBadge;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.server_call.GenericServerCalls;
import com.mr_w.resourceplus.activities.VideoCallActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListFragment extends BaseFragment<FragmentChatListBinding, ChatListViewModel>
        implements ChatListAdapter.SendChatData,
        ChatListAdapter.RemoveItem,
        ChatListAdapter.ViewUser,
        ChatListAdapter.Selection,
        ChatListNavigator {
    private FragmentChatListBinding mBinding;
    private List<Conversation> list = new ArrayList<>();
    public ChatListAdapter adapter;
    public static ChatListFragment object;
    private RecyclerView mRecyclerView;
    private Conversation conversation;
    private BroadcastReceiver newMessageReceiver;
    private BroadcastReceiver typingReceiver;
    private int count = 0;
    private boolean isResumed = false;
    private Users me;

    private Database db;
    private RequestQueue requestQueue;
    private GenericServerCalls genericServerCalls;

    public ChatListFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public int getBindingVariable() {
        return BR.chat_list;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = getViewDataBinding();
        me = viewModel.getDataManager().getUserDetails();
        mRecyclerView = mBinding.recyclerView;
        object = this;
        db = Database.getInstance(getActivity());

        registerReceivers();

        mBinding.swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                deleteAll();
                viewModel.getChatList();
                mBinding.swipe.setEnabled(false);
            }
        });

    }

    @Override
    public void onResume() {
        isResumed = true;
        viewModel.getConversations().observe(getViewLifecycleOwner(), new Observer<List<Conversation>>() {
            @Override
            public void onChanged(List<Conversation> conversations) {
                setChatRecyclerView(conversations);
            }
        });
        count = 0;
        super.onResume();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(newMessageReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(typingReceiver);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (!hasAllPermissionsGranted(grantResults)) {
                requestAllPermission();
            } else {
                startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("conversation", conversation));
                closeKeyboard();
                isResumed = false;
            }
        }
    }

    @Override
    public void performClick(Conversation conversation) {
        this.conversation = conversation;
        if (checkAllPermission()) {
            requestAllPermission();
        } else {
            startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("conversation", conversation));
            closeKeyboard();
            isResumed = false;
        }
    }

    @Override
    public void performLongClick(Conversation conversation) {
        if (adapter.getSelections().size() > 0) {

            MainFragment.instance.getmBinding().selectionTopBar.setVisibility(View.VISIBLE);
            MainFragment.instance.getmBinding().topAppBar.setVisibility(View.GONE);

            MainFragment.instance.getmBinding().back.setOnClickListener(v -> {
                MainFragment.instance.getmBinding().selectionTopBar.setVisibility(View.GONE);
                MainFragment.instance.getmBinding().topAppBar.setVisibility(View.VISIBLE);

                MenuItem unselect = MainFragment.instance.getmBinding().selectionTopBar.getMenu().findItem(R.id.unselect_all);
                MenuItem select = MainFragment.instance.getmBinding().selectionTopBar.getMenu().findItem(R.id.select_all);

                select.setVisible(true);
                unselect.setVisible(false);

                List<Conversation> temp = new ArrayList<>(adapter.getSelections());
                for (Conversation index : temp) {
                    adapter.getSelections().remove(0);
                    adapter.notifyItemChanged(list.indexOf(index));
                }
                MainFragment.instance.getmBinding().back.setOnClickListener(null);
            });

            MainFragment.instance.getmBinding().selectionTopBar.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_delete) {
                    List<Conversation> temp = new ArrayList<>(adapter.getSelections());
                    for (Conversation index : temp) {
                        int pos = list.indexOf(index);
                        deleteConversation(list.get(pos).get_id());
                        list.remove(pos);
                        adapter.getSelections().remove(adapter.getSelections().indexOf(index));
                        adapter.notifyItemRemoved(pos);
                    }
                    MainFragment.instance.getmBinding().selectionTopBar.setVisibility(View.GONE);
                    MainFragment.instance.getmBinding().topAppBar.setVisibility(View.VISIBLE);

                    if (list.size() == 0) {
                        mBinding.lnNotFound.setVisibility(View.VISIBLE);
                        mBinding.recyclerView.setVisibility(View.GONE);
                    }
                } else if (id == R.id.select_all) {
                    adapter.setSelections(new ArrayList<>(list));
                    adapter.notifyDataSetChanged();

                    MenuItem unselect = MainFragment.instance.getmBinding().selectionTopBar.getMenu().findItem(R.id.unselect_all);
                    MenuItem select = MainFragment.instance.getmBinding().selectionTopBar.getMenu().findItem(R.id.select_all);

                    select.setVisible(false);
                    unselect.setVisible(true);
                } else if (id == R.id.unselect_all) {
                    adapter.setSelections(new ArrayList<>());
                    adapter.notifyDataSetChanged();

                    MainFragment.instance.getmBinding().selectionTopBar.setVisibility(View.GONE);
                    MainFragment.instance.getmBinding().topAppBar.setVisibility(View.VISIBLE);

                    MenuItem unselect = MainFragment.instance.getmBinding().selectionTopBar.getMenu().findItem(R.id.unselect_all);
                    MenuItem select = MainFragment.instance.getmBinding().selectionTopBar.getMenu().findItem(R.id.select_all);

                    select.setVisible(true);
                    unselect.setVisible(false);
                }
                return true;
            });

        } else {
            MainFragment.instance.getmBinding().selectionTopBar.setVisibility(View.GONE);
            MainFragment.instance.getmBinding().topAppBar.setVisibility(View.VISIBLE);

            MainFragment.instance.getmBinding().back.setOnClickListener(null);
        }


    }

    @Override
    public void onRemove() {
        if (adapter.getSelections().size() == 0) {
            MainFragment.instance.getmBinding().selectionTopBar.setVisibility(View.GONE);
            MainFragment.instance.getmBinding().topAppBar.setVisibility(View.VISIBLE);
        } else if (adapter.getSelections().size() == 1) {
            MainFragment.instance.getmBinding().totalSelections.setText("");
        }
    }

    @Override
    public void onSelect() {
        if (adapter.getSelections().size() > 1) {
            MainFragment.instance.getmBinding().totalSelections.setText(adapter.getSelections().size() + "   Selected");
        }
    }

    @Override
    public void updateView() {
    }

    @Override
    public void view(int pos) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_view_user, null);
        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        Conversation conversation = adapter.getList().get(pos);
        ImageView chat, call, video, info, profile;
        TextView username;

        chat = dialogView.findViewById(R.id.btn_chat);
        call = dialogView.findViewById(R.id.btn_call);
        video = dialogView.findViewById(R.id.btn_video);
        info = dialogView.findViewById(R.id.btn_info);
        profile = dialogView.findViewById(R.id.image_profile);
        username = dialogView.findViewById(R.id.tv_username);

        chat.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ChatActivity.class).putExtra("conversation", conversation));
            alertDialog.dismiss();

        });

        call.setOnClickListener(v -> {
            for (Users users : conversation.getMembers()) {
                if (!users.getPhoneId().equals(me.getPhoneId())) {
                    startActivity(new Intent(getContext(), AudioCallActivity.class)
                            .putExtra("to", users));
                    break;
                }
            }
        });

        video.setOnClickListener(v -> {
            for (Users users : conversation.getMembers()) {
                if (!users.getPhoneId().equals(me.getPhoneId())) {
                    startActivity(new Intent(getContext(), VideoCallActivity.class)
                            .putExtra("to", users)
                            .putExtra("mode", "video"));
                    break;
                }
            }
        });

        info.setOnClickListener(v -> {
            Fragment fragment = new ConversationDetailFragment();
            Bundle bundle = new Bundle();
            if (conversation.getType().equals("one-to-one")) {
                for (Users users : conversation.getMembers()) {
                    if (!users.getPhoneId().equals(me.getPhoneId())) {
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
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.fragmentBox, fragment, "conversation_detail");
            transaction.addToBackStack(null);
            transaction.commit();

            alertDialog.dismiss();
        });

        if (conversation.getType().equals("one-to-one")) {
            for (Users users : conversation.getMembers()) {
                if (!users.getPhoneId().equals(me.getPhoneId())) {
                    if (users.getPhoto() != null && !users.getPhoto().equals("") && !users.getPhoto().equals("null")) {
                        Glide.with(getContext()).load(users.getPhoto()).into(profile);

                        profile.setOnClickListener(v -> {
                            new DialogReviewSendMedia(getContext(), null, false).show(users.getPhoto(), users.getName(), "image");
                        });

                    }
                    break;
                }
            }
        } else {
            if (conversation.getImage() != null && !conversation.getImage().equals("") && !conversation.getImage().equals("null")) {
                Glide.with(getContext()).load(conversation.getImage()).into(profile);

                profile.setOnClickListener(v -> {
                    new DialogReviewSendMedia(getContext(), null, false).show(conversation.getImage(), conversation.getTitle(), "image");
                });
            }
        }
        username.setText(conversation.getTitle());

        alertDialog.show();

    }

    private void registerReceivers() {

        BroadcastReceiver sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                viewModel.updateList();
            }
        };
        registerReceiver("message_sent", sentReceiver);

        BroadcastReceiver contactsSyncReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data");
                if (data != null) {
                    viewModel.updateList();
                }
            }
        };
        registerReceiver("contacts_sync", contactsSyncReceiver);

        typingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                di data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        boolean isPresent = false;
                        JSONObject obj = new JSONObject(data);
                        Users typer = new Gson().fromJson(obj.getString("typer"), Users.class);
                        String id = obj.getString("conversationId");
                        String phoneId = me.getPhoneId();
                        List<Users> usersList = new Gson().fromJson(obj.getString("to"), new TypeToken<List<Users>>() {
                        }.getType());
                        for (int i = 0; i < usersList.size(); i++) {
                            if (usersList.get(i).getPhoneId().equals(phoneId)) {
                                isPresent = true;
                                break;
                            }
                        }
                        for (int i = 0; i < list.size(); i++) {
                            if (isPresent && !phoneId.equals(typer.getPhoneId()) && list.get(i).get_id().equals(id)) {
                                int length = Integer.parseInt(obj.getString("length"));
                                if (length > 0) {
                                    if (db.getContact(typer.getPhoneNo()) != null)
                                        adapter.setTyping(db.getContact(typer.getPhoneNo()).getName() + " is Typing...");
                                    else
                                        adapter.setTyping(typer.getPhoneNo() + " is Typing...");
                                } else {
                                    adapter.setTyping(null);
                                }
                                adapter.notifyItemChanged(i);
//                                checkUnreadCount(id, i, "upd");
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        registerReceiver("typing", typingReceiver);

        newMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);

                        if (obj.has("cancel")) {
                            String phoneId = obj.getString("to");
                            String cancel = obj.getString("cancel");
                            if (me.getPhoneId().equals(phoneId)) {
                                if (cancel.equals("pre")) {
                                    if (mBinding.callLayout.getVisibility() == View.VISIBLE)
                                        mBinding.callLayout.setVisibility(View.GONE);
                                }
                            }
                        } else if (obj.has("call")) {

                            if (count > 0 && isResumed)
                                return;

                            Users caller = new Gson().fromJson(obj.getString("caller"), Users.class);
                            String roomId = obj.getString("room_id");
                            count = 0;

                            if (obj.getString("to").equals(me.getPhoneId())) {
                                count++;
                                if (obj.getString("mode").equals("video")) {
                                    startActivity(new Intent(getActivity(), VideoCallActivity.class)
                                            .putExtra("caller_id", roomId)
                                            .putExtra("to", caller)
                                            .putExtra("incoming", true));
                                } else {
                                    startActivity(new Intent(getActivity(), AudioCallActivity.class)
                                            .putExtra("caller_id", roomId)
                                            .putExtra("to", caller)
                                            .putExtra("incoming", true));
                                }
                            }
                        } else if (obj.has("ids")) {
                            Conversation temp = new Gson().fromJson(obj.getString("conversation"), Conversation.class);
                            List<String> ids = new Gson().fromJson(obj.getString("ids"), new TypeToken<List<String>>() {
                            }.getType());
                            if (ids.contains(me.getPhoneId())) {
                                count = 0;
                                for (Conversation item : list) {
                                    if (item.getMessage().getId().equals(temp.getMessage().getId())) {
                                        count++;
                                        break;
                                    }
                                }
                                if (count == 0) {
                                    saveToLocalDB(temp.getMessage());
                                    list.add(temp);
                                    Collections.sort(list, Conversation.orderedList);
                                    adapter.notifyDataSetChanged();
                                    saveToLocalDB(temp);
                                }
                            }
                        } else if (obj.has("toUsers")) {
                            String phoneId = obj.getString("phoneId");
                            List<String> communicatedIds = new Gson().fromJson(obj.getString("toUsers"), new TypeToken<List<String>>() {
                            }.getType());
                            if (communicatedIds.contains(me.getPhoneId())) {
                                if (obj.has("name")) {
                                    String name = obj.getString("name");
                                    for (int i = 0; i < list.size(); i++) {
                                        for (int j = 0; j < list.get(i).getMembers().size(); j++) {
                                            if (list.get(i).getMembers().get(j).getPhoneId().equals(phoneId)) {
                                                list.get(i).getMembers().get(j).setName(name);
                                                adapter.notifyItemChanged(i);
                                                updateConversation(list.get(i), phoneId, list.get(i).getMembers().get(j));
                                            }
                                        }
                                    }
                                } else if (obj.has("about")) {
                                    String about = obj.getString("about");
                                    for (int i = 0; i < list.size(); i++) {
                                        for (int j = 0; j < list.get(i).getMembers().size(); j++) {
                                            if (list.get(i).getMembers().get(j).getPhoneId().equals(phoneId)) {
                                                list.get(i).getMembers().get(j).setAbout(about);
                                                adapter.notifyItemChanged(i);
                                                updateConversation(list.get(i), phoneId, list.get(i).getMembers().get(j));
                                            }
                                        }
                                    }
                                } else if (obj.has("image")) {
                                    String image = obj.getString("image");
                                    for (int i = 0; i < list.size(); i++) {
                                        for (int j = 0; j < list.get(i).getMembers().size(); j++) {
                                            if (list.get(i).getMembers().get(j).getPhoneId().equals(phoneId)) {
                                                list.get(i).getMembers().get(j).setPhoto(image);
                                                adapter.notifyItemChanged(i);
                                                updateConversation(list.get(i), phoneId, list.get(i).getMembers().get(j));
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (obj.has("type2")) {
                            Conversation temp = new Gson().fromJson(obj.getString("conversation"), Conversation.class);
                            boolean isPresent = false;
                            for (Users users : temp.getMembers()) {
                                if (users.getPhoneId().equals(me.getPhoneId())) {
                                    isPresent = true;
                                    break;
                                }
                            }
                            if (!temp.getMessage().getSenderPhoneId().equals(me.getPhoneId()) &&
                                    isPresent) {
                                count = 0;
                                for (Conversation item : list) {
                                    if (item.getMessage().getId().equals(temp.getMessage().getId())) {
                                        count++;
                                        break;
                                    }
                                }
                                if (count == 0) {
                                    saveToLocalDB(temp.getMessage());
                                    saveToLocalDB(temp);
                                    list.add(temp);
                                    Collections.sort(list, Conversation.orderedList);
//                                    adapter.notifyDataSetChanged();
//                                    int index = list.indexOf(temp);
//                                    checkUnreadCount(temp.get_id(), index, "ins");
                                    checkUnreadCount();

                                    if (isResumed) {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("messageId", temp.getMessage().getId());
                                        jsonObject.put("to", temp.getMessage().getSenderPhoneId());
                                        jsonObject.put("from", me.getPhoneId());
                                        jsonObject.put("conversationId", temp.get_id());

                                        if (!ResourcePlusApplication.mSocket.connected())
                                            ResourcePlusApplication.mSocket.connect();
                                        ResourcePlusApplication.mSocket.emit("delivered", jsonObject);
                                    }
//                                    else
//                                        ResourcePlusApplication.mSocket.emit("seen", jsonObject);

//                                    setChatRecyclerView();
                                }
                            }
                        } else if (!obj.has("answer")) {

                            boolean isPresent = false;
                            String senderPhoneId = obj.getString("sender");
                            Conversation temp = new Gson().fromJson(obj.getString("conversation"), Conversation.class);
                            String phoneId = me.getPhoneId();
                            List<Users> usersList = temp.getMembers();
                            for (int i = 0; i < usersList.size(); i++) {
                                if (usersList.get(i).getPhoneId().equals(phoneId) && !temp.getRemovedMembers().contains(phoneId)) {
                                    isPresent = true;
                                    break;
                                }
                            }
                            boolean isConversationFound = false;
                            for (int i = 0; i < list.size(); i++) {
                                if (isPresent && !phoneId.equals(senderPhoneId) && list.get(i).get_id().equals(temp.get_id())) {

                                    isConversationFound = true;
                                    MessageData message = new MessageData();
                                    message.setId(obj.has("_id") ? obj.getString("_id") : "");
                                    message.setType(MessageData.getType(obj.getString("type")));
                                    message.setStatus(obj.has("status") ? obj.getString("status") : "");
                                    message.setText(obj.has("text") ? obj.getString("text") : "");
                                    message.setUrl(obj.has("url") ? obj.getString("url") : "");
                                    message.setFileSize(obj.has("fileSize") ? obj.getString("fileSize") : "");
                                    message.setSenderPhoneId(obj.has("sender") ? obj.getString("sender") : "");
                                    message.setConversationId(temp.get_id());
                                    message.setDateTime(obj.has("dateTime") ? obj.getString("dateTime") : "");

                                    saveToLocalDB(message);
                                    conversation = temp;
                                    conversation.setMessage(message);
                                    updateConversation(conversation);

                                    list.set(i, conversation);
                                    Collections.sort(list, Conversation.orderedList);
//                                    adapter.notifyDataSetChanged();
//                                    int index = list.indexOf(conversation);
//                                    checkUnreadCount(conversation.get_id(), index, "upd");
                                    checkUnreadCount();

                                    if (isResumed) {
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.put("messageId", message.getId());
                                        jsonObject.put("to", message.getSenderPhoneId());
                                        jsonObject.put("from", me.getPhoneId());
                                        jsonObject.put("conversationId", conversation.get_id());

                                        if (!ResourcePlusApplication.mSocket.connected())
                                            ResourcePlusApplication.mSocket.connect();
                                        ResourcePlusApplication.mSocket.emit("delivered", jsonObject);
                                    }

                                    break;

                                }
                            }
                            if (!isConversationFound && isPresent && !phoneId.equals(senderPhoneId)) {

                                MessageData message = new MessageData();
                                message.setId(obj.has("_id") ? obj.getString("_id") : "");
                                message.setType(MessageData.getType(obj.getString("type")));
                                message.setStatus(obj.has("status") ? obj.getString("status") : "");
                                message.setText(obj.has("text") ? obj.getString("text") : "");
                                message.setUrl(obj.has("url") ? obj.getString("url") : "");
                                message.setFileSize(obj.has("fileSize") ? obj.getString("fileSize") : "");
                                message.setSenderPhoneId(obj.has("sender") ? obj.getString("sender") : "");
                                message.setConversationId(temp.get_id());
                                message.setDateTime(obj.has("dateTime") ? obj.getString("dateTime") : "");

                                conversation = temp;
                                conversation.setMessage(message);
                                saveToLocalDB(message);
                                saveToLocalDB(conversation);

                                list.add(conversation);
                                Collections.sort(list, Conversation.orderedList);
//                                adapter.notifyDataSetChanged();
//                                int index = list.indexOf(conversation);
//                                checkUnreadCount(conversation.get_id(), index, "ins");
                                checkUnreadCount();

                                if (isResumed) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("messageId", message.getId());
                                    jsonObject.put("to", message.getSenderPhoneId());
                                    jsonObject.put("from", me.getPhoneId());
                                    jsonObject.put("conversationId", conversation.get_id());

                                    if (!ResourcePlusApplication.mSocket.connected())
                                        ResourcePlusApplication.mSocket.connect();
                                    ResourcePlusApplication.mSocket.emit("delivered", jsonObject);
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

        BroadcastReceiver seenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (!isResumed)
                    return;
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);
                        String id = obj.getString("to");
                        String from = obj.getString("from");
                        String conversationId = obj.getString("conversationId");
                        if (me.getPhoneId().equals(id)) {
                            getMessage(from, conversationId, "seen");
                        }
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }

                }
            }
        };
        registerReceiver("seen", seenReceiver);

        BroadcastReceiver deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (!isResumed)
                    return;
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);
                        String id = obj.getString("to");
                        String from = obj.getString("from");
                        String conversationId = obj.getString("conversationId");
                        if (me.getPhoneId().equals(id)) {
                            getMessage(from, conversationId, "delivered");
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
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(broadcastReceiver, filter);
    }

    private void setChatRecyclerView(List<Conversation> list) {
        if (list.size() == 0) {
            mBinding.lnNotFound.setVisibility(View.VISIBLE);
            mBinding.recyclerView.setVisibility(View.GONE);
        } else {
            mBinding.lnNotFound.setVisibility(View.GONE);
            mBinding.recyclerView.setVisibility(View.VISIBLE);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        Collections.sort(list, Conversation.orderedList);
        adapter = new ChatListAdapter(getActivity(), list);
        adapter.setUserPrefs(viewModel.getDataManager());
        adapter.setPhoneId(me.getPhoneId());
        adapter.setSelection(this);
        adapter.setViewUser(this);
        adapter.setSendChatData(this);
        adapter.setRemoveItem(this);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setAdapter(adapter);

        if (list.size() > 0)
            checkUnreadCount();

    }

    private boolean checkAllPermission() {
        int camera_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        int write_external_storage_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);
        return camera_result == PackageManager.PERMISSION_DENIED ||
                write_external_storage_result == PackageManager.PERMISSION_DENIED ||
                record_audio_result == PackageManager.PERMISSION_DENIED;
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private void requestAllPermission() {
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_CONTACTS
        }, 111);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();

        if (view == null) {
            view = new View(getActivity());
        }

        InputMethodManager manager
                = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveToLocalDB(Conversation list) {
        if (!db.isConversationExists(list.get_id()))
            db.insertConversation(list);
        else
            db.updateConversation(list);
    }

    private void saveToLocalDB(MessageData list) {
        if (!db.isMessageExists(list.getId()))
            db.insertMessage(list);
        else
            db.updateMessage(list);
    }

    private void updateConversation(Conversation conv, String phoneId, Users member) {
        saveToLocalDB(conv);
        Users user = db.getUser(phoneId);
        user.setName(member.getName());
        user.setAbout(member.getAbout());
        user.setPhoto(member.getPhoto());
        db.updateUser(user);
    }

    private void updateConversation(Conversation conv) {
        saveToLocalDB(conv);
    }

    private void checkUnreadCount() {
        if (isResumed) {
            adapter.getUnReads().clear();
            for (Conversation con : list) {
                int unreadCount = 0;
                unreadCount = db.getUnreadCount(con.get_id(), me.getPhoneId());
                if (unreadCount > 0) {
                    int i = list.indexOf(con);
                    adapter.getUnReads().add(new UnreadBadge(i, unreadCount));
                }
            }
            adapter.notifyDataSetChanged();
        }
    }

    private void getMessage(String phoneId, String conversationId, String status) {
        Conversation conversation = db.getConversation(conversationId);
        if (conversation != null) {
            for (Conversation conv : list) {
                if (conv.get_id().equals(conversation.get_id())) {
                    if (conversation.getType().equals("group")) {
                        List<MessageStatesRecord> records = conversation.getMessage().getStatesRecords();
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
                            conversation.getMessage().setStatus("seen");
                        } else
                            conversation.getMessage().setStatus("delivered");
                    } else
                        conversation.getMessage().setStatus(status);
                    saveToLocalDB(conversation.getMessage());
                    int index = list.indexOf(conv);
                    list.set(index, conversation);
                    Collections.sort(list, Conversation.orderedList);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }
            conversation.getMessage().setStatus(status);
            saveToLocalDB(conversation.getMessage());
            list.add(conversation);
            Collections.sort(list, Conversation.orderedList);
            adapter.notifyDataSetChanged();
        }
    }

    private void deleteAll() {
        db.deleteAllConversations();
    }

    private void deleteConversation(String id) {
        db.deleteConversation(id);
    }

    @Override
    public void checkRefreshing() {
        if (mBinding.swipe.isRefreshing()) {
            mBinding.swipe.setRefreshing(false);
            mBinding.swipe.setEnabled(true);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void hideRecycler() {
        mBinding.lnNotFound.setVisibility(View.VISIBLE);
        mBinding.recyclerView.setVisibility(View.GONE);
    }
}
package com.mr_w.resourceplus.fragments.search_participants;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.adapter.ParticipantsAdapter;
import com.mr_w.resourceplus.databinding.FragmentSearchParticipantBinding;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.Participant;
import com.mr_w.resourceplus.model.users.Users;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SearchParticipantFragment
        extends
        BaseFragment<FragmentSearchParticipantBinding, SearchParticipantsViewModel>
        implements
        ParticipantsAdapter.ClickListener,
        SearchParticipantsNavigator {

    private Conversation conversation;
    private FragmentSearchParticipantBinding mBinding;
    private ParticipantsAdapter adapter;

    @Override
    public int getBindingVariable() {
        return BR.search_participants;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_search_participant;
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = getViewDataBinding();
        conversation = (Conversation) getArguments().getSerializable("conversation");

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
                } else
                    mBinding.clear.setVisibility(View.GONE);
                adapter.getFilter().filter(mBinding.searchBar.getText().toString());
            }
        });

        mBinding.clear.setOnClickListener(v -> {
            mBinding.clear.setVisibility(View.GONE);
            mBinding.searchBar.setText("");
        });

        mBinding.btnBack.setOnClickListener(v -> {
            closeKeyboard();
            getActivity().onBackPressed();
        });

        mBinding.searchBar.requestFocus();

        viewModel.getParticipants().observe(getViewLifecycleOwner(), new Observer<List<Participant>>() {
            @Override
            public void onChanged(List<Participant> participants) {
                adapter = new ParticipantsAdapter(getActivity(), participants, SearchParticipantFragment.this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
                mBinding.participants.setLayoutManager(layoutManager);
                mBinding.participants.setItemAnimator(new DefaultItemAnimator());
                mBinding.participants.setAdapter(adapter);
            }
        });

        setParticipants();
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

    private void setParticipants() {

        List<Participant> participants = new ArrayList<>();
        for (Users user : conversation.getMembers()) {
            Participant participant = new Participant();
            participant.setUser(user);

            if (conversation.getAdmin().contains(user.getPhoneId())) {
                participant.setAdmin(true);
            }

            participants.add(participant);
        }
        Collections.sort(participants, Participant.adminOnTop);
        viewModel.addAllToList(participants);

    }

    @Override
    public void onClick(int pos, Participant participant, View v) {
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }
}
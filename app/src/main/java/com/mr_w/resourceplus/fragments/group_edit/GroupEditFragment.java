package com.mr_w.resourceplus.fragments.group_edit;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.databinding.FragmentGroupEditBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.supernova_emoji.actions.EmojIconActions;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class GroupEditFragment extends BaseFragment<FragmentGroupEditBinding, GroupEditViewModel> implements GroupEditNavigator {

    private String type;
    private Conversation conversation;
    FragmentGroupEditBinding mBinding;
    Bundle bundle;
    private EmojIconActions emoticon;
    private UserPreferences preferences;

    public GroupEditFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeEmoticon();
    }

    private void initializeEmoticon() {
        emoticon = new EmojIconActions(getActivity(), mBinding.getRoot(), mBinding.subject, mBinding.emoji);
        emoticon.setUseSystemEmoji(true);
        mBinding.subject.setUseSystemDefault(true);
        emoticon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
    }

    @Override
    public int getBindingVariable() {
        return BR.group_edit;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_group_edit;
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
        preferences = UserPreferences.getInstance(getActivity(), "UserPrefs");
        bundle = getArguments();
        type = bundle.getString("type");
        conversation = (Conversation) bundle.getSerializable("conversation");

        mBinding.subject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBinding.count.setText("" + editable.length());
            }
        });

        mBinding.subject.setText(conversation.getTitle());
        if (type.equals("description")) {
            mBinding.header.setText("Enter new group description");
            mBinding.subject.setText(conversation.getDescription());
            mBinding.subject.setHint("Description...");
            mBinding.description.setVisibility(View.VISIBLE);
        }

        mBinding.cancel.setOnClickListener(v -> {
            if (emoticon.getPopup().isKeyBoardOpen())
                Utils.hideKeyboard(getActivity());
            getActivity().onBackPressed();
        });

        mBinding.done.setOnClickListener(v -> {

            viewModel.setConversation(conversation);
            viewModel.setPhoneNumber(preferences.getUserDetails().getPhoneNo());
            viewModel.setActivity(new WeakReference<>((ChatActivity) getActivity()));
            viewModel.setType(type);
            if (type.equals("subject")) {
                if (mBinding.subject.length() == 0 || mBinding.subject.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "Title can't be empty", Toast.LENGTH_SHORT).show();
                    goBack();
//                    Utils.hideKeyboard(getActivity());
                } else {

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("title", mBinding.subject.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    conversation.setTitle(mBinding.subject.getText().toString());
                    viewModel.updateGroup(jsonObject);
                }
            } else {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("description", mBinding.subject.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                conversation.setDescription(mBinding.subject.getText().toString());
                viewModel.updateGroup(jsonObject);
            }
        });
    }

    @Override
    public void checkKeyboard() {
        if (emoticon.getPopup().isKeyBoardOpen())
            Utils.hideKeyboard(getActivity());
    }

    @Override
    public void goBack() {
        getActivity().onBackPressed();
    }
}
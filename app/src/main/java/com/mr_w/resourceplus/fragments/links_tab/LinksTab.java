package com.mr_w.resourceplus.fragments.links_tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.androidnetworking.error.ANError;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.adapter.LinkAdapter;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.databinding.FragmentLinksTabBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinksTab extends BaseFragment<FragmentLinksTabBinding, LinksTabViewModel> implements LinksTabNavigator {

    FragmentLinksTabBinding mBinding;
    private Conversation conversation;

    public LinksTab() {
    }

    @Override
    public int getBindingVariable() {
        return BR.links_tab;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_links_tab;
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
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.setConversationId(conversation.get_id());
        viewModel.getLinks().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> list) {
                setMediaRecyclerView(list);
            }
        });
    }

    private void setMediaRecyclerView(List<String> linkList) {

        if (linkList.size() == 0) {
            mBinding.links.setVisibility(View.GONE);
            mBinding.noLink.setVisibility(View.VISIBLE);
        } else {
            mBinding.links.setVisibility(View.VISIBLE);
            mBinding.noLink.setVisibility(View.GONE);
        }

        LinkAdapter adapter = new LinkAdapter(getContext(), linkList);
        Collections.reverse(linkList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBinding.links.setLayoutManager(layoutManager);
        mBinding.links.setItemAnimator(new DefaultItemAnimator());
        mBinding.links.setAdapter(adapter);
        mBinding.links.scrollToPosition(adapter.getLinkList().size() - 1);

    }
}
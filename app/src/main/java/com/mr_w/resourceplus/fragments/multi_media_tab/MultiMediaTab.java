package com.mr_w.resourceplus.fragments.multi_media_tab;

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
import androidx.recyclerview.widget.GridLayoutManager;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.adapter.MultiMediaAdapter;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.databinding.FragmentMultiMediaTabBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiMediaTab extends BaseFragment<FragmentMultiMediaTabBinding, MultiMediaTabViewModel> implements MultiMediaTabNavigator {

    private FragmentMultiMediaTabBinding mBinding;
    private MultiMediaAdapter adapter;
    private Conversation conversation;

    public MultiMediaTab() {
    }

    @Override
    public int getBindingVariable() {
        return BR.multi_media_tab;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_multi_media_tab;
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
        viewModel.getMultiMedia().observe(this, new Observer<List<Media>>() {
            @Override
            public void onChanged(List<Media> media) {
                setMediaRecyclerView(media);
            }
        });
    }

    private void setMediaRecyclerView(List<Media> mediaList) {

        if (mediaList.size() == 0) {
            mBinding.multiMedia.setVisibility(View.GONE);
            mBinding.noMedia.setVisibility(View.VISIBLE);
        } else {
            mBinding.multiMedia.setVisibility(View.VISIBLE);
            mBinding.noMedia.setVisibility(View.GONE);
        }

        adapter = new MultiMediaAdapter(getContext(), mediaList);
        Collections.reverse(mediaList);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        mBinding.multiMedia.setLayoutManager(layoutManager);
        mBinding.multiMedia.setItemAnimator(new DefaultItemAnimator());
        mBinding.multiMedia.setAdapter(adapter);
        mBinding.multiMedia.scrollToPosition(adapter.getMediaList().size() - 1);

    }
}
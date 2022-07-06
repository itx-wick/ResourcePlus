package com.mr_w.resourceplus.fragments.document_tab;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.adapter.DocumentAdapter;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.databinding.FragmentDocumentTabBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.Media;

import java.util.Collections;
import java.util.List;

public class DocumentTab extends BaseFragment<FragmentDocumentTabBinding, DocumentTabViewModel> implements DocumentTabNavigator {

    FragmentDocumentTabBinding mBinding;
    private Conversation conversation;

    public DocumentTab() {
    }

    @Override
    public int getBindingVariable() {
        return BR.document_tab;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_document_tab;
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
        viewModel.getDocuments().observe(this, new Observer<List<Media>>() {
            @Override
            public void onChanged(List<Media> media) {
                setMediaRecyclerView(media);
            }
        });
    }

    private void setMediaRecyclerView(List<Media> mediaList) {

        if (mediaList.size() == 0) {
            mBinding.documents.setVisibility(View.GONE);
            mBinding.noDocument.setVisibility(View.VISIBLE);
        } else {
            mBinding.documents.setVisibility(View.VISIBLE);
            mBinding.noDocument.setVisibility(View.GONE);
        }

        DocumentAdapter adapter = new DocumentAdapter(getContext(), mediaList);
        Collections.reverse(mediaList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mBinding.documents.setLayoutManager(layoutManager);
        mBinding.documents.setItemAnimator(new DefaultItemAnimator());
        mBinding.documents.setAdapter(adapter);
        mBinding.documents.scrollToPosition(adapter.getMediaList().size() - 1);

    }
}
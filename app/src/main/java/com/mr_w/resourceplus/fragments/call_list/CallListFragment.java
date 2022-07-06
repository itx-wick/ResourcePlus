package com.mr_w.resourceplus.fragments.call_list;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.adapter.CallListAdapter;
import com.mr_w.resourceplus.databinding.FragmentCallListBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.CallLogs;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.DividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallListFragment extends BaseFragment<FragmentCallListBinding, CallListViewModel> implements CallListNavigator {
    private FragmentCallListBinding mBinding;
    private List<CallLogs> callLogs = new ArrayList<>();
    public CallListAdapter mAdapter;
    private RecyclerView mRecyclerView;
    public static CallListFragment callListObject;

    @Override
    public int getBindingVariable() {
        return BR.call_list;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_call_list;
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
        callLogs = new ArrayList<>();
        mRecyclerView = mBinding.contactsRecyclerView;
        callListObject = this;
        getCallList();
    }

    private void getCallList() {
        callLogs.add(new CallLogs("1",
                viewModel.getDataManager().getUserDetails(),
                viewModel.getDataManager().getUserDetails(),
                new SimpleDateFormat("dd/MM/yyyy H:mm:ss").format(new Date()),
                false,
                "audio",
                15000));
        callLogs.add(new CallLogs("2",
                viewModel.getDataManager().getUserDetails(),
                viewModel.getDataManager().getUserDetails(),
                new SimpleDateFormat("dd/MM/yyyy H:mm:ss").format(new Date()),
                false,
                "audio",
                5005));
        callLogs.add(new CallLogs("3",
                viewModel.getDataManager().getUserDetails(),
                viewModel.getDataManager().getUserDetails(),
                new SimpleDateFormat("dd/MM/yyyy H:mm:ss").format(new Date()),
                true,
                "audio",
                30000));
        setCallRecyclerView(callLogs);
    }

    private void setCallRecyclerView(List<CallLogs> list) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new CallListAdapter(getActivity(), list);
        mAdapter.setMe(viewModel.getDataManager().getUserDetails());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
    }
}
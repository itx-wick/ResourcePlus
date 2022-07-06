package com.mr_w.resourceplus.fragments.start_fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.databinding.FragmentStartBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.storage.UserPreferences;

public class StartFragment
        extends
        BaseFragment<FragmentStartBinding, StartViewModel>
        implements
        StartNavigator {

    private FragmentStartBinding mBinding;

    @Override
    public int getBindingVariable() {
        return BR.start_fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_start;
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
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel.getDataManager().getBoolean(UserPreferences.PREF_USER_IS_LOGIN)) {
            passToDashboard(mBinding.getRoot());
        }
    }

    @Override
    public void passToLogin(View v) {
        Navigation.findNavController(v).navigate(R.id.action_startFragment_to_phoneLoginFragment);
    }

    @Override
    public void passToDashboard(View v) {
        Navigation.findNavController(v).navigate(R.id.action_startFragment_to_mainFragment);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
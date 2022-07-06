package com.mr_w.resourceplus.fragments.otp_fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.databinding.FragmentOtpBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;

public class OtpFragment extends BaseFragment<FragmentOtpBinding, OtpViewModel> implements View.OnClickListener,
        OtpNavigator {

    private FragmentOtpBinding mBinding;
    ProgressDialog dialog;

    @Override
    public int getBindingVariable() {
        return BR.otp;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_otp;
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
        mBinding.btnSubmit2.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit2) {
            closeKeyboard();
            progressDialogShow();
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    Navigation.findNavController(v).navigate(R.id.action_verifyCodeFragment_to_userInfoFragment);
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void progressDialogShow() {
        dialog = new ProgressDialog(getActivity());
        dialog.show();
        dialog.setContentView(R.layout.progress_dialog);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager manager
                    = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }
}
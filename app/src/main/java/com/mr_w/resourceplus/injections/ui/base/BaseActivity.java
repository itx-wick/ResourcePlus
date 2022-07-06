package com.mr_w.resourceplus.injections.ui.base;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.di.component.DaggerActivityComponent;
import com.mr_w.resourceplus.injections.di.module.ActivityModule;
import com.mr_w.resourceplus.injections.utils.CommonUtils;
import com.mr_w.resourceplus.utils.Utils;

import java.util.Objects;

import javax.inject.Inject;

public abstract class BaseActivity<T extends ViewDataBinding, V extends BaseViewModel> extends AppCompatActivity
        implements BaseFragment.Callback {

    private ProgressDialog mProgressDialog;

    private T mViewDataBinding;

    @Inject
    protected V viewModel;

    public abstract int getBindingVariable();

    public abstract
    @LayoutRes
    int getLayoutId();


    @Override
    public void onFragmentAttached() {

    }

    @Override
    public void onFragmentDetached(String tag) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId());
        performDependencyInjection(getBuildComponent());
        super.onCreate(savedInstanceState);
    }

    public T getViewDataBinding() {
        return mViewDataBinding;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public void hideLoading() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    public boolean isNetworkConnected() {
        return Utils.checkInternet(getApplicationContext());
    }

    private ActivityComponent getBuildComponent() {
        return DaggerActivityComponent.builder()
                .appComponent(((ResourcePlusApplication) getApplication()).appComponent)
                .activityModule(new ActivityModule(this))
                .build();
    }

    public abstract void performDependencyInjection(ActivityComponent buildComponent);

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    public void showLoading() {
        hideLoading();
        mProgressDialog = CommonUtils.showLoadingDialog(this);
    }

    public void addVariableToBinding(int id, Object value) {
        mViewDataBinding.setVariable(id, value);
    }

    public void performDataBinding() {
        mViewDataBinding.setVariable(getBindingVariable(), viewModel);
        mViewDataBinding.executePendingBindings();
    }
}


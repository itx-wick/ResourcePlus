package com.mr_w.resourceplus.activities.profile_activity;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.databinding.ActivityProfileBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;

import java.lang.ref.WeakReference;

public class ProfileActivity extends BaseActivity<ActivityProfileBinding, ProfileViewModel>
        implements ProfileNavigator {

    @Override
    public int getBindingVariable() {
        return BR.profile;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addVariableToBinding(BR.imageUrl, viewModel.getImage());
        performDataBinding();
        getViewDataBinding();
        viewModel.setNavigator(this);
        viewModel.setActivity(new WeakReference<>(this));
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    public void openFabControls(DataManager dataManager) {
        if (!dataManager.getUserDetails().getPhoto().equals(""))
            new DialogReviewSendMedia(this, false, null, dataManager.getUserDetails().getPhoto()).loadImage(null);
        else
            new DialogReviewSendMedia(this, ContextCompat.getDrawable(this, R.drawable.no_pic)).show();
    }

    @Override
    public void goBack() {
        onBackPressed();
        overridePendingTransition(0, 0);
    }
}



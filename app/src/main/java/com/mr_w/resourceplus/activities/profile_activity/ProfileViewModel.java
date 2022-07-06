package com.mr_w.resourceplus.activities.profile_activity;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

import java.lang.ref.WeakReference;

public class ProfileViewModel extends BaseViewModel<ProfileNavigator> {

    private static final String TAG = "ProfileViewModel";
    private WeakReference<ProfileActivity> activity;
    private final String name;
    private final String phoneNumber;
    private final String about;
    private final String image;

    public ProfileViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);

        name = getDataManager().getUserDetails().getName();
        phoneNumber = getDataManager().getUserDetails().getPhoneNo();
        image = getDataManager().getUserDetails().getPhoto();
        about = !getDataManager().getUserDetails().getAbout().equals("") ? getDataManager().getUserDetails().getAbout() : "Hey There, I am using ResourcePlus!";
    }

    public void setActivity(WeakReference<ProfileActivity> activity) {
        this.activity = activity;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAbout() {
        return about;
    }

    public String getImage() {
        return image;
    }

    @BindingAdapter("profileImage")
    public static void loadImage(ImageView imageView, String image) {
        if (!image.equals("")) {
            Glide.with(imageView.getContext()).load(image).placeholder(R.drawable.no_pic).into(imageView);
        } else
            Glide.with(imageView.getContext()).load(R.drawable.no_pic).into(imageView);
    }

    public void clickFab(View v) {
        getNavigator().openFabControls(getDataManager());
    }

    public void onBack(View v) {
        getNavigator().goBack();
    }

}

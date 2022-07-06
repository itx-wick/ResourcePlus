package com.mr_w.resourceplus.injections.di.module;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;

import com.mr_w.resourceplus.activities.chat_activity.ChatViewModel;
import com.mr_w.resourceplus.activities.contacts_activity.ContactsViewModel;
import com.mr_w.resourceplus.activities.profile_activity.ProfileViewModel;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.activities.select_contact.SelectContactViewModel;
import com.mr_w.resourceplus.activities.settings_activity.SettingsViewModel;
import com.mr_w.resourceplus.activities.splash_activity.SplashViewModel;
import com.mr_w.resourceplus.activities.start_activity.StartViewModel;
import com.mr_w.resourceplus.fragments.camera_fragment.CameraViewModel;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.view_models.ViewModelProviderFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {
    private final BaseActivity<?, ?> activity;

    public ActivityModule(BaseActivity<?, ?> activity) {
        this.activity = activity;
    }

    @Provides
    SettingsViewModel provideSettingsModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<SettingsViewModel> supplier = () -> new SettingsViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<SettingsViewModel> factory = new ViewModelProviderFactory<>(SettingsViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(SettingsViewModel.class);
    }

    @Provides
    ContactsViewModel provideContactsModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<ContactsViewModel> supplier = () -> new ContactsViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<ContactsViewModel> factory = new ViewModelProviderFactory<>(ContactsViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ContactsViewModel.class);
    }

    @Provides
    ProfileViewModel provideProfileModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<ProfileViewModel> supplier = () -> new ProfileViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<ProfileViewModel> factory = new ViewModelProviderFactory<>(ProfileViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ProfileViewModel.class);
    }

    @Provides
    SelectContactViewModel provideSelectContactModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<SelectContactViewModel> supplier = () -> new SelectContactViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<SelectContactViewModel> factory = new ViewModelProviderFactory<>(SelectContactViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(SelectContactViewModel.class);
    }

    @Provides
    ChatViewModel provideChatModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<ChatViewModel> supplier = () -> new ChatViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<ChatViewModel> factory = new ViewModelProviderFactory<>(ChatViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(ChatViewModel.class);
    }

    @Provides
    SplashViewModel provideSplashModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<SplashViewModel> supplier = () -> new SplashViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<SplashViewModel> factory = new ViewModelProviderFactory<>(SplashViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(SplashViewModel.class);
    }

    @Provides
    StartViewModel provideStartModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<StartViewModel> supplier = () -> new StartViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<StartViewModel> factory = new ViewModelProviderFactory<>(StartViewModel.class, supplier);
        return new ViewModelProvider(activity, factory).get(StartViewModel.class);
    }

}

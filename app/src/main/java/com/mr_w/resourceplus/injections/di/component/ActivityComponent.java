package com.mr_w.resourceplus.injections.di.component;

import com.mr_w.resourceplus.activities.AudioCallActivity;
import com.mr_w.resourceplus.activities.call_info.CallInfoActivity;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.activities.contacts_activity.ContactActivity;
import com.mr_w.resourceplus.activities.document_pick.DocumentPickActivity;
import com.mr_w.resourceplus.activities.profile_activity.ProfileActivity;
import com.mr_w.resourceplus.activities.select_contact.SelectContactActivity;
import com.mr_w.resourceplus.activities.settings_activity.SettingsActivity;
import com.mr_w.resourceplus.activities.splash_activity.SplashActivity;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.injections.di.module.ActivityModule;
import com.mr_w.resourceplus.injections.di.scope.ActivityScope;
import com.mr_w.resourceplus.activities.VideoCallActivity;

import dagger.Component;

@ActivityScope
@Component(modules = ActivityModule.class, dependencies = AppComponent.class)
public interface ActivityComponent {

    void inject(StartActivity activity);

    void inject(SelectContactActivity activity);

    void inject(ContactActivity activity);

    void inject(SplashActivity splashActivity);

    void inject(AudioCallActivity audioCallActivity);

    void inject(VideoCallActivity videoCallActivity);

    void inject(ChatActivity chatActivity);

    void inject(ProfileActivity profileActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(DocumentPickActivity documentPickActivity);

    void inject(CallInfoActivity callInfoActivity);

}

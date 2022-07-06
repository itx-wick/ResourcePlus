package com.mr_w.resourceplus.injections.di.component;

import com.mr_w.resourceplus.fragments.camera_fragment.CameraFragment;
import com.mr_w.resourceplus.fragments.chat_list.ChatListFragment;
import com.mr_w.resourceplus.fragments.status_list.StatusListFragment;
import com.mr_w.resourceplus.fragments.call_list.CallListFragment;
import com.mr_w.resourceplus.fragments.phone_login.LoginFragment;
import com.mr_w.resourceplus.fragments.register_fragment.RegistrationFragment;
import com.mr_w.resourceplus.fragments.conversation_detail.ConversationDetailFragment;
import com.mr_w.resourceplus.fragments.create_group.CreateGroupFragment;
import com.mr_w.resourceplus.fragments.document_tab.DocumentTab;
import com.mr_w.resourceplus.fragments.gallery_picker.GalleryPickerFragment;
import com.mr_w.resourceplus.fragments.group_edit.GroupEditFragment;
import com.mr_w.resourceplus.fragments.links_tab.LinksTab;
import com.mr_w.resourceplus.fragments.main_fragment.MainFragment;
import com.mr_w.resourceplus.fragments.media_gallery.MediaGalleryFragment;
import com.mr_w.resourceplus.fragments.media_preview.MediaPreviewFragment;
import com.mr_w.resourceplus.fragments.multi_media_tab.MultiMediaTab;
import com.mr_w.resourceplus.fragments.search_participants.SearchParticipantFragment;
import com.mr_w.resourceplus.fragments.start_fragment.StartFragment;
import com.mr_w.resourceplus.fragments.otp_fragment.OtpFragment;
import com.mr_w.resourceplus.injections.di.module.FragmentModule;
import com.mr_w.resourceplus.injections.di.scope.FragmentScope;

import dagger.Component;

@FragmentScope
@Component(modules = FragmentModule.class, dependencies = AppComponent.class)
public interface FragmentComponent {
    void inject(ConversationDetailFragment fragment);

    void inject(CreateGroupFragment fragment);

    void inject(DocumentTab fragment);

    void inject(GalleryPickerFragment fragment);

    void inject(GroupEditFragment fragment);

    void inject(LinksTab fragment);

    void inject(MainFragment fragment);

    void inject(MediaGalleryFragment fragment);

    void inject(MediaPreviewFragment fragment);

    void inject(MultiMediaTab fragment);

    void inject(LoginFragment fragment);

    void inject(SearchParticipantFragment fragment);

    void inject(StartFragment fragment);

    void inject(RegistrationFragment fragment);

    void inject(OtpFragment fragment);

    void inject(CallListFragment fragment);

    void inject(ChatListFragment fragment);

    void inject(CameraFragment fragment);

    void inject(StatusListFragment fragment);
}

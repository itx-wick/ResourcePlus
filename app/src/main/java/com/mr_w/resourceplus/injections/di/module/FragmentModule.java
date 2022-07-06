package com.mr_w.resourceplus.injections.di.module;

import androidx.core.util.Supplier;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mr_w.resourceplus.fragments.call_list.CallListViewModel;
import com.mr_w.resourceplus.fragments.camera_fragment.CameraViewModel;
import com.mr_w.resourceplus.fragments.chat_list.ChatListViewModel;
import com.mr_w.resourceplus.fragments.conversation_detail.ConversationDetailViewModel;
import com.mr_w.resourceplus.fragments.create_group.CreateGroupViewModel;
import com.mr_w.resourceplus.fragments.document_tab.DocumentTabViewModel;
import com.mr_w.resourceplus.fragments.gallery_picker.GalleryPickerViewModel;
import com.mr_w.resourceplus.fragments.group_edit.GroupEditViewModel;
import com.mr_w.resourceplus.fragments.links_tab.LinksTabViewModel;
import com.mr_w.resourceplus.fragments.main_fragment.MainFragmentViewModel;
import com.mr_w.resourceplus.fragments.media_gallery.MediaGalleryViewModel;
import com.mr_w.resourceplus.fragments.media_preview.MediaPreviewViewModel;
import com.mr_w.resourceplus.fragments.multi_media_tab.MultiMediaTabViewModel;
import com.mr_w.resourceplus.fragments.otp_fragment.OtpViewModel;
import com.mr_w.resourceplus.fragments.phone_login.LoginViewModel;
import com.mr_w.resourceplus.fragments.register_fragment.RegisterViewModel;
import com.mr_w.resourceplus.fragments.search_participants.SearchParticipantsViewModel;
import com.mr_w.resourceplus.fragments.start_fragment.StartViewModel;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.view_models.ViewModelProviderFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class FragmentModule {

    private BaseFragment<?, ?> fragment;

    public FragmentModule(BaseFragment<?, ?> fragment) {
        this.fragment = fragment;
    }

    @Provides
    LinearLayoutManager provideLinearLayoutManager() {
        return new LinearLayoutManager(fragment.getActivity());
    }

    @Provides
    ConversationDetailViewModel provideCDViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<ConversationDetailViewModel> supplier = () -> new ConversationDetailViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<ConversationDetailViewModel> factory = new ViewModelProviderFactory<>(ConversationDetailViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(ConversationDetailViewModel.class);
    }

    @Provides
    CallListViewModel provideCLViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<CallListViewModel> supplier = () -> new CallListViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<CallListViewModel> factory = new ViewModelProviderFactory<>(CallListViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(CallListViewModel.class);
    }

    @Provides
    ChatListViewModel provideChatLViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<ChatListViewModel> supplier = () -> new ChatListViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<ChatListViewModel> factory = new ViewModelProviderFactory<>(ChatListViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(ChatListViewModel.class);
    }

    @Provides
    CreateGroupViewModel provideCGViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<CreateGroupViewModel> supplier = () -> new CreateGroupViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<CreateGroupViewModel> factory = new ViewModelProviderFactory<>(CreateGroupViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(CreateGroupViewModel.class);
    }

    @Provides
    DocumentTabViewModel provideDTViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<DocumentTabViewModel> supplier = () -> new DocumentTabViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<DocumentTabViewModel> factory = new ViewModelProviderFactory<>(DocumentTabViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(DocumentTabViewModel.class);
    }

    @Provides
    GalleryPickerViewModel provideGPViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<GalleryPickerViewModel> supplier = () -> new GalleryPickerViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<GalleryPickerViewModel> factory = new ViewModelProviderFactory<>(GalleryPickerViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(GalleryPickerViewModel.class);
    }

    @Provides
    GroupEditViewModel provideGEViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<GroupEditViewModel> supplier = () -> new GroupEditViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<GroupEditViewModel> factory = new ViewModelProviderFactory<>(GroupEditViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(GroupEditViewModel.class);
    }

    @Provides
    LinksTabViewModel provideLTViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<LinksTabViewModel> supplier = () -> new LinksTabViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<LinksTabViewModel> factory = new ViewModelProviderFactory<>(LinksTabViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(LinksTabViewModel.class);
    }

    @Provides
    MainFragmentViewModel provideMFViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<MainFragmentViewModel> supplier = () -> new MainFragmentViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<MainFragmentViewModel> factory = new ViewModelProviderFactory<>(MainFragmentViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MainFragmentViewModel.class);
    }

    @Provides
    MediaGalleryViewModel provideMGViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<MediaGalleryViewModel> supplier = () -> new MediaGalleryViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<MediaGalleryViewModel> factory = new ViewModelProviderFactory<>(MediaGalleryViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MediaGalleryViewModel.class);
    }

    @Provides
    MediaPreviewViewModel provideMPViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<MediaPreviewViewModel> supplier = () -> new MediaPreviewViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<MediaPreviewViewModel> factory = new ViewModelProviderFactory<>(MediaPreviewViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MediaPreviewViewModel.class);
    }

    @Provides
    MultiMediaTabViewModel provideMMTViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<MultiMediaTabViewModel> supplier = () -> new MultiMediaTabViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<MultiMediaTabViewModel> factory = new ViewModelProviderFactory<>(MultiMediaTabViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(MultiMediaTabViewModel.class);
    }

    @Provides
    OtpViewModel provideOtpViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<OtpViewModel> supplier = () -> new OtpViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<OtpViewModel> factory = new ViewModelProviderFactory<>(OtpViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(OtpViewModel.class);
    }

    @Provides
    LoginViewModel provideLoginViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<LoginViewModel> supplier = () -> new LoginViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<LoginViewModel> factory = new ViewModelProviderFactory<>(LoginViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(LoginViewModel.class);
    }

    @Provides
    RegisterViewModel provideRegisterViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<RegisterViewModel> supplier = () -> new RegisterViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<RegisterViewModel> factory = new ViewModelProviderFactory<>(RegisterViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(RegisterViewModel.class);
    }

    @Provides
    SearchParticipantsViewModel provideSearchViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<SearchParticipantsViewModel> supplier = () -> new SearchParticipantsViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<SearchParticipantsViewModel> factory = new ViewModelProviderFactory<>(SearchParticipantsViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(SearchParticipantsViewModel.class);
    }

    @Provides
    StartViewModel provideStartViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<StartViewModel> supplier = () -> new StartViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<StartViewModel> factory = new ViewModelProviderFactory<>(StartViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(StartViewModel.class);
    }

    @Provides
    CameraViewModel provideCameraModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
        Supplier<CameraViewModel> supplier = () -> new CameraViewModel(dataManager, schedulerProvider);
        ViewModelProviderFactory<CameraViewModel> factory = new ViewModelProviderFactory<>(CameraViewModel.class, supplier);
        return new ViewModelProvider(fragment, factory).get(CameraViewModel.class);
    }

}

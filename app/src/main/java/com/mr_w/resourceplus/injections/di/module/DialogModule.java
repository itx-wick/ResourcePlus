package com.mr_w.resourceplus.injections.di.module;

import com.mr_w.resourceplus.injections.ui.base.BaseDialog;

import dagger.Module;

@Module
public class DialogModule {

    private BaseDialog dialog;

    public DialogModule(BaseDialog dialog) {
        this.dialog = dialog;
    }

//    @Provides
//    RateUsViewModel provideRateUsViewModel(DataManager dataManager, SchedulerProvider schedulerProvider) {
//        Supplier<RateUsViewModel> supplier = () -> new RateUsViewModel(dataManager, schedulerProvider);
//        ViewModelProviderFactory<RateUsViewModel> factory = new ViewModelProviderFactory<>(RateUsViewModel.class, supplier);
//        return new ViewModelProvider(dialog.getActivity(), factory).get(RateUsViewModel.class);
//    }

}

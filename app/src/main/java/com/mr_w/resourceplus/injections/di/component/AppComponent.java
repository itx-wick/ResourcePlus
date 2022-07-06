package com.mr_w.resourceplus.injections.di.component;

import android.app.Application;

import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.di.module.AppModule;
import com.mr_w.resourceplus.injections.network.remote.AppApiHelper;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(ResourcePlusApplication app);

    DataManager getDataManager();

    SchedulerProvider getSchedulerProvider();

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }
}

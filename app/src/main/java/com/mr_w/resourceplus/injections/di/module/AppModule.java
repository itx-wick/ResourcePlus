package com.mr_w.resourceplus.injections.di.module;

import android.app.Application;
import android.content.Context;

import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.injections.data.AppDataManager;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.data.local.db.AppDbHelper;
import com.mr_w.resourceplus.injections.data.local.db.DbHelper;
import com.mr_w.resourceplus.injections.data.local.preferences.AppPreferencesHelper;
import com.mr_w.resourceplus.injections.data.local.preferences.PreferencesHelper;
import com.mr_w.resourceplus.injections.di.PreferenceInfo;
import com.mr_w.resourceplus.injections.network.remote.ApiHelper;
import com.mr_w.resourceplus.injections.network.remote.AppApiHelper;
import com.mr_w.resourceplus.injections.utils.rx.AppSchedulerProvider;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    @Singleton
    ApiHelper provideApiHelper(AppApiHelper appApiHelper) {
        return appApiHelper;
    }

    @Provides
    @Singleton
    Database provideAppDatabase(Context context) {
        return Database.getInstance(context);
    }

    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    @Provides
    @PreferenceInfo
    String providePreferenceName() {
        return AppPreferencesHelper.PREF_NAME;
    }

    @Provides
    @Singleton
    PreferencesHelper providePreferencesHelper(AppPreferencesHelper appPreferencesHelper) {
        return appPreferencesHelper;
    }

    @Provides
    @Singleton
    DataManager provideDataManager(AppDataManager appDataManager) {
        return appDataManager;
    }

    @Provides
    @Singleton
    DbHelper provideDbHelper(AppDbHelper appDbHelper) {
        return appDbHelper;
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

}

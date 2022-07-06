package com.mr_w.resourceplus.fragments.media_gallery;

import android.util.Log;

import com.google.gson.Gson;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.fragments.create_group.CreateGroupNavigator;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.ui.base.BaseViewModel;
import com.mr_w.resourceplus.injections.utils.rx.SchedulerProvider;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.users.Users;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MediaGalleryViewModel extends BaseViewModel<MediaGalleryNavigator> {

    private static final String TAG = "MediaGalleryViewModel";

    public MediaGalleryViewModel(DataManager dataManager,
                                 SchedulerProvider schedulerProvider) {
        super(dataManager, schedulerProvider);
    }
}

package com.mr_w.resourceplus.utils;

import android.content.Intent;

import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.services.SocketService;

public class ActiveActivitiesTracker {
    private static int sActiveActivities = 0;

    public static void activityStarted() {
        if (sActiveActivities == 0) {
            if (!SocketService.isRunning())
                ResourcePlusApplication.getContext()
                        .startService(new Intent(ResourcePlusApplication.getContext(), SocketService.class));
        }
        sActiveActivities++;
    }

    public static void activityStopped() {
        sActiveActivities--;
        if (sActiveActivities == 0) {
//            if (SocketService.isRunning())
//                ResourcePlusApplication.getContext()
//                        .stopService(new Intent(ResourcePlusApplication.getContext(), SocketService.class));
        }
    }
}

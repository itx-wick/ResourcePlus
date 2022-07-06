package com.mr_w.resourceplus.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RequestPermissionsHelper {
    public static final int requestPermissionCode = 212;
    public static final int notShouldShowRequestPermissionRationaleCode = 213;
    public static final int notCheckSelfPermissionCode = 214;

    public static String[] persmissionsStringArray = new String[]
            {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                    , Manifest.permission.READ_EXTERNAL_STORAGE
                    , Manifest.permission.CAMERA
                    , Manifest.permission.WRITE_CONTACTS
                    , Manifest.permission.RECORD_AUDIO
                    , Manifest.permission.READ_CONTACTS
            };


    public static Context mContext;
    public static Activity mActivity;

    public static int requestPermission(Activity activity) {
        mContext = activity;
        mActivity = activity;
        if (!RequestPermissionsHelper.checkSelfPermission(persmissionsStringArray)) {
            if (!RequestPermissionsHelper.shouldShowRequestPermissionRationale(persmissionsStringArray)) {
                return notShouldShowRequestPermissionRationaleCode; // show a dialog that permission denied
            } else {
                return requestPermissionCode; // request for permissions' grant
            }
        } else {
            return notCheckSelfPermissionCode; //if not self permission granted then initialize application
        }
    }

    private static boolean checkSelfPermission(String[] persmissionsStringArray) {
        final int grant = PackageManager.PERMISSION_GRANTED;
        for (String s : persmissionsStringArray) {
            if (ContextCompat.checkSelfPermission(mContext, s) != grant)
                return false;
        }
        return true;
    }

    private static boolean shouldShowRequestPermissionRationale(String[] persmissionsStringArray) {
        final int grant = PackageManager.PERMISSION_GRANTED;
        for (String s : persmissionsStringArray) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, s))
                return false;
        }
        return true;
    }
}
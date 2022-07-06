package com.mr_w.resourceplus.utils;

import androidx.fragment.app.FragmentActivity;

import com.mr_w.resourceplus.callbacks.FileLoaderCallbacks;
import com.mr_w.resourceplus.interfaces.FilterResultCallback;

import static com.mr_w.resourceplus.callbacks.FileLoaderCallbacks.TYPE_FILE;

public class FileFilter {

    public static void getFiles(FragmentActivity activity,
                                FilterResultCallback<DocumentFile> callback, String[] suffix) {
        activity.getSupportLoaderManager().initLoader(0, null,
                new FileLoaderCallbacks(activity, callback, TYPE_FILE, suffix));
    }
}
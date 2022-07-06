package com.mr_w.resourceplus.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.mr_w.resourceplus.ResourcePlusApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Cache {

    public static final String TAG = Cache.class.getSimpleName();
    private static final String CHILD_DIR = "images";
    private static final String TEMP_FILE_NAME = "pic";
    private static final String FILE_EXTENSION = ".jpg";
    private static final int COMPRESS_QUALITY = 100;
    private File file;

    public File saveImgToCache(Bitmap bitmap, @Nullable String name) {
        File cachePath = null;
        String fileName = TEMP_FILE_NAME;
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        try {
            cachePath = new File(ResourcePlusApplication.getContext().getCacheDir(), CHILD_DIR);
            cachePath.mkdirs();
            file = new File(cachePath + "/" + fileName + FILE_EXTENSION);

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, stream);
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "saveImgToCache error: " + bitmap, e);
        }
        return file;
    }

    public Uri saveToCacheAndGetUri(Bitmap bitmap) {
        return saveToCacheAndGetUri(bitmap, null);
    }

    public Uri saveToCacheAndGetUri(Bitmap bitmap, @Nullable String name) {
        File file = saveImgToCache(bitmap, name);
        return getImageUri(file, name);
    }

    @Nullable
    public Uri getUriByFileName(String name) {
        Context context = ResourcePlusApplication.getContext();
        String fileName;
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        } else {
            return null;
        }

        File imagePath = new File(context.getCacheDir(), CHILD_DIR);
        File newFile = new File(imagePath, fileName + FILE_EXTENSION);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", newFile);
    }

    private Uri getImageUri(File fileDir, @Nullable String name) {
        Context context = ResourcePlusApplication.getContext();
        String fileName = TEMP_FILE_NAME;
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        File newFile = new File(fileDir, fileName + FILE_EXTENSION);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", newFile);
    }

    public String getContentType(Uri uri) {
        return ResourcePlusApplication.getContext().getContentResolver().getType(uri);
    }

}


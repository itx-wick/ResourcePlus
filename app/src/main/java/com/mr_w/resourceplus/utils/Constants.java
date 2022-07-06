package com.mr_w.resourceplus.utils;

import android.os.Environment;

public class Constants {
    public static final String CHAT_SERVER_URL = "https://socket-io-chat.now.sh/";

    public static final String folder_pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/ReSource+/";
    public static final String folder_videos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/ReSource+/";
    public static final String folder_audios = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/ReSource+/";
    public static final String folder_documents = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/ReSource+/";
    //    public static final String videosSubFolder = "Videos/";
//    public static final String audiosSubFolder = "Audios/";
//    public static final String documentsSubFolder = "Documents/";
    public static final String imagesSubFolder = "Images/";
    public static final String gifSubFolder = "Gifs/";
}
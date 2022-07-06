package com.mr_w.resourceplus.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.mr_w.resourceplus.ResourcePlusApplication;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class Utils {

    public static int HEIGHT, WIDTH;

    public static void vibe(Context c, long l) {
        ((Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(l);
    }

    public static void getScreenSize(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        HEIGHT = displayMetrics.heightPixels;
        WIDTH = displayMetrics.widthPixels;
    }

    public static void SlideToAbove(View view) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                5.0f, Animation.RELATIVE_TO_SELF, 0.0f);

        slide.setDuration(800);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        view.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

        });

    }

    public static void SlideToDown(View view) {
        Animation slide = null;
        slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 5.2f);

        slide.setDuration(400);
        slide.setFillAfter(true);
        slide.setFillEnabled(true);
        view.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.GONE);
            }

        });

    }

    public static boolean isFilePresent(String url) {

        String fileName;
        File file;
        URL url2;
        if (!url.contains("http")) {
            file = new File(Uri.parse(url).getPath());
        } else {
            try {
                url2 = new URL(url);
                fileName = FilenameUtils.getName(url2.getPath());
            } catch (Exception e) {
                return false;
            }
            String path = getDirectoryPath(fileName) + fileName;
            file = new File(path);
        }

        return file.exists();
    }

    public static boolean checkInternet(Context context) {
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mgr.getActiveNetworkInfo();
        if (netInfo != null) {
            return netInfo.isConnected();
        } else {
            return false;
        }
    }

    public static String getDirectoryPath(String fileName) {
        String folder = null;

        if (fileName.contains(".mpg") || fileName.contains(".mpeg") || fileName.contains(".mpe")
                || fileName.contains(".mp4") || fileName.contains(".avi"))
//            folder = Constants.folder_videos + fileName;
            folder = Constants.folder_videos;
        else if (fileName.contains(".3gp") || fileName.contains(".flac") || fileName.contains(".mwa") || fileName.contains(".wav")
                || fileName.contains(".mp3") || fileName.contains(".m4a")) {
            folder = Constants.folder_audios;
        } else if (fileName.contains(".doc") || fileName.contains(".docs") || fileName.contains(".pdf") || fileName.contains(".ppt")
                || fileName.contains(".pptx") || fileName.contains(".xls") || fileName.contains(".xlsx") || fileName.contains(".zip")
                || fileName.contains(".rar") || fileName.contains(".rtf") || fileName.contains(".txt"))
            folder = Constants.folder_documents;
        else if (fileName.contains(".jpg") || fileName.contains(".jpeg") || fileName.contains(".png"))
            folder = Constants.folder_pictures + Constants.imagesSubFolder;
        else if (fileName.contains(".gif"))
            folder = Constants.folder_pictures + Constants.gifSubFolder;

        return folder;
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    public static Boolean isFileBiggerThan4MB(File file) {
        long fileSizeInBytes = file.length();
        double fileSizeInKB = fileSizeInBytes / 1024.0;
        double fileSizeInMB = fileSizeInKB / 1024.0;

        return fileSizeInMB > 4.0;
    }

    public static double getFileSize(File file) {
        long fileSizeInBytes = file.length();
        double fileSizeInKB = fileSizeInBytes / 1024.0;
        double fileSizeInMB = fileSizeInKB / 1024.0;

        return fileSizeInMB;
    }

    public static void openFile(File filepath, Context context) {
        try {
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", filepath);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (filepath.toString().contains(".doc") || filepath.toString().contains(".docx")) {
                intent.setDataAndType(uri, "application/msword");
            } else if (filepath.toString().contains(".pdf")) {
                intent.setDataAndType(uri, "application/pdf");
            } else if (filepath.toString().contains(".ppt") || filepath.toString().contains(".pptx")) {
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (filepath.toString().contains(".xls") || filepath.toString().contains(".xlsx")) {
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (filepath.toString().contains(".zip")) {
                intent.setDataAndType(uri, "application/zip");
            } else if (filepath.toString().contains(".rar")) {
                intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (filepath.toString().contains(".rtf")) {
                intent.setDataAndType(uri, "application/rtf");
            } else if (filepath.toString().contains(".wav") || filepath.toString().contains(".mp3")) {
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (filepath.toString().contains(".gif")) {
                intent.setDataAndType(uri, "image/gif");
            } else if (filepath.toString().contains(".jpg") || filepath.toString().contains(".jpeg") || filepath.toString().contains(".png")) {
                intent.setDataAndType(uri, "image/jpeg");
            } else if (filepath.toString().contains(".txt")) {
                intent.setDataAndType(uri, "text/plain");
            } else if (filepath.toString().contains(".3gp") || filepath.toString().contains(".mpg") ||
                    filepath.toString().contains(".mpeg") || filepath.toString().contains(".mpe") || filepath.toString().contains(".mp4") || filepath.toString().contains(".avi")) {
                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Open with..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("DefaultLocale")
    public static String msToTimeConverter(int millis) {
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    public static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public static File getFileFromBitmap(Bitmap bitmap, Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null)
            cacheDir = context.getCacheDir();
        String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
        File root = new File(rootDir);
        if (!root.exists())
            root.mkdirs();
        File compressed = new File(root, new SimpleDateFormat("yyyy_MM_dd_hh:mm:ss").format(new Date()) + ".jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(compressed);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressed;
    }

    public static void saveBitmapToLocation(Bitmap bitmap, String path, Context context) {
        if (context == null)
            throw new NullPointerException("Context must not be null.");
        File file = new File(path);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(byteArrayOutputStream.toByteArray());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getCompressed(Context context, String path) throws IOException {
        if (context == null)
            throw new NullPointerException("Context must not be null.");
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null)
            cacheDir = context.getCacheDir();
        String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
        File root = new File(rootDir);
        if (!root.exists())
            root.mkdirs();
        Bitmap bitmap = decodeImageFromFiles(path, 480, 480);
        bitmap = rotateImageIfRequired(bitmap, Uri.fromFile(new File(path)));
        File compressed = new File(root, new SimpleDateFormat("yyyy_MM_dd_hh:mm:ss").format(new Date()) + ".jpg");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(compressed);
        fileOutputStream.write(byteArrayOutputStream.toByteArray());
        fileOutputStream.flush();
        fileOutputStream.close();
        return compressed;
    }

    public static Bitmap decodeImageFromFiles(String path, int width, int height) {
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, scaleOptions);
        int scale = 1;
        while (scaleOptions.outWidth / scale / 2 >= width
                && scaleOptions.outHeight / scale / 2 >= height) {
            scale *= 2;
        }
        BitmapFactory.Options outOptions = new BitmapFactory.Options();
        outOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, outOptions);
    }

    public static float dpToPx(float dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void hideKeyboard(Activity activity) {
        try {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showKeyboard(Activity activity) {
        try {
            ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri saveToCacheAndGetUri(Context context, Bitmap bitmap) {
        return saveToCacheAndGetUri(context, bitmap, null);
    }

    public Uri saveToCacheAndGetUri(Context context, Bitmap bitmap, @Nullable String name) {
        File file = saveImgToCache(context, bitmap, name);
        return getImageUri(context, file, name);
    }

    public File saveImgToCache(Context context, Bitmap bitmap, @Nullable String name) {
        File root = null;
        String fileName = System.currentTimeMillis() + "";
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        try {
//            cachePath = new File(getContext().getExternalCacheDir(), "images/");
//            if (!cachePath.exists()){
//                cachePath.mkdirs();
//            }
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null)
                cacheDir = context.getCacheDir();

            String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
            root = new File(rootDir);
            if (!root.exists())
                root.mkdirs();
            String imageName = fileName + ".jpeg";
            FileOutputStream stream = new FileOutputStream(new File(rootDir, imageName));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            Log.e(ResourcePlusApplication.TAG, "saveImgToCache error: " + bitmap, e);
        }
        return root;
    }

    private Uri getImageUri(Context context, File fileDir, @Nullable String name) {
        String fileName = System.currentTimeMillis() + "";
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        File newFile = new File(fileDir, fileName);
        return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", newFile);
    }

    public static byte[] getFileDataFromDrawable(Bitmap bm, float degree) {
        if (bm == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap resizedImage = Bitmap.createScaledBitmap(bm, bm.getWidth() / 2, bm.getHeight() / 2, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        resizedImage.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        return baos.toByteArray();
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static byte[] convert(String path) throws IOException {

        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fis.read(b)) != -1; ) {
            bos.write(b, 0, readNum);
        }

        return bos.toByteArray();
    }

}

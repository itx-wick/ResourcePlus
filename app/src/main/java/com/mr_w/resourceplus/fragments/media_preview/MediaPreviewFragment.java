package com.mr_w.resourceplus.fragments.media_preview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.databinding.FragmentMediaPreviewBinding;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class MediaPreviewFragment extends BaseFragment<FragmentMediaPreviewBinding, MediaPreviewViewModel> implements MediaPreviewNavigator {

    private static final String TAG = "MediaPreviewFragment";
    private final int IMAGE_GALLERY_REQUEST = 111;
    FragmentMediaPreviewBinding mBinding;
    private BottomSheetDialog bottomSheetDialog;
    private File file;
    private String imageName;
    private ProgressDialog dialog;
    private Conversation conversation;
    private UserPreferences preferences;

    public MediaPreviewFragment() {
    }

    @Override
    public int getBindingVariable() {
        return BR.media_preview;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_media_preview;
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = getViewDataBinding();
        preferences = UserPreferences.getInstance(getActivity(), "UserPrefs");
        conversation = (Conversation) getArguments().getSerializable("conversation");
        String uri = getArguments().getString("uri");
        showGroupIcon(uri);
    }

    public void showGroupIcon(String uri) {

        Uri path;
        final Bitmap[] bmp = {null};
        RequestOptions requestOptions = null;
        if (Utils.isFilePresent(uri)) {
            String filename = uri.substring(uri.lastIndexOf('/') + 1);
            path = FileProvider.getUriForFile(getActivity(), ResourcePlusApplication.getContext().getPackageName() + ".provider",
                    new File(Utils.getDirectoryPath(filename) + filename));
            mBinding.imageView.setImageURI(path);
        } else if (conversation.getType().equals("group")) {
            requestOptions = new RequestOptions()
                    .placeholder(R.drawable.group_icon)
                    .error(R.drawable.group_icon);
        } else {
            requestOptions = new RequestOptions()
                    .placeholder(R.drawable.icon_male_ph)
                    .error(R.drawable.icon_male_ph);
        }

        Glide.with(getActivity()).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                mBinding.imageView.setImageBitmap(resource);
                bmp[0] = resource;
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
        if (bmp[0] == null)
            Glide.with(getContext()).load(conversation.getType().equals("group") ?
                    R.drawable.group_icon :
                    R.drawable.icon_male_ph).into(mBinding.imageView);

        mBinding.btnBack.setOnClickListener(v -> getActivity().onBackPressed());
        mBinding.btnEdit.setOnClickListener(v -> {
            showBottomSheetPickPhoto();
        });
        mBinding.btnShare.setOnClickListener(v -> {
            if (bmp[0] != null) {
                Uri uris = getImageUri(getContext(), bmp[0]);
                ShareIntentCalled(uris);
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Uri saveImage(Bitmap image) {
        File imagesFolder = new File(getContext().getCacheDir(), "images");
        Uri uri = null;
        try {
            imagesFolder.mkdirs();
            File file = new File(imagesFolder, "temp.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = Uri.fromFile(file);

        } catch (IOException e) {
            Log.d("TAG", "IOException while trying to write file for sharing: " + e.getMessage());
        }
        return uri;
    }

    private void ShareIntentCalled(Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        startActivityForResult(Intent.createChooser(intent, "Share Image"), 100);
    }

    private void showBottomSheetPickPhoto() {
        @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

        ((View) view.findViewById(R.id.ln_gallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkCameraPermission())
                    openGallery();
                else requestCameraPermission(111);
                bottomSheetDialog.dismiss();
            }
        });
        ((View) view.findViewById(R.id.ln_camera)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkCameraPermission())
                    openCamera();
                else requestCameraPermission(222);
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bottomSheetDialog = null;
            }
        });

        bottomSheetDialog.show();
    }

    private boolean checkCameraPermission() {
        int camera_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        int write_external_strorage_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return camera_result == PackageManager.PERMISSION_DENIED || write_external_strorage_result == PackageManager.PERMISSION_DENIED;
    }

    private void openCamera() {
        Options options = Options.init()
                .setRequestCode(440)
                .setCount(1)
                .setFrontfacing(true)
                .setExcludeVideos(true)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);

        Pix.start(MediaPreviewFragment.this, options);
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    private void requestCameraPermission(int reqCode) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, reqCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111) {
            if (hasAllPermissionsGranted(grantResults)) {
                openGallery();
            } else {
                requestCameraPermission(111);
            }
        } else if (requestCode == 222) {
            if (hasAllPermissionsGranted(grantResults)) {
                openCamera();
            } else {
                requestCameraPermission(222);
            }
        }
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 440) {

            ArrayList<String> mResults = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            assert mResults != null;
            Uri uri = Uri.fromFile(new File(mResults.get(0)));
            try {
                file = Utils.getCompressed(getContext(), uri.getPath());
                performCrop(FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (requestCode == IMAGE_GALLERY_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {
            Uri imageUri = data.getData();

            String imagePath;
            Cursor cursor = null;
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                cursor = getContext().getContentResolver().query(imageUri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                imagePath = cursor.getString(column_index);
            } catch (Exception e) {
                imagePath = imageUri.getPath();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            try {
                file = Utils.getCompressed(getContext(), imagePath);
                performCrop(FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            com.theartofdev.edmodo.cropper.CropImage.ActivityResult result = com.theartofdev.edmodo.cropper.CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    Bitmap thePic = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                    String imagePath;
                    Cursor cursor = null;
                    try {
                        String[] proj = {MediaStore.Images.Media.DATA};
                        cursor = getActivity().getContentResolver().query(resultUri, proj, null, null, null);
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        imagePath = cursor.getString(column_index);
                    } catch (Exception e) {
                        imagePath = resultUri.getPath();
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    file = new File(imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                viewModel.setActivity(new WeakReference<>((ChatActivity) getActivity()));
                viewModel.setConversation(conversation);
                viewModel.setPhoneNumber(preferences.getUserDetails().getPhoneNo());
                viewModel.uploadFile(file);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void performCrop(Uri picUri) {
        CropImage.activity(picUri)
                .setAspectRatio(1, 1)
                .start(getContext(), this);
    }

    public Uri saveToCacheAndGetUri(Bitmap bitmap) {
        return saveToCacheAndGetUri(bitmap, null);
    }

    public Uri saveToCacheAndGetUri(Bitmap bitmap, @Nullable String name) {
        File file = saveImgToCache(bitmap, name);
        return getImageUri(file, name);
    }

    public File saveImgToCache(Bitmap bitmap, @Nullable String name) {
        File root = null;
        String fileName = System.currentTimeMillis() + "";
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        try {
            File cacheDir = getContext().getExternalCacheDir();
            if (cacheDir == null)
                cacheDir = getContext().getCacheDir();

            String rootDir = cacheDir.getAbsolutePath() + "/ImageCompressor";
            root = new File(rootDir);
            if (!root.exists())
                root.mkdirs();
            imageName = fileName + ".jpeg";
            FileOutputStream stream = new FileOutputStream(new File(rootDir, imageName));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (IOException e) {
            Log.e(TAG, "saveImgToCache error: " + bitmap, e);
        }
        return root;
    }

    private Uri getImageUri(File fileDir, @Nullable String name) {
        String fileName = System.currentTimeMillis() + "";
        if (!TextUtils.isEmpty(name)) {
            fileName = name;
        }
        File newFile = new File(fileDir, imageName);
        return FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", newFile);
    }

    @Override
    public void showProgressBar() {
        if (dialog == null) {
            dialog = new ProgressDialog(getContext());
            dialog.show();
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        if (!dialog.isShowing())
            dialog.show();
    }

    @Override
    public void hideProgressBar() {
        dialog.dismiss();
    }

    @Override
    public void goBack() {
        getActivity().onBackPressed();
    }
}
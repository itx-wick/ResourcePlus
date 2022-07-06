package com.mr_w.resourceplus.fragments.register_fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.Navigation;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.databinding.FragmentRegisterBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class RegistrationFragment extends BaseFragment<FragmentRegisterBinding, RegisterViewModel> implements View.OnClickListener,
        RegisterNavigator {

    private FragmentRegisterBinding mBinding;
    private BottomSheetDialog bottomSheetDialog;
    ProgressDialog dialog;
    String mobileNumber = "", mobileNumber2 = "";
    private File file;
    private Bitmap thePic;

    @Override
    public int getBindingVariable() {
        return BR.register;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_register;
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
        viewModel.setActivity(new WeakReference<>((StartActivity) getActivity()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBinding = getViewDataBinding();
        mBinding.userFullName.setText(viewModel.getDataManager().getUserDetails().getName());
        mBinding.userFullName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.userFullName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBinding.tvAbout2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBinding.userFullName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mobileNumber = viewModel.getDataManager().getUserDetails().getPhoneNo();
        mBinding.fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.fabCamera.setEnabled(false);
                showBottomSheetPickPhoto();
            }
        });
        mBinding.btnSubmit3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSubmit3) {

            JSONObject params = new JSONObject();
            try {
                params.put("name", mBinding.userFullName.getText().toString());
                params.put("number", mobileNumber);
                params.put("number1", mobileNumber2);
                params.put("userBio", mBinding.tvAbout2.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (thePic != null) {
                viewModel.uploadFile(params, file, v);
            } else {
                try {
                    params.put("image", "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                viewModel.signUp(v, params);
            }
            closeKeyboard();
        }
    }

    private boolean checkCameraPermission() {
        int camera_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        int write_external_strorage_result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return camera_result == PackageManager.PERMISSION_DENIED || write_external_strorage_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestCameraPermission(int reqCode) {
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, reqCode);
    }

    private void showBottomSheetPickPhoto() {
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.bottom_sheet_pick, null);

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

        bottomSheetDialog = new BottomSheetDialog(getActivity());
        bottomSheetDialog.setContentView(view);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBinding.fabCamera.setEnabled(true);
                bottomSheetDialog = null;
            }
        });

        bottomSheetDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    private void openCamera() {

        Options options = Options.init()
                .setCount(1)
                .setFrontfacing(true)
                .setExcludeVideos(true)
                .setVideoDurationLimitinSeconds(30)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = new Intent(getContext(), Pix.class);
        intent.putExtra("options", options);

        cameraRequest.launch(intent);
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/* video/*");
        photoPickerIntent.setType("image/jpeg, image/jpg, image/png, video/*");
        galleryRequest.launch(photoPickerIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    thePic = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
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
                reviewImage(thePic);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendMedia(getActivity(), bitmap, true).show(new DialogReviewSendMedia.OnCallBack() {
            @Override
            public void onButtonSendClick() {

                progressDialogShow();
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        mBinding.userImage.setImageBitmap(bitmap);
                    }
                };
                handler.post(runnable);
            }
        });
    }

    private void progressDialogShow() {
        if (dialog == null || !dialog.isShowing()) {
            dialog = new ProgressDialog(getActivity());
            dialog.show();
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
    }

    private void performCrop(Uri picUri) {
        CropImage.activity(picUri)
                .setAspectRatio(1, 1)
                .start(getContext(), this);
    }

    private void closeKeyboard() {
        View view = getActivity().getCurrentFocus();

        if (view != null) {
            InputMethodManager manager
                    = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    ActivityResultLauncher<Intent> cameraRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ArrayList<String> mResults = result.getData().getStringArrayListExtra(Pix.IMAGE_RESULTS);
                    assert mResults != null;
                    Uri uri = Uri.fromFile(new File(mResults.get(0)));
                    try {
                        file = Utils.getCompressed(getContext(), uri.getPath());
                        performCrop(FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    ActivityResultLauncher<Intent> galleryRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();

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
                }
            });

    @Override
    public void showProgressBar() {
        progressDialogShow();
    }

    @Override
    public void hideProgressBar() {
        if (dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateForward(View v) {
        Navigation.findNavController(v).navigate(R.id.action_userInfoFragment_to_mainFragment);
    }
}
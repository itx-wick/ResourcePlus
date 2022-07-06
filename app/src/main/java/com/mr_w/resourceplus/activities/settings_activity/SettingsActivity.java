package com.mr_w.resourceplus.activities.settings_activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;

import com.bumptech.glide.Glide;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.activities.profile_activity.ProfileActivity;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.databinding.ActivitySettingsBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.model.PhoneValidateResponse;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding, SettingsViewModel>
        implements View.OnClickListener,
        SettingsNavigator {

    private ActivitySettingsBinding mBinding;
    private Users me;
    AlertDialog alertDialog;
    private BottomSheetDialog bottomSheetDialog;
    private final int IMAGE_GALLERY_REQUEST = 111;
    ProgressDialog dialog;
    String phone2;
    EditText editName, editAbout, editMobileNumber;
    TextView checkView;
    CountryCodePicker ccp;
    String countryCode1 = "+92", countryNameCode1;
    private File file;
    private Bitmap thePic;
    private List<String> communicatedIds = new ArrayList<>();
    private final Socket mSocket = ResourcePlusApplication.mSocket;

    @Override
    public int getBindingVariable() {
        return BR.settings;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        performDataBinding();
        mBinding = getViewDataBinding();
        viewModel.setNavigator(this);
        me = viewModel.getDataManager().getUserDetails();

        mBinding.tvUsername.setText(me.getName());
        Glide.with(this).load(me.getPhoto()).placeholder(R.drawable.no_pic).into(mBinding.imageProfile);
        mBinding.tvAbout.setText(me.getAbout().equals("") ? "Hey there, I am using ResourcePlus" : me.getAbout());

        mBinding.btnBackSettings.setOnClickListener(this);
        mBinding.layoutChangeNumber.setOnClickListener(this);
        mBinding.layoutChangePhoto.setOnClickListener(this);
        mBinding.layoutChangeName.setOnClickListener(this);
        mBinding.layoutChangeAbout.setOnClickListener(this);
        mBinding.btnUserProfile.setOnClickListener(this);
        mBinding.layoutLogout.setOnClickListener(this);
        mBinding.layoutSwitchAccount.setOnClickListener(this);

        viewModel.getCommunications().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> list) {
                communicatedIds = list;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 2) {

            ArrayList<String> mResults = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            assert mResults != null;
            Uri uri = Uri.fromFile(new File(mResults.get(0)));
            try {
                file = Utils.getCompressed(this, uri.getPath());
                performCrop(FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
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
                cursor = getContentResolver().query(imageUri, proj, null, null, null);
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
                file = Utils.getCompressed(this, imagePath);
                performCrop(FileProvider.getUriForFile(this, getPackageName() + ".provider", file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    thePic = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                    String imagePath;
                    Cursor cursor = null;
                    try {
                        String[] proj = {MediaStore.Images.Media.DATA};
                        cursor = getContentResolver().query(resultUri, proj, null, null, null);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_user_profile:
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
                break;
            case R.id.btn_back_settings:
                onBackPressed();
                overridePendingTransition(0, 0);
                break;
            case R.id.layoutChangeNumber:
                editNumberDialog(v);
                break;
            case R.id.layoutChangePhoto:
                showBottomSheetPickPhoto();
                break;
            case R.id.layoutChangeName:
                editNameDialog(v);
                break;
            case R.id.layoutChangeAbout:
                editAboutDialog(v);
                break;
//            case R.id.layoutBackup:
//                Toast.makeText(this, "Backup Clicked", Toast.LENGTH_SHORT).show();
//                break;
            case R.id.layoutSwitchAccount:
                showDialog();
                break;
            case R.id.layoutLogout:
                viewModel.getDataManager().saveBoolean(UserPreferences.PREF_USER_IS_LOGIN, false);
                viewModel.getDataManager().saveBoolean(UserPreferences.PREF_CHATS_LOADED, false);
                viewModel.getDataManager().setUserDetails(null);
                deleteAllMessages();
                deleteAllConversations();
                Intent intent = new Intent(this, StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
        }
    }

    public void editNameDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(SettingsActivity.this, R.style.DialogSlideAnim));
        ViewGroup viewGroup = findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_name_dialog, viewGroup, false);
        builder.setView(dialogView);

        editName = dialogView.findViewById(R.id.editFullName);
        Button updateNameBtn = dialogView.findViewById(R.id.btnUpdateName);
        editName.setText(viewModel.getDataManager().getUserDetails().getName());
        alertDialog = builder.create();

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(24)});
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        updateNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editName.length() == 0 || editName.getText().toString().trim().equals("")) {
                    Toast.makeText(SettingsActivity.this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("name", editName.getText().toString());
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }

                viewModel.updateUserProfile(jsonObject, "name");
                alertDialog.dismiss();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public void editNumberDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(SettingsActivity.this, R.style.DialogSlideAnim));
        ViewGroup viewGroup = findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_number_dialog, viewGroup, false);
        builder.setView(dialogView);

        editMobileNumber = (EditText) dialogView.findViewById(R.id.editMobileNumber);
        checkView = dialogView.findViewById(R.id.checkView);
        ccp = dialogView.findViewById(R.id.ccpUpdate);
        Button updateBtn = (Button) dialogView.findViewById(R.id.btnUpdateNumber);
        editMobileNumber.setText(viewModel.getDataManager().getUserDetails().getPhoneNo().split(" ")[0]);
        phone2 = viewModel.getDataManager().getUserDetails().getPhoneNo();
        alertDialog = builder.create();


        ccp.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                countryCode1 = ccp.getSelectedCountryCodeWithPlus();
                countryNameCode1 = ccp.getSelectedCountryNameCode();
            }
        });

        editMobileNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editMobileNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1 && s.toString().startsWith("0")) {
                    s.clear();
                }
                if (s.toString().isEmpty()) {
                    dialogView.findViewById(R.id.checkView).setVisibility(View.GONE);
                }
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!editMobileNumber.getText().toString().isEmpty()) {
                    if (editMobileNumber.getText().toString().startsWith("319") || editMobileNumber.getText().toString().isEmpty()) {
                        checkView.setText("Please Enter Valid Number");
                        checkView.setVisibility(View.VISIBLE);
                    } else if (isPhoneNumberValidate(countryCode1, editMobileNumber.getText().toString())) {
                        checkView.setVisibility(View.GONE);

                        //Todo Update number api call
                        alertDialog.dismiss();

                    } else {
                        checkView.setText("Please Enter Valid Number 1");
                        checkView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public void editAboutDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(SettingsActivity.this, R.style.DialogSlideAnim));
        ViewGroup viewGroup = findViewById(android.R.id.content);
        final View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.edit_about_dialog, viewGroup, false);
        builder.setView(dialogView);

        editAbout = dialogView.findViewById(R.id.editAbout);
        Button updateAboutBtn = dialogView.findViewById(R.id.btnUpdateAbout);
        editAbout.setText(viewModel.getDataManager().getUserDetails().getAbout());
        alertDialog = builder.create();
        editAbout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editAbout.setFilters(new InputFilter[]{new InputFilter.LengthFilter(150)});
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        updateAboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    if (editAbout.length() == 0 || editAbout.getText().toString().trim().equals(""))
                        jsonObject.put("userBio", "Hey there! I am using ResourcePlus.");
                    else
                        jsonObject.put("userBio", editAbout.getText().toString());
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }

                viewModel.updateUserProfile(jsonObject, "about");
                alertDialog.dismiss();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
    }

    public static boolean isPhoneNumberValidate(String countryCode, String mobNumber) {
        PhoneValidateResponse phoneNumberValidate = new PhoneValidateResponse();
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneNumber = null;
        boolean finalNumber = false;
        PhoneNumberUtil.PhoneNumberType isMobile = null;
        boolean isValid = false;
        try {
            String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
            phoneNumber = phoneNumberUtil.parse(mobNumber, isoCode);
            isValid = phoneNumberUtil.isValidNumber(phoneNumber);
            isMobile = phoneNumberUtil.getNumberType(phoneNumber);
            phoneNumberValidate.setCode(String.valueOf(phoneNumber.getCountryCode()));
            phoneNumberValidate.setPhone(String.valueOf(phoneNumber.getNationalNumber()));
            phoneNumberValidate.setValid(false);

        } catch (NumberParseException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (isValid && (PhoneNumberUtil.PhoneNumberType.MOBILE == isMobile)) {
            finalNumber = true;
            phoneNumberValidate.setValid(true);
        }
        return finalNumber;
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_bottom_sheet);
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.BottomDialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }

    private boolean checkCameraPermission() {
        int camera_result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int write_external_strorage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return camera_result == PackageManager.PERMISSION_DENIED || write_external_strorage_result == PackageManager.PERMISSION_DENIED;
    }

    private void requestCameraPermission(int reqCode) {
        ActivityCompat.requestPermissions(this, new String[]{
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

        bottomSheetDialog = new BottomSheetDialog(this);
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
                .setRequestCode(2)
                .setCount(1)
                .setFrontfacing(true)
                .setExcludeVideos(true)
                .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT);

        Pix.start(this, options);
    }

    private void openGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);
    }

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendMedia(this, bitmap, true).show(new DialogReviewSendMedia.OnCallBack() {
            @Override
            public void onButtonSendClick() {

                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        viewModel.uploadFile(file);
                    }
                };
                handler.post(runnable);
            }
        });
    }

    private void performCrop(Uri picUri) {
        CropImage.activity(picUri)
                .setAspectRatio(1, 1)
                .start(this);
    }

    private void deleteAllMessages() {
        viewModel.deleteAllMessages();
    }

    private void deleteAllConversations() {
        viewModel.deleteAllConversations();
    }

    private void deleteAllUsers() {
        viewModel.deleteAllUsers();
    }

    @Override
    public void emitName(String name) {
        mBinding.tvUsername.setText(name);
        if (communicatedIds.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phoneId", me.getPhoneId());
                jsonObject.put("name", me.getName());
                jsonObject.put("toUsers", new Gson().toJson(communicatedIds));
            } catch (JSONException exception) {
                exception.printStackTrace();
            }

            if (!ResourcePlusApplication.mSocket.connected())
                ResourcePlusApplication.mSocket.connect();
            mSocket.emit("sendMessage", jsonObject);
        }
    }

    @Override
    public void emitAbout(String about) {

        mBinding.tvAbout.setText(about);
        if (communicatedIds.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phoneId", me.getPhoneId());
                jsonObject.put("about", me.getAbout());
                jsonObject.put("toUsers", new Gson().toJson(communicatedIds));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!ResourcePlusApplication.mSocket.connected())
                ResourcePlusApplication.mSocket.connect();
            mSocket.emit("sendMessage", jsonObject);
        }
    }

    @Override
    public void emitImage(String image) {
        Glide.with(SettingsActivity.this).load(image).into(mBinding.imageProfile);
        if (communicatedIds.size() > 0) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("phoneId", me.getPhoneId());
                jsonObject.put("image", me.getPhoto());
                jsonObject.put("toUsers", new Gson().toJson(communicatedIds));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!ResourcePlusApplication.mSocket.connected())
                ResourcePlusApplication.mSocket.connect();
            mSocket.emit("sendMessage", jsonObject);
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgressBar() {
        if (dialog == null) {
            dialog = new ProgressDialog(SettingsActivity.this);
            dialog.show();
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void hideProgressBar() {
        if (dialog.isShowing())
            dialog.dismiss();
    }
}
package com.mr_w.resourceplus.fragments.create_group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
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
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.androidnetworking.error.ANError;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.adapter.GroupAdapter;
import com.mr_w.resourceplus.databinding.FragmentCreateGroupBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.supernova_emoji.actions.EmojIconActions;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateGroupFragment extends BaseFragment<FragmentCreateGroupBinding, CreateGroupViewModel> implements
        View.OnClickListener,
        CreateGroupNavigator {

    private FragmentCreateGroupBinding mBinding;
    private List<Users> usersList = new ArrayList<>();
    private GroupAdapter mAdapter;
    private BottomSheetDialog bottomSheetDialog;
    private DataManager userPrefs;
    private String phoneId;
    private File file;
    private Bitmap thePic;
    boolean isKeyBoardShowing = false;
    private final List<Users> tempList = new ArrayList<>();
    private ProgressDialog dialog;

    @Override
    public int getBindingVariable() {
        return BR.create_group;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_create_group;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = getViewDataBinding();

        userPrefs = viewModel.getDataManager();
        phoneId = userPrefs.getUserDetails().getPhoneId();
        usersList = new Gson().fromJson(getArguments().getString("participants"), new TypeToken<List<Users>>() {
        }.getType());

        mBinding.addImageGroup.setOnClickListener(this);
        mBinding.groupImage.setOnClickListener(this);
        mBinding.fab.setOnClickListener(this);
        setData(usersList);

        mBinding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mBinding.searchBar.length() > 0) {
                    if (mBinding.clear.getVisibility() == View.GONE)
                        mBinding.clear.setVisibility(View.VISIBLE);
                    performSearch();
                } else {
                    mBinding.clear.setVisibility(View.GONE);
                    usersList.clear();
                    usersList.addAll(tempList);
                    setData(usersList);
                }
            }
        });

        mBinding.clear.setOnClickListener(v -> {
            mBinding.clear.setVisibility(View.GONE);
            mBinding.searchBar.setText("");
        });

        mBinding.search.setOnClickListener(v -> {
            Utils.showKeyboard(getActivity());
            tempList.clear();
            tempList.addAll(usersList);
            isKeyBoardShowing = true;
            mBinding.searchBar.requestFocus();
            mBinding.layoutSearch.setVisibility(View.VISIBLE);
            mBinding.title.setVisibility(View.GONE);
            mBinding.search.setVisibility(View.GONE);
        });
        mBinding.btnBack.setOnClickListener(v -> {
            if (mBinding.layoutSearch.getVisibility() == View.VISIBLE) {
                closeKeyboard();
                isKeyBoardShowing = false;
                mBinding.searchBar.setText("");
                mBinding.layoutSearch.setVisibility(View.GONE);
                mBinding.title.setVisibility(View.VISIBLE);
                mBinding.search.setVisibility(View.VISIBLE);
            } else {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    private void performSearch() {
        usersList.clear();
        for (Users user : tempList) {
            if (user.getName().toLowerCase().contains(mBinding.searchBar.getText().toString().toLowerCase())) {
                usersList.add(user);
            }
        }
        setData(usersList);
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeEmoji();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    public void onStop() {
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.group_image:
            case R.id.add_image_group:
                mBinding.addImageGroup.setEnabled(false);
                showBottomSheetPickPhoto();
                break;
            case R.id.fab:

                if (mBinding.groupTitle.length() == 0 || mBinding.groupTitle.getText().toString().trim().equals("")) {
                    Toast.makeText(getContext(), "Group title can not be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Users me = userPrefs.getUserDetails();
                usersList.add(me);
                List<String> admins = new ArrayList<>();
                admins.add(phoneId);

                List<String> ids = new ArrayList<>();
                for (Users users : usersList) {
                    if (!ids.contains(users.getPhoneId()))
                        ids.add(users.getPhoneId());
                }
                JSONArray temp = new JSONArray(ids);

                JSONObject postData = new JSONObject();
                try {
                    postData.put("title", mBinding.groupTitle.getText().toString());
                    postData.put("creator", userPrefs.getUserDetails().getPhoneId());
                    postData.put("admin", new JSONArray(admins));
                    postData.put("type", false);
                    postData.put("members", temp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                viewModel.setUsersList(usersList);
                viewModel.setPhoneNumber(userPrefs.getUserDetails().getPhoneNo());
                closeKeyboard();
                if (thePic != null) {
                    viewModel.uploadFile(postData, file);
                } else {
                    try {
                        postData.put("image", "");
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                    viewModel.createGroup(postData);
                }
                break;
        }
    }

    private void initializeEmoji() {
        EmojIconActions emojIcon = new EmojIconActions(getContext(), mBinding.getRoot(), mBinding.groupTitle, mBinding.emoticonBtn);
        emojIcon.setUseSystemEmoji(true);
        mBinding.groupTitle.setUseSystemDefault(true);
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley);
    }

    private void requestCameraPermission(int reqCode) {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
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

        bottomSheetDialog = new BottomSheetDialog(getContext());
        bottomSheetDialog.setContentView(view);

        Objects.requireNonNull(bottomSheetDialog.getWindow()).addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        bottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBinding.addImageGroup.setEnabled(true);
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
                .setCount(1)
                .setFrontfacing(true)
                .setExcludeVideos(true)
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

    private void reviewImage(Bitmap bitmap) {
        new DialogReviewSendMedia(getContext(), bitmap, true).show(new DialogReviewSendMedia.OnCallBack() {
            @Override
            public void onButtonSendClick() {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mBinding.addImageGroup.setVisibility(View.GONE);
                        mBinding.groupImage.setImageBitmap(bitmap);
                    }
                };
                handler.post(runnable);
            }
        });
    }

    private void setData(List<Users> list) {
        mAdapter = new GroupAdapter(getContext(), list);
        setListData(list);
        mBinding.ContactsList.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mBinding.ContactsList.setHasFixedSize(true);
        mBinding.ContactsList.setAdapter(mAdapter);
        mBinding.participantCounter.setText("Participants : " + list.size() + "/255");

        if (list.size() == 0) {
            mBinding.ContactsList.setVisibility(View.GONE);
        } else
            mBinding.ContactsList.setVisibility(View.VISIBLE);
    }

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void setListData(List<Users> dataItemList) {

        if (mAdapter != null) {
            mAdapter.setList(dataItemList);
        }
    }

    private void performCrop(Uri picUri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 360);
            cropIntent.putExtra("outputY", 360);
            cropIntent.putExtra("return-data", true);
            cropIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cropRequest.launch(cropIntent);
        } catch (ActivityNotFoundException anfe) {
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
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
            results -> {
                if (results.getResultCode() == Activity.RESULT_OK && results.getData() != null) {
                    ArrayList<String> mResults = results.getData().getStringArrayListExtra(Pix.IMAGE_RESULTS);
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

    ActivityResultLauncher<Intent> cropRequest = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    thePic = extras.getParcelable("data");

                    file = Utils.getFileFromBitmap(thePic, getContext());
                    reviewImage(thePic);
                }
            });

    @Override
    public void navigateToChatScreen(Conversation conversation) {
        startActivity(new Intent(getContext(), ChatActivity.class)
                .putExtra("conversation", conversation));
        getActivity().finish();
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void handleError(ANError error) {

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
}
package com.mr_w.resourceplus.fragments.camera_fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.databinding.FragmentCameraBinding;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.injections.di.component.FragmentComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseFragment;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.utils.Utils;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Audio;
import com.otaliastudios.cameraview.controls.Facing;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.controls.Mode;
import com.otaliastudios.cameraview.size.AspectRatio;
import com.otaliastudios.cameraview.size.SizeSelector;
import com.otaliastudios.cameraview.size.SizeSelectors;
import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.mr_w.resourceplus.activities.chat_activity.ChatActivity.isFileBiggerThan10MB;

public class CameraFragment extends BaseFragment<FragmentCameraBinding, CameraViewModel> implements CameraNavigator {

    private static final String TAG = "CameraFragment";
    FragmentCameraBinding binding;
    private Handler handler;
    private Runnable runnable;

    @Override
    public int getBindingVariable() {
        return BR.camera_fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_camera;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.cameraView.open();
        binding.cameraView.setMode(Mode.PICTURE);
    }

    @Override
    public void onPause() {
        binding.cameraView.close();
        super.onPause();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding = getViewDataBinding();

        binding.cameraView.open();
        binding.cameraView.setMode(Mode.PICTURE);

        initialize();
    }

    @Override
    public void performDependencyInjection(FragmentComponent buildComponent) {
        buildComponent.inject(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initialize() {
//        WindowManager.LayoutParams params = getWindow().getAttributes();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            params.layoutInDisplayCutoutMode =
//                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
//        }
        Utils.getScreenSize(getActivity());
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        binding.cameraView.setMode(Mode.PICTURE);
        binding.cameraView.setAudio(Audio.OFF);

        SizeSelector width = SizeSelectors.minWidth(Utils.WIDTH);
        SizeSelector height = SizeSelectors.minHeight(Utils.HEIGHT);
        SizeSelector dimensions = SizeSelectors.and(width, height); // Matches sizes bigger than width X height
        SizeSelector ratio = SizeSelectors.aspectRatio(AspectRatio.of(1, 2), 0); // Matches 1:2 sizes.
        SizeSelector ratio3 = SizeSelectors.aspectRatio(AspectRatio.of(2, 3), 0); // Matches 2:3 sizes.
        SizeSelector ratio2 = SizeSelectors.aspectRatio(AspectRatio.of(9, 16), 0); // Matches 9:16 sizes.
        SizeSelector result = SizeSelectors.or(
                SizeSelectors.and(ratio, dimensions),
                SizeSelectors.and(ratio2, dimensions),
                SizeSelectors.and(ratio3, dimensions)
        );
        binding.cameraView.setPictureSize(result);
        binding.cameraView.setLifecycleOwner(getViewLifecycleOwner());

        binding.cameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(@NotNull PictureResult result) {
                Utils.vibe(getActivity(), 50);
                result.toBitmap(bitmap -> {
                });
//                result.toFile();
            }

            @Override
            public void onVideoRecordingStart() {
                super.onVideoRecordingStart();
                Utils.vibe(getActivity(), 50);
                startTimer();
                Log.d(TAG, "onVideoRecordingStart: ");
            }

            @Override
            public void onVideoRecordingEnd() {
                super.onVideoRecordingEnd();
                Log.d(TAG, "onVideoRecordingEnd: ");
                handler.removeCallbacks(runnable);
                binding.counterLL.setVisibility(View.GONE);
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                Log.d(TAG, "onVideoTaken: ");
                new DialogReviewSendMedia(getContext()).show(Uri.fromFile(result.getFile()).getPath(), new DialogReviewSendMedia.OnCallBack() {
                    @Override
                    public void onButtonSendClick() {

                    }
                });
            }
        });

        binding.flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.cameraView.getFlash() == Flash.TORCH) {
                    binding.cameraView.setFlash(Flash.OFF);
                    binding.flashImage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_off_black_24dp));
                } else {
                    binding.cameraView.setFlash(Flash.TORCH);
                    binding.flashImage.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_flash_on_black_24dp));
                }
            }
        });

        binding.lensFacing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.cameraView.setFacing(binding.cameraView.getFacing() == Facing.BACK ? Facing.FRONT : Facing.BACK);
            }
        });

        binding.primaryClickButton.setOnClickListener(view -> {
            binding.cameraView.takePicture();
        });

        binding.primaryClickButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        binding.primaryClickBackground.setVisibility(View.VISIBLE);
                        binding.cameraView.setMode(Mode.VIDEO);
                        binding.cameraView.takeVideo(new File(getContext().getCacheDir().getAbsolutePath() + "/video_temp.mp4"));
                        break;
                    case MotionEvent.ACTION_UP:
                        binding.cameraView.stopVideo();
                        binding.primaryClickBackground.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reviewImage(bitmap, resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        }
    }

    private void reviewImage(Bitmap bitmap, Uri uri) {
        new DialogReviewSendMedia(getContext(), bitmap, true).show(() -> {
            if (uri != null) {
                String imagePath;
                Cursor cursor = null;
                try {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    imagePath = cursor.getString(column_index);
                } catch (Exception e) {
                    imagePath = uri.getPath();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                File file = new File(imagePath);
                if (isFileBiggerThan10MB(file)) {
                    Toast.makeText(getContext(), "Upload limit exceeded. Max 10MB file can be uploaded", Toast.LENGTH_LONG).show();
                } else {
                    MessageData message = new MessageData();
                    message.setId(UUID.randomUUID().toString());
                    message.setType(MessageData.TYPE.PICTURE);
                    message.setUrl(imagePath);
//                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
                    message.setStatus("sending");
                    message.setText(null);

                    DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                    message.setDateTime(utcFormat.format(new Date()));
//                    message.setSenderPhoneId(preferences.getUserDetails().getPhoneId());
//
//                    if (conversation == null) {
//                        viewModel.setMessage(message);
//                        viewModel.createConversation(user);
//                    } else {
//                        message.setConversationId(conversation.get_id());
//                        mChatAdapter.getmMessageData().add(message);
//                        mChatAdapter.notifyItemInserted(mChatAdapter.getmMessageData().size() - 1);
//                        mRecyclerView.scrollToPosition(mChatAdapter.getmMessageData().size() - 1);
//                    }
                }
            }

        });
    }

    private void performCrop(Uri picUri) {

        CropImage.activity(picUri)
                .start(getActivity());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.cameraView.destroy();
    }

    private void startTimer() {

        binding.counterLL.setVisibility(View.VISIBLE);
        long startTime = System.currentTimeMillis();
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis() - startTime;
                binding.videoCounter.setText(Utils.msToTimeConverter((int) current));
                handler.postDelayed(this, 1000);
            }
        };
        getActivity().runOnUiThread(runnable);
    }

}
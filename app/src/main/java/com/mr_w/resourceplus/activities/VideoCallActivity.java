package com.mr_w.resourceplus.activities;

import android.app.PictureInPictureParams;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.receivers.BluetoothStateReceiver;
import com.mr_w.resourceplus.receivers.WiredHeadsetStateReceiver;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.webRtc.PeerConnectionParameters;
import com.mr_w.resourceplus.webRtc.WebRtcClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import static com.mr_w.resourceplus.ResourcePlusApplication.TAG;

public class VideoCallActivity extends AppCompatActivity implements WebRtcClient.RtcListener,
        WiredHeadsetStateReceiver.WiredHeadsetStateReceiverListener,
        BluetoothStateReceiver.BluetoothStateReceiverListener {

    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private PictureInPictureParams.Builder builder;
    private final VideoRendererGui.ScalingType scalingType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;
    private GLSurfaceView vsv;
    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;
    private WebRtcClient client;
    private String mSocketAddress;
    private String callerId;
    private Users to;
    private CardView mic, video, hangup, switch_video;
    private RelativeLayout infoLayout;
    private TextView title, video_call_status;
    private boolean isMuted = false, isVideoPaused = false;
    private ImageView img_mic, img_video, pic;
    private BroadcastReceiver messageReceiver;
    private boolean isConnected = false;
    private WiredHeadsetStateReceiver wiredHeadsetStateReceiver = null;
    private BluetoothStateReceiver bluetoothStateReceiver = null;
    private AudioManager audioManager;
    private LinearLayout pipLayout;
    private LinearLayout videoControls;
    private LinearLayout incomingLayout;
    private CardView hangupVideo;
    private Button joinCall;
    private boolean isInComing;

//    public void startCallNotification() {
//        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        ringtone  = RingtoneManager.getRingtone(this, notification);
//        ringtone.play();
//        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
//        long[] vibrationCycle = {0, 1000, 1000};
//        if (vibrator.hasVibrator()) {
//            vibrator.vibrate(vibrationCycle, 1);
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_video_call);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new PictureInPictureParams.Builder();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        callerId = getIntent().hasExtra("caller_id") ? getIntent().getStringExtra("caller_id") : null;
        to = getIntent().hasExtra("to") ? (Users) getIntent().getSerializableExtra("to") : null;
        isInComing = getIntent().hasExtra("incoming") && getIntent().getBooleanExtra("incoming", false);
        mSocketAddress = "http://" + getResources().getString(R.string.host);
        mSocketAddress += (":" + getResources().getString(R.string.port) + "/");

        vsv = findViewById(R.id.glview_call);
        pipLayout = findViewById(R.id.pipLayout);
        videoControls = findViewById(R.id.video_controls);
        incomingLayout = findViewById(R.id.incoming_layout);
        mic = findViewById(R.id.audio);
        video = findViewById(R.id.video);
        hangup = findViewById(R.id.hangup);
        hangupVideo = findViewById(R.id.hangup_video);
        joinCall = findViewById(R.id.joinCall);
        switch_video = findViewById(R.id.toggle_video);
        img_mic = findViewById(R.id.img_mic);
        img_video = findViewById(R.id.img_video);
        title = findViewById(R.id.title);
        pic = findViewById(R.id.pic);
        infoLayout = findViewById(R.id.infoLayout);
        video_call_status = findViewById(R.id.video_call_status);

        if (isInComing) {
            videoControls.setVisibility(View.GONE);
            incomingLayout.setVisibility(View.VISIBLE);
            infoLayout.setVisibility(View.VISIBLE);

            hangupVideo.setOnClickListener(v -> {
                JSONObject answer = new JSONObject();
                try {
                    answer.put("answer", "decline");
                    answer.put("to", to.getPhoneId());
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }

                if (!ResourcePlusApplication.mSocket.connected())
                    ResourcePlusApplication.mSocket.connect();
                ResourcePlusApplication.mSocket.emit("sendMessage", answer);
                finish();
            });

            joinCall.setOnClickListener(v -> {
                JSONObject answer = new JSONObject();
                try {
                    answer.put("answer", "accept");
                    answer.put("to", to.getPhoneId());
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }

                if (!ResourcePlusApplication.mSocket.connected())
                    ResourcePlusApplication.mSocket.connect();
                ResourcePlusApplication.mSocket.emit("sendMessage", answer);

                incomingLayout.setVisibility(View.GONE);
                videoControls.setVisibility(View.VISIBLE);
                infoLayout.setVisibility(View.GONE);

                try {
                    answer(callerId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
        }
        initialize();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setWiredHeadsetStateReceiver();
        setBluetoothStateReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
//        vsv.onPause();
//        if (client != null) {
//            client.onPause();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        vsv.onResume();
//        if (client != null) {
//            client.onResume();
//        }
    }

    @Override
    public void onDestroy() {
        isConnected = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        if (client != null) {
            client.onDestroy();
        }
        wiredHeadsetStateReceiver.removeListener(this);
        bluetoothStateReceiver.removeListener(this);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        super.onDestroy();
    }

    @Override
    public void onHeadsetConnected() {

        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onHeadsetDisconnected() {
        audioManager.setSpeakerphoneOn(true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    @Override
    public void onBluetoothConnected() {
        audioManager.setBluetoothScoOn(true);
        audioManager.setSpeakerphoneOn(false);
        audioManager.startBluetoothSco();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onBluetoothDisconnected() {
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(true);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    @Override
    public void onCallReady(String callId) {
        if (callerId == null) {
            call(callId);
        }
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(() -> Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
        localStream.videoTracks.get(0).addRenderer(new VideoRenderer(localRender));
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType, false);
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
        remoteStream.videoTracks.get(0).addRenderer(new VideoRenderer(remoteRender));
        VideoRendererGui.update(remoteRender,
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTED, LOCAL_Y_CONNECTED,
                LOCAL_WIDTH_CONNECTED, LOCAL_HEIGHT_CONNECTED,
                scalingType, false);
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        VideoRendererGui.update(localRender,
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING,
                scalingType, false);

        finish();
    }

    private void initialize() {

        if (callerId == null) {
            infoLayout.setVisibility(View.VISIBLE);
        }

        title.setText(to.getName());
        if (to.getPhoto().equals("")) {
            pic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_pic));
        } else
            Glide.with(this).load(to.getPhoto()).into(pic);

        vsv.setPreserveEGLContextOnPause(true);
        vsv.setKeepScreenOn(true);
        VideoRendererGui.setView(vsv, new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
        remoteRender = VideoRendererGui.create(
                REMOTE_X, REMOTE_Y,
                REMOTE_WIDTH, REMOTE_HEIGHT, scalingType, false);

        localRender = VideoRendererGui.create(
                LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING,
                LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING, scalingType, true);

        pipLayout.setOnClickListener(v -> {
            pictureInPictureMode();
        });

        video.setOnClickListener(v -> {
            if (isVideoPaused) {
                if (client != null) {
                    client.onResume();
                } else
                    vsv.onResume();
                img_video.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_videocall));
                isVideoPaused = false;
            } else {
                if (client != null) {
                    client.onPause();
                } else
                    vsv.onPause();
                img_video.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_video_off));
                isVideoPaused = true;
            }
        });

        hangup.setOnClickListener(v -> {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("to", to.getPhoneId());
                if (!isConnected)
                    jsonObject.put("cancel", "pre");
                else
                    jsonObject.put("cancel", "post");
            } catch (JSONException exception) {
                exception.printStackTrace();
            }

            if (!ResourcePlusApplication.mSocket.connected())
                ResourcePlusApplication.mSocket.connect();
            ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);

            finish();
        });

        switch_video.setOnClickListener(v -> {
            client.switchCamera();
        });

        mic.setOnClickListener(v -> {
            if (isMuted) {
                setMicMuted(false);
                img_mic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_voice_call));
                isMuted = false;
            } else {
                setMicMuted(true);
                img_mic.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_voice_off));
                isMuted = true;
            }
        });

        audioManager.setSpeakerphoneOn(true);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setMicMuted(false);

    }

    private void init() {
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParameters params = new PeerConnectionParameters(
                true, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, false);

        client = new WebRtcClient(this, mSocketAddress, params, VideoRendererGui.getEGLContext());
        registerReceivers();
    }

    private void registerReceivers() {

        messageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra("data");
                if (data != null) {
                    try {
                        JSONObject obj = new JSONObject(data);

                        if (obj.has("cancel")) {
                            String me = UserPreferences.getInstance(VideoCallActivity.this, "UserPrefs").getUserDetails().getPhoneId();
                            String phoneId = obj.getString("to");
                            String cancel = obj.getString("cancel");
                            if (me.equals(phoneId)) {
                                if (cancel.equals("post")) {
                                    Toast.makeText(context, "Call Terminated", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else if (cancel.equals("pre")) {
                                    Toast.makeText(context, "Call Missed", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        } else {

                            String phoneId = UserPreferences.getInstance(VideoCallActivity.this, "UserPrefs").getUserDetails().getPhoneId();
                            if (phoneId.equals(obj.getString("to"))) {
                                String answer = obj.getString("answer");
                                if (answer.equals("decline")) {
                                    video_call_status.setText("Declined");
                                    Toast.makeText(context, "Call Declined", Toast.LENGTH_SHORT).show();
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    }, 500);
                                } else {
                                    isConnected = true;
                                    infoLayout.setVisibility(View.GONE);
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        registerReceiver("getMessage", messageReceiver);

    }

    public void answer(String callerId) throws JSONException {
        isConnected = true;
        client.sendMessage(callerId, "init", null);
        startCam();
    }

    public void call(String callId) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("call", true);
            jsonObject.put("mode", "video");
            jsonObject.put("to", to.getPhoneId());
            jsonObject.put("room_id", callId);
            jsonObject.put("caller", new Gson().toJson(UserPreferences.getInstance(this, "UserPrefs").getUserDetails()));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        if (!ResourcePlusApplication.mSocket.connected())
            ResourcePlusApplication.mSocket.connect();
        ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);

        startCam();
    }

    public void startCam() {
        client.start("android_webRTC");
    }

    private void setMicMuted(boolean state) {
//        int workingAudioMode = audioManager.getMode();

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (audioManager.isMicrophoneMute() != state) {
            audioManager.setMicrophoneMute(state);
        }
//        audioManager.setMode(workingAudioMode);
    }

    public void registerReceiver(String filterAction, BroadcastReceiver broadcastReceiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(filterAction);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
    }

    private void setWiredHeadsetStateReceiver() {
        wiredHeadsetStateReceiver = new WiredHeadsetStateReceiver(this);
        wiredHeadsetStateReceiver.addListener(this);
        registerReceiver(wiredHeadsetStateReceiver, new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));

    }

    private void setBluetoothStateReceiver() {
        bluetoothStateReceiver = new BluetoothStateReceiver(this);
        bluetoothStateReceiver.addListener(this);
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothStateReceiver, intentFilter);

    }

    private void pictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational rational = new Rational(vsv.getWidth(), vsv.getHeight());
            builder.setAspectRatio(rational).build();
            enterPictureInPictureMode(builder.build());

            pipLayout.setVisibility(View.GONE);
            infoLayout.setVisibility(View.GONE);
            videoControls.setVisibility(View.GONE);

        } else
            Log.d(TAG, "This device do not support pip mode");
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (!isInPictureInPictureMode()) {
                pictureInPictureMode();
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isInPictureInPictureMode()) {
                infoLayout.setVisibility(View.GONE);
                pipLayout.setVisibility(View.GONE);
                videoControls.setVisibility(View.GONE);
            } else {
                if (!isConnected)
                    infoLayout.setVisibility(View.VISIBLE);
                pipLayout.setVisibility(View.VISIBLE);
                videoControls.setVisibility(View.VISIBLE);
            }
        }
    }
}

package com.mr_w.resourceplus.activities;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.gson.Gson;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.databinding.ActivityAudioCallBinding;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.receivers.BluetoothStateReceiver;
import com.mr_w.resourceplus.receivers.WiredHeadsetStateReceiver;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Utils;
import com.mr_w.resourceplus.webRtc.PeerConnectionParameters;
import com.mr_w.resourceplus.webRtc.WebRtcClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.MediaStream;
import org.webrtc.VideoRendererGui;

import javax.inject.Inject;

public class AudioCallActivity extends AppCompatActivity implements View.OnClickListener,
        WebRtcClient.RtcListener,
        WiredHeadsetStateReceiver.WiredHeadsetStateReceiverListener,
        BluetoothStateReceiver.BluetoothStateReceiverListener,
        GestureDetector.OnGestureListener {

    private ActivityAudioCallBinding mBinding;
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private WebRtcClient client;
    private String mSocketAddress;
    private String callerId;
    private Users to;
    private boolean isMuted = false;
    private Handler handler;
    private Runnable runnable;
    private PowerManager.WakeLock wakeLock;
    private boolean isConnected = false;
    private AudioManager audioManager;
    private SensorEventListener proximitySensorEventListener;
    private SensorManager sensorManager;
    private BroadcastReceiver messageReceiver;
    private WiredHeadsetStateReceiver wiredHeadsetStateReceiver;
    private BluetoothStateReceiver bluetoothStateReceiver;
    private GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_audio_call);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        gestureDetector = new GestureDetector(this);

        callerId = getIntent().hasExtra("caller_id") ? getIntent().getStringExtra("caller_id") : null;
        to = getIntent().hasExtra("to") ? (Users) getIntent().getSerializableExtra("to") : null;
        boolean isInComing = getIntent().hasExtra("incoming") && getIntent().getBooleanExtra("incoming", false);
        mSocketAddress = "http://" + getResources().getString(R.string.host);
        mSocketAddress += (":" + getResources().getString(R.string.port) + "/");

        mBinding.name.setText(to.getName());
        if (to.getPhoto().equals("")) {
            mBinding.dp.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_pic));
        } else
            Glide.with(this).load(to.getPhoto()).into(mBinding.dp);

        if (isInComing) {
            animateAcceptButton();

            mBinding.hangupAudio.setVisibility(View.GONE);
            mBinding.layoutControls.setVisibility(View.GONE);
            mBinding.layoutIncomming.setVisibility(View.VISIBLE);
            mBinding.acceptIncomming.setVisibility(View.VISIBLE);
            mBinding.audioCallStatus.setText("Incoming Call");

            mBinding.acceptIncomming.setOnTouchListener((View view, MotionEvent motionEvent) -> !gestureDetector.onTouchEvent(motionEvent));

//            mBinding.acceptIncomming.setOnClickListener(v -> {
//
//                initializeListeners();
//
//                JSONObject answer = new JSONObject();
//                try {
//                    answer.put("answer", "accept");
//                    answer.put("to", to.getPhoneId());
//                } catch (JSONException exception) {
//                    exception.printStackTrace();
//                }
//
//                if (!ResourcePlusApplication.mSocket.connected())
//                    ResourcePlusApplication.mSocket.connect();
//                ResourcePlusApplication.mSocket.emit("sendMessage", answer);
//
//                mBinding.hangupAudio.setVisibility(View.VISIBLE);
//                mBinding.layoutControls.setVisibility(View.VISIBLE);
//                mBinding.layoutIncomming.setVisibility(View.GONE);
//            });

            mBinding.cancelWithReply.setOnClickListener(v -> {
//TODO
            });

            mBinding.cancelIncomming.setOnClickListener(v -> {
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
        } else
            initializeListeners();

        mBinding.hangupAudio.setOnClickListener(this);
        mBinding.btnMicOff.setOnClickListener(this);
        mBinding.btnCam.setOnClickListener(this);
        mBinding.btnVolume.setOnClickListener(this);

        registerReceivers();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setWiredHeadsetStateReceiver();
        setBluetoothStateReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (client != null) {
            client.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            client.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    protected void onDestroy() {
        isConnected = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
        if (sensorManager != null) {
            sensorManager.unregisterListener(proximitySensorEventListener);
            if (wakeLock.isHeld())
                wakeLock.release();
        }
        if (client != null) {
            client.onDestroy();
        }
        wiredHeadsetStateReceiver.removeListener(this);
        bluetoothStateReceiver.removeListener(this);
        setMicMuted(false);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.hangup_audio:
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

                if (handler != null)
                    handler.removeCallbacks(runnable);
                finish();
                break;
            case R.id.btnVolume:
                if (!audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(true);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    setVolumeControlStream(AudioManager.STREAM_MUSIC);
                    mBinding.btnVolume.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_speaker_on));
                } else {
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
                    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                    mBinding.btnVolume.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_speaker_off));
                }
                break;
            case R.id.btnCam:
                break;
            case R.id.btnBluetooth:
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter.isEnabled()) {
                    adapter.disable();
                } else {
                    adapter.enable();
                }
                break;
            case R.id.btnMicOff:
                if (isMuted) {
                    setMicMuted(false);
                    mBinding.btnMicOff.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_voice_call));
                    isMuted = false;
                } else {
                    setMicMuted(true);
                    mBinding.btnMicOff.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_voice_off));
                    isMuted = true;
                }
                break;
        }
    }

    @Override
    public void onHeadsetConnected() {
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onHeadsetDisconnected() {
        audioManager.setSpeakerphoneOn(false);
        audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    @Override
    public void onBluetoothConnected() {
        audioManager.setBluetoothScoOn(true);
        audioManager.setSpeakerphoneOn(false);
        audioManager.startBluetoothSco();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mBinding.btnBluetooth.setImageDrawable(ContextCompat.getDrawable(AudioCallActivity.this, R.drawable.ic_bluetooth_on));
    }

    @Override
    public void onBluetoothDisconnected() {
        audioManager.stopBluetoothSco();
        audioManager.setBluetoothScoOn(false);
        audioManager.setSpeakerphoneOn(false);
        audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        mBinding.btnBluetooth.setImageDrawable(ContextCompat.getDrawable(AudioCallActivity.this, R.drawable.ic_bluetooth_off));
    }

    @Override
    public void onCallReady(String callId) {
        if (callerId != null) {
            try {
                answer(callerId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            call(callId);
        }
    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocalStream(MediaStream localStream) {
    }

    @Override
    public void onAddRemoteStream(MediaStream remoteStream, int endPoint) {
    }

    @Override
    public void onRemoveRemoteStream(int endPoint) {
        if (handler != null)
            handler.removeCallbacks(runnable);
        finish();
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        float diffY = motionEvent1.getY() - motionEvent.getY();
        float diffX = motionEvent1.getX() - motionEvent.getX();
        if (Math.abs(diffX) < Math.abs(diffY)) {
            if (Math.abs(diffY) > 50 && Math.abs(v1) > 50) {
                if (diffY < 0)
                    onSwipeUp();
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private void initializeListeners() {

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        PeerConnectionParameters params = new PeerConnectionParameters(
                false, false, displaySize.x, displaySize.y, 30, 1, VIDEO_CODEC_VP9, true, 1, AUDIO_CODEC_OPUS, false);

        client = new WebRtcClient(this, mSocketAddress, params, VideoRendererGui.getEGLContext());

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        try {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, getLocalClassName());
        } catch (Throwable ignored) {
            Toast.makeText(this, "issue", Toast.LENGTH_SHORT).show();
        }

        proximitySensorEventListener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    if (event.values[0] == 0) {
                        if (!wakeLock.isHeld())
                            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
                    } else {
                        if (wakeLock.isHeld())
                            wakeLock.release();
                    }
                }
            }
        };

        if (proximitySensor != null) {
            sensorManager.registerListener(proximitySensorEventListener,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        setMicMuted(false);

    }

    public void answer(String callerId) throws JSONException {
        isConnected = true;
        client.sendMessage(callerId, "init", null);
        initializeResources();
        startTimer();
    }

    public void call(String callId) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("call", true);
            jsonObject.put("mode", "audio");
            jsonObject.put("to", to.getPhoneId());
            jsonObject.put("room_id", callId);
            jsonObject.put("caller", new Gson().toJson(UserPreferences.getInstance(this, "UserPrefs").getUserDetails()));
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

        if (!ResourcePlusApplication.mSocket.connected())
            ResourcePlusApplication.mSocket.connect();
        ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);

        initializeResources();
    }

    public void initializeResources() {
        client.start("audio_call");
    }

    private void setMicMuted(boolean state) {
//        int workingAudioMode = audioManager.getMode();

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        if (audioManager.isMicrophoneMute() != state) {
            audioManager.setMicrophoneMute(state);
        }
//        audioManager.setMode(workingAudioMode);
    }

    private void startTimer() {

        long startTime = System.currentTimeMillis();
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                long current = System.currentTimeMillis() - startTime;
                mBinding.audioCallStatus.setText(Utils.msToTimeConverter((int) current));
                handler.postDelayed(this, 1000);
            }
        };
        runOnUiThread(runnable);
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
                            String me = UserPreferences.getInstance(AudioCallActivity.this, "UserPrefs").getUserDetails().getPhoneId();
                            String phoneId = obj.getString("to");
                            String cancel = obj.getString("cancel");
                            if (me.equals(phoneId)) {
                                if (cancel.equals("post")) {
                                    Toast.makeText(context, "Call Terminated", Toast.LENGTH_SHORT).show();
                                    createCallLog();
                                    finish();
                                } else if (cancel.equals("pre")) {
                                    Toast.makeText(context, "Call Missed", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        } else {

                            String phoneId = UserPreferences.getInstance(AudioCallActivity.this, "UserPrefs").getUserDetails().getPhoneId();
                            if (phoneId.equals(obj.getString("to"))) {
                                String answer = obj.getString("answer");
                                if (answer.equals("decline")) {
                                    mBinding.audioCallStatus.setText("Declined");
                                    Toast.makeText(context, "Call Declined", Toast.LENGTH_SHORT).show();
                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    }, 500);
                                } else {
                                    isConnected = true;
                                    startTimer();
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

    private void createCallLog() {
        String postUrl = "http://51.89.165.55:3000/api/calls";

        JSONObject postData = new JSONObject();
        try {
            postData.put("userId", "");
            postData.put("callMode", "");
            postData.put("status", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean status = response.getBoolean("success");
                    if (status) {
                        //TODO
                    } else {
                        Toast.makeText(AudioCallActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("LOG_VOLLEY", String.valueOf(error));
                Toast.makeText(AudioCallActivity.this, "" + error, Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        int MY_SOCKET_TIMEOUT_MS = 50000;
        Volley.newRequestQueue(this).add(jsonObjectRequest).setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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

    private void onSwipeUp() {
        initializeListeners();

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

        mBinding.hangupAudio.setVisibility(View.VISIBLE);
        mBinding.layoutControls.setVisibility(View.VISIBLE);
        mBinding.layoutIncomming.setVisibility(View.GONE);
        mBinding.acceptIncomming.setVisibility(View.GONE);
    }

    private void animateAcceptButton() {
        ObjectAnimator animIncoming = ObjectAnimator.ofFloat(mBinding.acceptIncomming, "translationY", -100f, 0f);
        animIncoming.setDuration(1500);
        animIncoming.setInterpolator(new BounceInterpolator());
        animIncoming.setRepeatCount(20);
        animIncoming.start();
    }
}
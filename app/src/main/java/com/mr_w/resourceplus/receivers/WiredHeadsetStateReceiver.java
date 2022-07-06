package com.mr_w.resourceplus.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class WiredHeadsetStateReceiver extends BroadcastReceiver {

    private final AudioManager mManager;
    private List<WiredHeadsetStateReceiverListener> mListeners = new ArrayList<>();
    private boolean mConnected = false;

    public WiredHeadsetStateReceiver(Context context) {

        mManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        context.registerReceiver(this, intentFilter);
        checkStateChanged();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;

        if (checkStateChanged()) notifyStateToAll();
    }

    private boolean checkStateChanged() {

        AudioDeviceInfo[] devices = mManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        for (AudioDeviceInfo device : devices) {
            if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
                    || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                mConnected = true;
                return true;
            }
        }
        return false;
    }

    private void notifyStateToAll() {
        for (WiredHeadsetStateReceiverListener listener : mListeners) {
            notifyState(listener);
        }
    }

    private void notifyState(WiredHeadsetStateReceiverListener listener) {
        if (listener != null) {
            if (mConnected)
                listener.onHeadsetConnected();
            else
                listener.onHeadsetDisconnected();
        }
    }

    public void addListener(WiredHeadsetStateReceiverListener l) {
        mListeners.add(l);
        notifyState(l);
    }

    public void removeListener(WiredHeadsetStateReceiverListener l) {
        mListeners.remove(l);
    }

    public interface WiredHeadsetStateReceiverListener {
        void onHeadsetConnected();

        void onHeadsetDisconnected();
    }

}

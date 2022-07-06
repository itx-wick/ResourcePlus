package com.mr_w.resourceplus.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class BluetoothStateReceiver extends BroadcastReceiver {

    private final AudioManager mManager;
    private List<BluetoothStateReceiverListener> mListeners = new ArrayList<>();
    private boolean mConnected = false;

    public BluetoothStateReceiver(Context context) {

        mManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(this, intentFilter);
        checkStateChanged(null);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null)
            return;

        if (checkStateChanged(intent)) notifyStateToAll();
    }

    private boolean checkStateChanged(Intent intent) {

        if (intent != null) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                mConnected = true;
                return mConnected;
            }
        } else {
            AudioDeviceInfo[] devices = mManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
            for (AudioDeviceInfo device : devices) {
                if (device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                    mConnected = true;
                    return true;
                }
            }
        }
        return false;
    }

    private void notifyStateToAll() {
        for (BluetoothStateReceiverListener listener : mListeners) {
            notifyState(listener);
        }
    }

    private void notifyState(BluetoothStateReceiverListener listener) {
        if (listener != null) {
            if (mConnected)
                listener.onBluetoothConnected();
            else
                listener.onBluetoothDisconnected();
        }
    }

    public void addListener(BluetoothStateReceiverListener l) {
        mListeners.add(l);
        notifyState(l);
    }

    public void removeListener(BluetoothStateReceiverListener l) {
        mListeners.remove(l);
    }

    public interface BluetoothStateReceiverListener {
        void onBluetoothConnected();

        void onBluetoothDisconnected();
    }

}

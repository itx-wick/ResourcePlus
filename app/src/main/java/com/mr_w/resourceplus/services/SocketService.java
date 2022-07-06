package com.mr_w.resourceplus.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.mr_w.resourceplus.ResourcePlusApplication;

public class SocketService extends Service {

    public static final String ServiceIntent = "Socket_Service";
    Socket socket = ResourcePlusApplication.mSocket;
    Context context = this;
    private static boolean isRunning = false;

    Emitter.Listener typingListener = args -> {
        String data = args[0].toString();
        Intent i = new Intent("typing");
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    };

    Emitter.Listener receiveMessageListener = args -> {
        String data = args[0].toString();
        Intent i = new Intent("getMessage");
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    };

    Emitter.Listener seenMessageListener = args -> {
        String data = args[0].toString();
        Intent i = new Intent("seen");
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    };

    Emitter.Listener deliveredMessageListener = args -> {
        String data = args[0].toString();
        Intent i = new Intent("delivered");
        i.putExtra("data", data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        connectListeners();
        return START_NOT_STICKY;
    }

    private void connectListeners() {
        socket.on("isTyping", typingListener);
        socket.on("getMessage", receiveMessageListener);
        socket.on("seen", seenMessageListener);
        socket.on("delivered", deliveredMessageListener);
    }

    @Override
    public void onDestroy() {
        socket.off("isTyping");
        socket.off("getMessage");
        socket.off("seen");
        socket.off("delivered");
        socket.disconnect();
        isRunning = false;
        stopSelf();
        super.onDestroy();
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}

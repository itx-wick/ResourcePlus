package com.mr_w.resourceplus;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.mr_w.resourceplus.injections.di.component.AppComponent;
import com.mr_w.resourceplus.injections.di.component.DaggerAppComponent;
import com.mr_w.resourceplus.services.SocketService;

import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static com.mr_w.resourceplus.activities.splash_activity.SplashActivity.base;

//public class ResourcePlusApplication extends Application {
public class ResourcePlusApplication extends Application {

    public static final String TAG = ResourcePlusApplication.class
            .getSimpleName();
    private static Context context;
    public static Socket mSocket;
    private static CountDownTimer timer;
    private static boolean isTimerStart = false;

    static {
        try {
            mSocket = IO.socket(base());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public AppComponent appComponent;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build();

        appComponent.inject(this);
        AndroidNetworking.initialize(getApplicationContext());
//        if (BuildConfig.DEBUG) {
//            AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY);
//        }

        mSocket.connect();
        try {
            context.startService(new Intent(this, SocketService.class));
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
        startTimer();
//        MultiDex.install(this);
        handleSSLHandshake();
    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception exx) {
            exx.printStackTrace();
        }
    }

    public static void startTimer() {
        isTimerStart = true;
        timer = new CountDownTimer(60000, 2000) {
            public void onTick(long millisUntilFinished) {
                //Some code
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                Log.d("Timer", minutes + ":" + seconds);
                if (!SocketService.isRunning())
                    try {
                        ResourcePlusApplication.getContext()
                                .startService(new Intent(ResourcePlusApplication.getContext(), SocketService.class));
                    } catch (Exception e) {
                        Log.d(TAG, "onCreate: " + e.getMessage());
                    }
                else
                    Log.d(TAG, "onTick: Service is running");

            }

            public void onFinish() {
                timer.cancel();
                timer.start();
            }
        };
        timer.start();
    }

}


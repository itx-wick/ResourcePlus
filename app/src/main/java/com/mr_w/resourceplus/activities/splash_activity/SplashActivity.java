package com.mr_w.resourceplus.activities.splash_activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.mr_w.resourceplus.BR;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.start_activity.StartActivity;
import com.mr_w.resourceplus.databinding.ActivitySplashBinding;
import com.mr_w.resourceplus.injections.di.component.ActivityComponent;
import com.mr_w.resourceplus.injections.ui.base.BaseActivity;
import com.mr_w.resourceplus.utils.RequestPermissionsHelper;
import com.mr_w.resourceplus.utils.SweetAlertDialogs;

import org.jetbrains.annotations.NotNull;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SplashActivity extends BaseActivity<ActivitySplashBinding, SplashViewModel> implements SplashNavigator {

    public static final int PERMISSION_REQUEST_CODE = 0;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    public int getBindingVariable() {
        return BR.splash;
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    public void performDependencyInjection(ActivityComponent buildComponent) {
        buildComponent.inject(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel.setNavigator(this);
        requestPermission();
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = RequestPermissionsHelper.requestPermission(SplashActivity.this);
            if (permission == RequestPermissionsHelper.requestPermissionCode) {
                ActivityCompat.requestPermissions(SplashActivity.this, RequestPermissionsHelper.persmissionsStringArray, SplashActivity.PERMISSION_REQUEST_CODE);
            } else if (permission == RequestPermissionsHelper.notShouldShowRequestPermissionRationaleCode) {
                showDialogPermissionDenied();
            }
            if (permission == RequestPermissionsHelper.notCheckSelfPermissionCode) {
                processHandler();
            }
        } else {
            processHandler();
        }
    }

    private void processHandler() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                goForward();
            }
        }, 500);

    }

    public void showDialogPermissionDenied() {

        SweetAlertDialogs.getInstance().showDialogPermissionDenied(this, new SweetAlertDialogs.OnDialogClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions,
                                           @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SplashActivity.PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean granted = true;
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        granted = false;
                        break;
                    }
                }
                if (granted) {
                    processHandler();
                } else {
                    showDialogPermissionDenied();
                }
            } else {
                showDialogPermissionDenied();
            }
        }
    }

    public native static String base();

    public native static String baseAPIURL();

    @Override
    public void goForward() {
        startActivity(new Intent(SplashActivity.this, StartActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
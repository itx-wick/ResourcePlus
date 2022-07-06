package com.mr_w.resourceplus.activities.start_activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.fragments.main_fragment.MainFragment;
import com.mr_w.resourceplus.receivers.NetworkStateReceiver;
import com.mr_w.resourceplus.utils.ActiveActivitiesTracker;
import com.mr_w.resourceplus.utils.Utils;

public class StartActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    ConstraintLayout root;
    private Snackbar snackbar = null;
    private boolean isConnected;
    private NetworkStateReceiver networkStateReceiver = null;

    private void setNetworkStateReceiver() {
        networkStateReceiver = new NetworkStateReceiver(this);
        networkStateReceiver.addListener(this);
        registerReceiver(networkStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    public void onNetworkAvailable() {
        if (!isConnected)
            root.setEnabled(true);
        isConnected = true;
        snackbar.dismiss();
    }

    @Override
    public void onNetworkUnavailable() {
        isConnected = false;
        root.setEnabled(false);
        snackbar.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        root = findViewById(R.id.root);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            getWindow().setStatusBarColor(getResources().getColor(R.color.primaryDark, this.getTheme()));
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(getResources().getColor(R.color.primaryDark));
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isConnected = Utils.checkInternet(this);
        snackbar = Snackbar.make(findViewById(R.id.root), getString(R.string.connection_unavailable), Snackbar.LENGTH_INDEFINITE);
        setNetworkStateReceiver();
        ActiveActivitiesTracker.activityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        networkStateReceiver.removeListener(this);
        ActiveActivitiesTracker.activityStopped();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {

        Fragment navHostFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            Fragment nav = navHostFragment.getChildFragmentManager().getFragments().get(navHostFragment.getChildFragmentManager().getFragments().size() - 1);
            if (nav instanceof MainFragment) {
                if (!((MainFragment) nav).isKeyBoardShowing && ((MainFragment) nav).isLayoutShown) {
                    ((MainFragment) nav).hideSearchLayout();
                } else
                    super.onBackPressed();
            } else {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentBox);
                if (fragment != null) {
                    startActivity(new Intent(StartActivity.this, StartActivity.class));
                    finish();
                } else
//                    super.onBackPressed();
                    System.exit(0);
            }
        } else {
//            super.onBackPressed();
            System.exit(0);
        }
    }
}
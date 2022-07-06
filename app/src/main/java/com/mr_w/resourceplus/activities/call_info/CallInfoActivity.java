package com.mr_w.resourceplus.activities.call_info;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.databinding.ActivityCallInfoBinding;

public class CallInfoActivity extends AppCompatActivity {

    ActivityCallInfoBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_call_info);

        mBinding.btnBack.setOnClickListener(v -> {
            onBackPressed();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_call_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_message:
                break;
            case R.id.action_remove:
                break;
            case R.id.action_block:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
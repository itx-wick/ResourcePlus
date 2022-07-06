package com.mr_w.resourceplus.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mr_w.resourceplus.abstract_models.BackgroundCallbacks;
import com.mr_w.resourceplus.async.SyncContacts;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ContactsSyncService extends JobService {
    private static final String TAG = "ContactsSyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        return true;
    }

}
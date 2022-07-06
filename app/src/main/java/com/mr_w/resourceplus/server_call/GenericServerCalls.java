package com.mr_w.resourceplus.server_call;

import android.app.ProgressDialog;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.callbacks.GenericCallbacks;
import com.mr_w.resourceplus.injections.network.remote.ApiEndPoints;
import com.mr_w.resourceplus.utils.DataPart;
import com.mr_w.resourceplus.utils.VolleyMultipartRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.mr_w.resourceplus.utils.Utils.convert;

public class GenericServerCalls {

    private final Context context;
    private String url;
    private RequestQueue requestQueue;
    private int requestMethod;
    private boolean showProgress;
    private ProgressDialog dialog;

    public GenericServerCalls(Context context, String url, RequestQueue requestQueue, int requestMethod, boolean showProgress) {
        this.context = context;
        this.url = url;
        this.requestQueue = requestQueue;
        this.requestMethod = requestMethod;
        this.showProgress = showProgress;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRequestQueue(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }

    public void setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    private void showProgress() {
        if (dialog == null) {
            if (!showProgress)
                return;
            dialog = new ProgressDialog(context);
            dialog.show();
            dialog.setContentView(R.layout.progress_dialog);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public void hideProgress() {
        if (dialog != null && showProgress)
            if (dialog.isShowing())
                dialog.dismiss();
    }

    private String getErrorMessage(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "No connection";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);
                String message = response.getString("message");

                if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                } else if (networkResponse.statusCode == 401) {
                    errorMessage = message + " Please login again";
                } else if (networkResponse.statusCode == 400) {
                    errorMessage = message + " Check your inputs";
                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + " Something is getting wrong";
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return errorMessage;
    }

    public void jsonRequest(JSONObject data, GenericCallbacks<JSONObject> genericCallbacks) {

        showProgress();
        JsonObjectRequest request = new JsonObjectRequest(requestMethod, url, data, response -> {

            hideProgress();
            genericCallbacks.onJsonSuccess(response);

        }, error -> {

            hideProgress();
            String errorMessage = getErrorMessage(error);
            genericCallbacks.onFailure(errorMessage);
            error.printStackTrace();

        });

        int timeout = 30000;
        request.setRetryPolicy(new DefaultRetryPolicy(timeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag("JsonRequest");
        requestQueue.add(request);

    }

    public void stringRequest(Map<String, String> params, GenericCallbacks<String> genericCallbacks) {

        showProgress();
        final StringRequest request = new StringRequest(requestMethod, url, response -> {

            hideProgress();
            genericCallbacks.onStringSuccess(response);

        }, error -> {

            hideProgress();
            String errorMessage = getErrorMessage(error);
            genericCallbacks.onFailure(errorMessage);
            error.printStackTrace();

        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
        };

        int timeout = 30000;
        request.setRetryPolicy(new DefaultRetryPolicy(timeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag("StringRequest");
        requestQueue.add(request);

    }

    public void multiPartRequest(File file, GenericCallbacks<JSONObject> genericCallbacks) {

        showProgress();
        VolleyMultipartRequest request = new VolleyMultipartRequest(requestMethod, ApiEndPoints.ENDPOINT_FILE_UPLOAD,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        try {
                            JSONObject jsonObject = new JSONObject(new String(response.data));
                            genericCallbacks.onMultiPartSuccess(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        hideProgress();
                        String errorMessage = getErrorMessage(error);
                        genericCallbacks.onFailure(errorMessage);
                        error.printStackTrace();

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return new HashMap<>();
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    params.put("file", new DataPart(file.getPath().substring(file.getPath().lastIndexOf('/') + 1), convert(file.getPath()), "image/jpeg"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        int timeout = 60000;
        request.setRetryPolicy(new DefaultRetryPolicy(timeout,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag("MultiPartRequest");
        requestQueue.add(request);

    }

}

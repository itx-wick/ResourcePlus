package com.mr_w.resourceplus.utils;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.CharsetUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;


public class MultiPartRequest extends Request<String> {

    MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    HttpEntity httpentity;

    private final IMultipartProgressListener mProgressListener;
    private final Response.Listener<String> mListener;
    private final File mFiles;
    private HashMap<String, String> mBody;
    private long fileLength;

    public MultiPartRequest(String url, File mFiles,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener, IMultipartProgressListener progressListener) {

        super(Method.POST, url, errorListener);

        this.mListener = listener;
        this.mFiles = mFiles;
        this.fileLength = getFileLength();
        this.mProgressListener = progressListener;
        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        try {
            entity.setCharset(CharsetUtils.get("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        buildMultipartEntity();
        httpentity = entity.build();
    }

    private void buildMultipartEntity() {
        entity.addPart("file", new FileBody(mFiles));
    }

    private long getFileLength() {
        long lgth = 0;
        lgth += mFiles.length();
        Log.e("lgth = ", Long.toString(lgth));
        System.out.println("lgth = " + lgth);
        return lgth;
    }

    @Override
    public String getBodyContentType() {
        return httpentity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            //httpentity.writeTo(bos);
            httpentity.writeTo(new CountingOutputStream(bos, fileLength,
                    mProgressListener));
        } catch (IOException e) {
            VolleyLog.e(e.toString());
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {

        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            return Response.success(jsonString,
                    HttpHeaderParser.parseCacheHeaders(response));

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    public interface IMultipartProgressListener {
        void transferred(long transferred, int progress);
    }


    public static class CountingOutputStream extends FilterOutputStream {
        private final MultiPartRequest.IMultipartProgressListener progressListener;
        private long transferred;
        private final long fileLength;

        public CountingOutputStream(final OutputStream out, long fileLength,
                                    final IMultipartProgressListener listener) {
            super(out);
            this.fileLength = fileLength;
            this.progressListener = listener;
            this.transferred = 0;
        }

        public void write(byte[] buffer, int offset, int length) throws IOException {
            out.write(buffer, offset, length);
            if (progressListener != null) {
                this.transferred += length;
                Log.e("Transferred", String.valueOf(this.transferred));
                int progress = (int) ((this.transferred * 100.0f) / fileLength);
                this.progressListener.transferred(this.transferred, progress);
            }
        }

        public void write(int oneByte) throws IOException {
            out.write(oneByte);
            if (progressListener != null) {
                this.transferred++;
                int progress = (int) ((transferred * 100.0f) / fileLength);
                Log.e("Transferred1", String.valueOf(this.transferred));
                this.progressListener.transferred(this.transferred, progress);
            }
        }
    }

}

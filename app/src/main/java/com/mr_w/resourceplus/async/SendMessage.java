package com.mr_w.resourceplus.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.callbacks.GenericCallbacks;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.injections.network.remote.ApiEndPoints;
import com.mr_w.resourceplus.model.Conversation;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.server_call.GenericServerCalls;
import com.mr_w.resourceplus.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class SendMessage extends AsyncTask<String, String, String> {

    private final Context context;
    private MessageData messageData;
    private final Database db;
    private File file;
    private int pos;
    private boolean isNewThread;
    private Conversation conversation;
    private RequestQueue fileUploadRequest;
    private GenericServerCalls genericServerCalls;

    public void setNewThread(boolean newThread) {
        isNewThread = newThread;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public void setMessageData(MessageData messageData) {
        this.messageData = messageData;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public SendMessage(Context context) {
        this.context = context;
        db = Database.getInstance(context);
    }

    public static SendMessage getInstance(Context context) {
        return new SendMessage(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        fileUploadRequest = Volley.newRequestQueue(context);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            if (file == null) {
                apiCall(messageData);
            } else {
                uploadFile(file, messageData);
            }

            return "good";
        } catch (Exception e) {
            cancelCall();
            Intent i = new Intent("message_sent");
            i.putExtra("success", false);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            return null;
        }
    }

    public void uploadFile(File file, MessageData message) {

        genericServerCalls = new GenericServerCalls(context,
                ApiEndPoints.ENDPOINT_FILE_UPLOAD,
                fileUploadRequest,
                Request.Method.POST,
                false);
        genericServerCalls.multiPartRequest(file, new GenericCallbacks<JSONObject>() {

            @Override
            public void onMultiPartSuccess(JSONObject response) {
                super.onMultiPartSuccess(response);

                try {
                    boolean status = response.getBoolean("success");
                    if (status) {
                        String path = response.getString("path");
                        double size = response.getDouble("size");

                        String fileName = path.substring(path.lastIndexOf('/') + 1);
                        String directoryPath = Utils.getDirectoryPath(fileName);
                        File directory = new File(directoryPath);
                        if (!directory.exists())
                            directory.mkdirs();
                        File newFile = new File(directoryPath + fileName);

                        Utils.copyFileUsingStream(file, newFile);
                        message.setFileSize(String.valueOf(size));
                        message.setUrl(path);
                        apiCall(message);
                    } else {
                        Toast.makeText(context, response.getString("msg"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String message1) {
                message.setStatus("cancelled");
                saveToLocalDB(message);
                conversation.setMessage(message);
                updateConversation();
                Toast.makeText(context, message1, Toast.LENGTH_LONG).show();

                Intent i = new Intent("message_failed");
                i.putExtra("data", pos);
                i.putExtra("success", false);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }
        });

    }

    private void apiCall(MessageData message) {

        JSONObject postData = new JSONObject();
        try {
            postData.put("sender", message.getSenderPhoneId());
            postData.put("type", message.getType().toString());
            postData.put("status", "send");
            postData.put("conversationId", message.getConversationId());
            if (message.getType().equals(MessageData.TYPE.TEXT) || message.getType().equals(MessageData.TYPE.INFO) || message.getType().equals(MessageData.TYPE.LINK))
                postData.put("text", message.getText());
            else {
                postData.put("url", message.getUrl());
                postData.put("fileSize", message.getFileSize());
                if (message.getText() != null)
                    if (!message.getText().isEmpty())
                        postData.put("text", message.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (genericServerCalls == null) {
            genericServerCalls = new GenericServerCalls(context,
                    ApiEndPoints.ENDPOINT_MESSAGES_CREATE,
                    fileUploadRequest,
                    Request.Method.POST,
                    false);
        } else {
//            fileUploadRequest = Volley.newRequestQueue(context);
            genericServerCalls.setUrl(ApiEndPoints.ENDPOINT_MESSAGES_CREATE);
//            genericServerCalls.setRequestQueue(fileUploadRequest);
            genericServerCalls.setShowProgress(false);
        }

        genericServerCalls.jsonRequest(postData, new GenericCallbacks<JSONObject>() {

            @Override
            public void onJsonSuccess(JSONObject response) {
                super.onJsonSuccess(response);

                try {
                    int code = response.getInt("status");
                    if (code == 200) {

                        db.deleteMessage(message.getId());
                        message.setStatus("send");
                        message.setId(response.getString("_id"));
                        message.setDateTime(response.getString("createdAt"));
                        conversation.setMessage(message);

                        if (isNewThread) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("conversation", new Gson().toJson(conversation));
                            jsonObject.put("type2", "new_conversation");

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", jsonObject);
                            isNewThread = false;
                        } else {

                            postData.put("_id", message.getId());
                            postData.put("dateTime", message.getDateTime());
                            postData.put("conversation", new Gson().toJson(conversation));

                            if (!ResourcePlusApplication.mSocket.connected())
                                ResourcePlusApplication.mSocket.connect();
                            ResourcePlusApplication.mSocket.emit("sendMessage", postData);
                        }

                        saveToLocalDB(message);
                        updateConversation();

                        Intent i = new Intent("message_sent");
                        i.putExtra("data", pos);
                        i.putExtra("success", true);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(i);

                        fileUploadRequest = null;
                    } else {
                        Toast.makeText(context, response.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String message1) {
                message.setStatus("cancelled");
                saveToLocalDB(message);
                conversation.setMessage(message);
                updateConversation();
                Toast.makeText(context, message1, Toast.LENGTH_LONG).show();

                Intent i = new Intent("message_failed");
                i.putExtra("data", pos);
                i.putExtra("success", false);
                LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            }
        });
    }

    private void saveToLocalDB(MessageData message) {
        if (!db.isMessageExists(message.getId()))
            db.insertMessage(message);
        else db.updateMessage(message);
    }

    private void updateConversation() {
        if (!db.isConversationExists(conversation.get_id()))
            db.insertConversation(conversation);
        else db.updateConversation(conversation);
    }

    public void cancelCall() {
        if (fileUploadRequest == null)
            return;

        fileUploadRequest.cancelAll("MultiPartRequest");
        fileUploadRequest.cancelAll("JsonRequest");
        messageData.setStatus("cancelled");
        if (messageData.getId() == null || messageData.getId().equals(""))
            messageData.setId(UUID.randomUUID().toString());
        saveToLocalDB(messageData);
        conversation.setMessage(messageData);
    }

}

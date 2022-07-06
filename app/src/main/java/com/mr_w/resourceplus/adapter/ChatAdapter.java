package com.mr_w.resourceplus.adapter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.injections.data.DataManager;
import com.mr_w.resourceplus.interfaces.DownloadListener;
import com.mr_w.resourceplus.model.Link;
import com.mr_w.resourceplus.model.LocalContacts;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.model.users.Users;
import com.mr_w.resourceplus.storage.UserPreferences;
import com.mr_w.resourceplus.utils.Utils;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    String conversationType;
    Activity activity;
    private int playBackPosition = 0;
    private List<MessageData> mMessageData;
    private MediaPlayer mediaPlayer;
    ImageViewer imageViewer;
    GetProfile getProfile;
    UploadMessage uploadMessage;
    private DownloadFile downloader;
    private Handler handler;
    private int count = 0;
    private ScrollToLast scrollToLast;
    private UpdateMessage updateMessage;
    private CancelSendingFiles cancelSendingFiles;
    private Link link;
    private int performBackgroundTransistion = -1;

    private final Database db;
    private LinearLayout.LayoutParams params;
    private boolean isRight;
    private MessageData data;

    private DataManager preferences;

    public void setPerformBackgroundTransition(int performBackgroundTransistion) {
        this.performBackgroundTransistion = performBackgroundTransistion;
    }

    public void setPreferences(DataManager preferences) {
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {

        data = mMessageData.get(position);

        if (performBackgroundTransistion != -1) {
            if (position == performBackgroundTransistion) {
                holder.itemView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorSelection));
                new Handler().postDelayed(() -> {
                    holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    performBackgroundTransistion = -1;
                }, 400);
            }
        }

        isRight = preferences.getUserDetails().getPhoneId().equals(data.getSenderPhoneId());

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.START;

        if (data.getType().equals(MessageData.TYPE.TEXT)) {
            handleTextMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.CONTACT)) {
            handleContactMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.GIF)) {
            handleGifMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.INFO)) {
            handleInfoMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.LINK)) {
            handleLinkMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.DOCUMENTS)) {
            handleDocumentMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.PICTURE)) {
            handleImageMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.VIDEO)) {
            handleVideoMessages(holder, position);
        } else if (data.getType().equals(MessageData.TYPE.AUDIO)) {
            handleAudioMessages(holder, position);
        }

        if (!isRight) {
            holder.msgLayout.setGravity(Gravity.START);

            if (conversationType.equals("group")) {
                if (data.getType().equals(MessageData.TYPE.INFO))
                    return;
                holder.nameTv.setVisibility(View.VISIBLE);
                Users obj = getProfile.getUser(data.getSenderPhoneId());
                holder.nameTv.setText(String.format(activity.getString(R.string.stringCon), obj.getName()));
            }
        } else {
            holder.msgLayout.setGravity(Gravity.END);
            holder.layoutText.setBackgroundResource(R.drawable.bubble_right);
            holder.textMessage.setTextColor(Color.WHITE);
            holder.timeTv.setTextColor(Color.WHITE);
        }
    }

    private void handleInfoMessages(ViewHolder holder, int position) {
        holder.layoutText.setVisibility(View.GONE);
        holder.layoutInfo.setVisibility(View.VISIBLE);

        String[] splits = data.getText().split("/");
        String concludedInfo = "";

        if (splits.length == 2) {
            String number = splits[0].trim();
            if (preferences.getUserDetails().getPhoneNo().equals(number))
                concludedInfo = "You " + splits[1];
            else {
                LocalContacts contact = db.getContact(number);
                if (contact == null)
                    concludedInfo = number + " " + splits[1];
                else
                    concludedInfo = contact.getName() + " " + splits[1];
            }
        } else if (splits.length == 3) {
            String number = splits[0].trim();
            String otherNumbers = splits[2].trim();
            LocalContacts firstContact = db.getContact(number);
            if (!otherNumbers.contains(",")) {
                LocalContacts secondContact = db.getContact(otherNumbers);
                if (firstContact == null && secondContact == null) {
                    concludedInfo = number + " " + splits[1] + " " + otherNumbers;
                } else if (firstContact == null) {
                    if (preferences.getUserDetails().getPhoneNo().equals(otherNumbers)) {
                        concludedInfo = number + " " + splits[1] + " You";
                    } else {
                        concludedInfo = number + " " + splits[1] + " " + secondContact.getName();
                    }
                } else if (secondContact == null) {
                    if (preferences.getUserDetails().getPhoneNo().equals(number)) {
                        concludedInfo = "You " + splits[1] + " " + otherNumbers;
                    } else {
                        concludedInfo = firstContact.getName() + " " + splits[1] + " " + otherNumbers;
                    }
                } else {
                    if (preferences.getUserDetails().getPhoneNo().equals(number)) {
                        concludedInfo = "You " + splits[1] + " " + secondContact.getName();
                    } else if (preferences.getUserDetails().getPhoneNo().equals(otherNumbers)) {
                        concludedInfo = firstContact.getName() + " " + splits[1] + " You";
                    } else {
                        concludedInfo = firstContact.getName() + " " + splits[1] + " " + secondContact.getName();
                    }
                }
            } else {

                if (firstContact != null) {
                    if (preferences.getUserDetails().getPhoneNo().equals(number)) {
                        concludedInfo = "You " + splits[1] + " ";
                    } else
                        concludedInfo = firstContact.getName() + " " + splits[1] + " ";
                } else {
                    if (preferences.getUserDetails().getPhoneNo().equals(number)) {
                        concludedInfo = "You " + splits[1] + " ";
                    } else
                        concludedInfo = number + " " + splits[1] + " ";
                }

                StringBuilder subValue = new StringBuilder();
                String[] numbers = otherNumbers.split(",");
                for (int i = 0; i < numbers.length; i++) {
                    LocalContacts check = db.getContact(numbers[i]);
                    if (check != null) {
                        if (numbers[i].equals(preferences.getUserDetails().getPhoneNo()))
                            subValue.append("You");
                        else
                            subValue.append(check.getName());
                    } else {
                        subValue.append(numbers[i]);
                    }
                    if (i < numbers.length - 1)
                        subValue.append(", ");
                }
                concludedInfo = concludedInfo + subValue.toString().trim();
            }
        } else if (splits.length == 4) {
            String firstNumber = splits[0].trim();
            String secondNumber = splits[2].trim();
            LocalContacts firstContact = db.getContact(firstNumber);
            LocalContacts secondContact = db.getContact(secondNumber);
            if (firstContact == null && secondContact == null) {
                concludedInfo = firstNumber + " " + splits[1] + " " + secondNumber + " " + splits[3];
            } else if (firstContact == null) {
                if (preferences.getUserDetails().getPhoneNo().equals(secondNumber)) {
                    concludedInfo = firstNumber + " " + splits[1] + " You " + splits[3];
                } else {
                    concludedInfo = firstNumber + " " + splits[1] + " " + secondContact.getName() + " " + splits[3];
                }
            } else if (secondContact == null) {
                if (preferences.getUserDetails().getPhoneNo().equals(firstNumber)) {
                    concludedInfo = "You " + splits[1] + " " + secondNumber + " " + splits[3];
                } else {
                    concludedInfo = firstContact.getName() + " " + splits[1] + " " + secondNumber + " " + splits[3];
                }
            } else {
                if (preferences.getUserDetails().getPhoneNo().equals(firstNumber)) {
                    concludedInfo = "You " + splits[1] + " " + secondContact.getName() + " " + splits[3];
                } else if (preferences.getUserDetails().getPhoneNo().equals(secondNumber)) {
                    concludedInfo = firstContact.getName() + " " + splits[1] + " You " + splits[3];
                } else {
                    concludedInfo = firstContact.getName() + " " + splits[1] + " " + secondContact.getName() + " " + splits[3];
                }
            }
        }

        holder.tvInfo.setText(concludedInfo);
    }

    private void handleAudioMessages(ViewHolder holder, int position) {

        holder.layoutVoice.setVisibility(View.VISIBLE);
        holder.layoutText.setVisibility(View.GONE);

        if (data.getStatus().equals("sending")) {
            holder.sendAudioLayout.setVisibility(View.VISIBLE);
            holder.timeAudio.setVisibility(View.GONE);
            holder.statusAudio.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            uploadMessage.uploadMessage(position, data);

            holder.cancelSendAudio.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendAudioLayout.setVisibility(View.GONE);
                holder.retryA.setVisibility(View.VISIBLE);

                holder.retryA.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.sendAudioLayout.setVisibility(View.VISIBLE);
                        holder.retryA.setVisibility(View.GONE);
                        uploadMessage.uploadMessage(position, data);
                    }
                });
            });

        } else if (data.getStatus().equals("cancelled")) {
            holder.retryA.setVisibility(View.VISIBLE);
            holder.timeAudio.setVisibility(View.GONE);
            holder.statusAudio.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));

            holder.retryA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.sendAudioLayout.setVisibility(View.VISIBLE);
                    holder.retryA.setVisibility(View.GONE);
                    uploadMessage.uploadMessage(position, data);
                }
            });

            holder.cancelSendAudio.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendAudioLayout.setVisibility(View.GONE);
                holder.retryA.setVisibility(View.VISIBLE);
            });
        } else {

            holder.sendAudioLayout.setVisibility(View.GONE);
            holder.timeAudio.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutVoice.setBackgroundResource(R.drawable.bg_layout_round_white);
                params = new LinearLayout.LayoutParams((int) activity.getResources().getDimension(R.dimen.audio_view_width), LinearLayout.LayoutParams.WRAP_CONTENT);
                holder.layoutVoice.setLayoutParams(params);
                holder.timeAudio.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusAudio.setVisibility(View.GONE);
                holder.seekBarAudio.setProgressDrawable(ContextCompat.getDrawable(activity, R.drawable.progress_bar_left));
                holder.btnPause.setColorFilter(ContextCompat.getColor(activity, R.color.primary), PorterDuff.Mode.SRC_IN);
                holder.btnPlay.setColorFilter(ContextCompat.getColor(activity, R.color.primary), PorterDuff.Mode.SRC_IN);
                holder.downloadAudio.setColorFilter(ContextCompat.getColor(activity, R.color.primary), PorterDuff.Mode.SRC_IN);
                holder.liveDuration.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.tDuration.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.seperator.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.audioFileSize.setTextColor(ContextCompat.getColor(activity, R.color.black));
            }

            holder.btnPlay.setVisibility(View.VISIBLE);
            holder.timeAudio.setText(getTime(data.getDateTime()));

            switch (data.getStatus()) {
                case "send":
                    holder.statusAudio.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusAudio.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusAudio.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusAudio.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }

            boolean isPresent = Utils.isFilePresent(data.getUrl());
            final String[] fullPath = {null};

            if (isPresent) {
                String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                fullPath[0] = Utils.getDirectoryPath(filename) + filename;
                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(fullPath[0]);
                String duration =
                        metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                int totalDuration = Integer.parseInt(duration);
                String tDuration = Utils.msToTimeConverter(totalDuration);
                holder.tDuration.setText(tDuration);
                metaRetriever.release();

                holder.layoutTimerAudio.setVisibility(View.VISIBLE);
            } else {
                holder.downloadAudio.setVisibility(View.VISIBLE);
                holder.btnPlay.setVisibility(View.GONE);
                holder.layoutTimerAudio.setVisibility(View.GONE);
                double fileSize = Double.parseDouble(data.getFileSize()) / 1024.0;
                holder.audioFileSize.setText(String.format(activity.getString(R.string.mb), fileSize));
                holder.audioFileSize.setVisibility(View.VISIBLE);
            }

            String finalFullPath = fullPath[0];
            holder.btnPlay.setOnClickListener(v -> {

                holder.btnPlay.setVisibility(View.GONE);
                holder.btnPause.setVisibility(View.VISIBLE);

                try {
                    if (playBackPosition != 0) {
                        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                            mediaPlayer.seekTo(playBackPosition);
                            mediaPlayer.start();
                        }
                    } else if (isPresent)
                        playAudio(finalFullPath, holder);
                    else
                        playAudio(data.getUrl(), holder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            holder.btnPause.setOnClickListener(v -> {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    playBackPosition = mediaPlayer.getCurrentPosition();
                    mediaPlayer.pause();

                    holder.btnPlay.setVisibility(View.VISIBLE);
                    holder.btnPause.setVisibility(View.GONE);
                }
            });

            holder.downloadAudio.setOnClickListener(v -> {
                downloader = new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
                    @Override
                    public void onPreExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderAudio.setVisibility(View.VISIBLE);
                                holder.downloadAudio.setVisibility(View.GONE);
                                holder.audioFileSize.setVisibility(View.INVISIBLE);
                                holder.progressBarAudio.setSecondaryProgress(0);
                            }
                        });
                        super.onPreExecute();
                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.progressBarAudio.setSecondaryProgress(progress);
                            }
                        });
                        super.onProgressUpdate(progress);
                    }

                    @Override
                    public void onFailure() {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                holder.loaderAudio.setVisibility(View.GONE);
                                holder.downloadAudio.setVisibility(View.VISIBLE);
                                try {
                                    String filename = FilenameUtils.getName(data.getUrl());
                                    File file = new File(Utils.getDirectoryPath(filename) + filename);
                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        super.onFailure();
                    }

                    @Override
                    public void onPostExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderAudio.setVisibility(View.GONE);
                                holder.btnPlay.setVisibility(View.VISIBLE);
                                notifyItemChanged(position);
                            }
                        });
                        super.onPostExecute();
                    }

                });
                downloader.setContext(activity);
                downloader.execute();
            });

            holder.cancelAudio.setOnClickListener(v -> {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloader.cancel(true);
                        holder.loaderAudio.setVisibility(View.GONE);
                        holder.downloadAudio.setVisibility(View.VISIBLE);
                        String filename = FilenameUtils.getName(data.getUrl());
                        File file = new File(Utils.getDirectoryPath(filename) + filename);
                        file.delete();
                    }
                });
            });
        }
    }

    private void handleVideoMessages(ViewHolder holder, int position) {

        holder.layoutImage.setVisibility(View.VISIBLE);
        holder.layoutText.setVisibility(View.GONE);

        if (data.getStatus().equals("sending")) {
            holder.sendVideoLayout.setVisibility(View.VISIBLE);
            holder.timeImg.setVisibility(View.GONE);
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(data.getUrl(), MediaStore.Video.Thumbnails.MINI_KIND);
            holder.imageMessage.setImageBitmap(bitmap);
            uploadMessage.uploadMessage(position, data);

            holder.cancelSendImgVideo.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendVideoLayout.setVisibility(View.GONE);
                holder.retryIV.setVisibility(View.VISIBLE);

                holder.retryIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.retryIV.setVisibility(View.GONE);
                        holder.sendVideoLayout.setVisibility(View.VISIBLE);

                        uploadMessage.uploadMessage(position, data);
                    }
                });
            });

        } else if (data.getStatus().equals("cancelled")) {
            holder.retryIV.setVisibility(View.VISIBLE);
            holder.timeImg.setVisibility(View.GONE);
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            Uri uri = null;
            String path = "";
            try {
                URL url = new URL(data.getUrl());
                if (Utils.isFilePresent(data.getUrl())) {
                    String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    path = Utils.getDirectoryPath(filename) + filename;
                    uri = Uri.fromFile(new File(path));
                }
            } catch (Exception e) {
                uri = Uri.parse(data.getUrl());
                path = uri.getPath();
            }
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);

            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(path);
            String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            holder.videoDuration.setText(Utils.msToTimeConverter(Integer.parseInt(duration)));
            metaRetriever.release();

            holder.videoDurationLayout.setVisibility(View.VISIBLE);
            holder.imageMessage.setImageBitmap(bitmap);

            holder.retryIV.setOnClickListener(v -> {
                holder.retryIV.setVisibility(View.GONE);
                holder.sendVideoLayout.setVisibility(View.VISIBLE);

                uploadMessage.uploadMessage(position, data);
            });

            holder.cancelSendImgVideo.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendVideoLayout.setVisibility(View.GONE);
                holder.retryIV.setVisibility(View.VISIBLE);
            });


        } else {
            holder.sendVideoLayout.setVisibility(View.GONE);
            holder.timeImg.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutImage.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                holder.layoutImage.setLayoutParams(params);
                holder.timeImg.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusImg.setVisibility(View.GONE);
            }
            holder.playVideo.setVisibility(View.VISIBLE);
            holder.timeImg.setText(getTime(data.getDateTime()));

            boolean isPresent = Utils.isFilePresent(data.getUrl());

            if (isPresent) {
                String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                String path = Utils.getDirectoryPath(filename) + filename;
                Uri uri = Uri.fromFile(new File(path));
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);

                MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
                metaRetriever.setDataSource(path);
                String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                holder.videoDuration.setText(Utils.msToTimeConverter(Integer.parseInt(duration)));
                metaRetriever.release();

                holder.videoDurationLayout.setVisibility(View.VISIBLE);
                holder.imageMessage.setImageBitmap(bitmap);
            } else {
                holder.imageMessage.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.blur));
                double fileSize = Double.parseDouble(data.getFileSize()) / 1024.0;
                holder.fileSizeVideo.setText(String.format(activity.getString(R.string.mb), fileSize));
                holder.downloadVideo.setVisibility(View.VISIBLE);
            }

            switch (data.getStatus()) {
                case "send":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusImg.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }

            holder.imageMessage.setOnClickListener(v -> {
                if (isPresent) {
                    String fileName = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    if (Utils.isFilePresent(fileName)) {
                        File file = new File(Utils.getDirectoryPath(fileName) + fileName);
                        Utils.openFile(file, activity);
                    }
                }
            });

            holder.playVideo.setOnClickListener(v -> {
                if (isPresent) {
                    String fileName = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    if (Utils.isFilePresent(data.getUrl())) {
                        File file = new File(Utils.getDirectoryPath(fileName) + fileName);
                        Utils.openFile(file, activity);
                    }
                }
            });

            holder.downloadVideo.setOnClickListener(v -> {
                downloader = new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
                    @Override
                    public void onPreExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.VISIBLE);
                                holder.downloadVideo.setVisibility(View.GONE);
                                holder.progressBarVideo.setSecondaryProgress(0);
                            }
                        });
                        super.onPreExecute();
                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.progressBarVideo.setSecondaryProgress(progress);
                            }
                        });
                        super.onProgressUpdate(progress);
                    }

                    @Override
                    public void onFailure() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.GONE);
                                holder.downloadVideo.setVisibility(View.VISIBLE);
                                try {
                                    String filename = FilenameUtils.getName(data.getUrl());
                                    File file = new File(Utils.getDirectoryPath(filename) + filename);
                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        super.onFailure();
                    }

                    @Override
                    public void onPostExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.GONE);
                                notifyItemChanged(position);
                            }
                        });
                        super.onPostExecute();
                    }

                });
                downloader.setContext(activity);
                downloader.execute();
            });

            holder.cancelVideo.setOnClickListener(v -> {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloader.cancel(true);
                        holder.loaderVideo.setVisibility(View.GONE);
                        holder.downloadVideo.setVisibility(View.VISIBLE);
                        String filename = FilenameUtils.getName(data.getUrl());
                        File file = new File(Utils.getDirectoryPath(filename) + filename);
                        file.delete();
                    }
                });
            });
        }
    }

    private void handleImageMessages(ViewHolder holder, int position) {

        holder.layoutImage.setVisibility(View.VISIBLE);
        holder.layoutText.setVisibility(View.GONE);
        if (data.getStatus().equals("sending")) {
            holder.sendVideoLayout.setVisibility(View.VISIBLE);
            holder.timeImg.setVisibility(View.GONE);
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            Uri uri = Uri.parse(data.getUrl());
            holder.imageMessage.setImageURI(uri);
            uploadMessage.uploadMessage(position, data);

            holder.cancelSendImgVideo.setOnClickListener(v -> {
                holder.retryIV.setVisibility(View.VISIBLE);
                cancelSendingFiles.onCancel(position);
                holder.sendVideoLayout.setVisibility(View.GONE);

                holder.retryIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.retryIV.setVisibility(View.GONE);
                        holder.sendVideoLayout.setVisibility(View.VISIBLE);

                        uploadMessage.uploadMessage(position, data);
                    }
                });
            });

        } else if (data.getStatus().equals("cancelled")) {
            holder.retryIV.setVisibility(View.VISIBLE);
            holder.timeImg.setVisibility(View.GONE);
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            try {
                URL url = new URL(data.getUrl());
                if (Utils.isFilePresent(data.getUrl())) {
                    String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    Uri uri = Uri.fromFile(new File(Utils.getDirectoryPath(filename) + filename));
                    holder.imageMessage.setImageURI(uri);
                }
            } catch (Exception e) {
                Uri uri = Uri.parse(data.getUrl());
                holder.imageMessage.setImageURI(uri);
            }

            holder.retryIV.setOnClickListener(v -> {
                holder.retryIV.setVisibility(View.GONE);
                holder.sendVideoLayout.setVisibility(View.VISIBLE);

                uploadMessage.uploadMessage(position, data);
            });

            holder.cancelSendImgVideo.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendVideoLayout.setVisibility(View.GONE);
                holder.retryIV.setVisibility(View.VISIBLE);
            });


        } else {

            holder.sendVideoLayout.setVisibility(View.GONE);
            holder.timeImg.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutImage.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                holder.layoutImage.setLayoutParams(params);
                holder.timeImg.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusImg.setVisibility(View.GONE);
            }

            boolean isPresent = Utils.isFilePresent(data.getUrl());
            if (isPresent) {
                String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                Uri uri = Uri.fromFile(new File(Utils.getDirectoryPath(filename) + filename));
                holder.imageMessage.setImageURI(uri);
            } else {
                holder.imageMessage.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.blur));
                double fileSize = Double.parseDouble(data.getFileSize()) / 1024.0;
                holder.fileSizeVideo.setText(String.format(activity.getString(R.string.mb), fileSize));
                holder.downloadVideo.setVisibility(View.VISIBLE);

//                    Glide.with(activity)
//                            .load(data.getUrl())
//                            .into(holder.imageMessage);
//                    new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
//                        @Override
//                        public void onPostExecute() {
//                            super.onPostExecute();
//                        }
//                    }).execute();
            }

            holder.timeImg.setText(getTime(data.getDateTime()));

            switch (data.getStatus()) {
                case "send":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusImg.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }

            holder.imageMessage.setOnClickListener(v -> {
                if (isPresent) {
                    String fileName = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    File file = new File(Utils.getDirectoryPath(fileName) + fileName);
//                            Utils.openFile(file, activity);
                    imageViewer.viewImage(FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file).toString());
                }
            });

            holder.downloadVideo.setOnClickListener(v -> {
                downloader = new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
                    @Override
                    public void onPreExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.VISIBLE);
                                holder.downloadVideo.setVisibility(View.GONE);
                                holder.progressBarVideo.setSecondaryProgress(0);
                            }
                        });
                        super.onPreExecute();
                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.progressBarVideo.setSecondaryProgress(progress);
                            }
                        });
                        super.onProgressUpdate(progress);
                    }

                    @Override
                    public void onFailure() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.GONE);
                                holder.downloadVideo.setVisibility(View.VISIBLE);
                                try {
                                    String filename = FilenameUtils.getName(data.getUrl());
                                    File file = new File(Utils.getDirectoryPath(filename) + filename);
                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        super.onFailure();
                    }

                    @Override
                    public void onPostExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.GONE);
                                notifyItemChanged(position);
                            }
                        });
                        super.onPostExecute();
                    }

                });
                downloader.setContext(activity);
                downloader.execute();
            });

            holder.cancelVideo.setOnClickListener(v -> {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloader.cancel(true);
                        holder.loaderVideo.setVisibility(View.GONE);
                        holder.downloadVideo.setVisibility(View.VISIBLE);
                        String filename = FilenameUtils.getName(data.getUrl());
                        File file = new File(Utils.getDirectoryPath(filename) + filename);
                        file.delete();
                    }
                });
            });

//                holder.imageMessage.setOnClickListener(v -> {
//                    imageViewer.viewImage(data.getUrl());
//                });
        }
    }

    private void handleDocumentMessages(ViewHolder holder, int position) {

        holder.layoutDocument.setVisibility(View.VISIBLE);
        holder.layoutText.setVisibility(View.GONE);

        if (data.getStatus().equals("sending")) {
            holder.sendDocLayout.setVisibility(View.VISIBLE);
            holder.retryD.setVisibility(View.GONE);
            holder.timeDocument.setVisibility(View.GONE);
            holder.statusDocument.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            uploadMessage.uploadMessage(position, data);

            holder.cancelSendDocument.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendDocLayout.setVisibility(View.GONE);
                holder.retryD.setVisibility(View.VISIBLE);

                holder.retryD.setOnClickListener(v1 -> {
                    holder.sendDocLayout.setVisibility(View.VISIBLE);
                    holder.retryD.setVisibility(View.GONE);

                    uploadMessage.uploadMessage(position, data);
                });

            });

        } else if (data.getStatus().equals("cancelled")) {
            holder.sendDocLayout.setVisibility(View.GONE);
            holder.retryD.setVisibility(View.VISIBLE);
            holder.timeDocument.setVisibility(View.GONE);
            holder.statusDocument.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));

            holder.retryD.setOnClickListener(v -> {
                holder.sendDocLayout.setVisibility(View.VISIBLE);
                holder.retryD.setVisibility(View.GONE);

                uploadMessage.uploadMessage(position, data);
            });

            holder.cancelSendDocument.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendDocLayout.setVisibility(View.GONE);
                holder.retryD.setVisibility(View.VISIBLE);
            });
        } else {
            holder.sendDocLayout.setVisibility(View.GONE);
            holder.timeDocument.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutDocument.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                holder.layoutDocument.setLayoutParams(params);
                holder.timeDocument.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusDocument.setVisibility(View.GONE);
            }

            holder.timeDocument.setText(getTime(data.getDateTime()));

            switch (data.getStatus()) {
                case "send":
                    holder.statusDocument.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusDocument.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusDocument.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusDocument.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }

            if (!Utils.isFilePresent(data.getUrl())) {
                double fileSize = Double.parseDouble(data.getFileSize()) / 1024.0;
                holder.fileSizeDocument.setText(String.format(activity.getString(R.string.mb), fileSize));
                holder.downloadDocument.setVisibility(View.VISIBLE);
            }

            holder.layoutDocument.setOnClickListener(v -> {
                if (Utils.isFilePresent(data.getUrl())) {
                    String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    String filepath = Utils.getDirectoryPath(filename) + filename;
                    Utils.openFile(new File(filepath), activity);
                }
            });

            holder.downloadDocument.setOnClickListener(v -> {
                downloader = new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
                    @Override
                    public void onPreExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderDocument.setVisibility(View.VISIBLE);
                                holder.downloadDocument.setVisibility(View.GONE);
                                holder.progressBarDocument.setSecondaryProgress(0);
                            }
                        });
                        super.onPreExecute();
                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.progressBarDocument.setSecondaryProgress(progress);
                            }
                        });
                        super.onProgressUpdate(progress);
                    }

                    @Override
                    public void onFailure() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderDocument.setVisibility(View.GONE);
                                holder.downloadDocument.setVisibility(View.VISIBLE);
                                try {
                                    String filename = FilenameUtils.getName(data.getUrl());
                                    File file = new File(Utils.getDirectoryPath(filename) + filename);
                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        super.onFailure();
                    }

                    @Override
                    public void onPostExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderDocument.setVisibility(View.GONE);
                                notifyItemChanged(position);
                            }
                        });
                        super.onPostExecute();
                    }

                });
                downloader.setContext(activity);
                downloader.execute();
            });

            holder.cancelDocument.setOnClickListener(v -> {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloader.cancel(true);
                        holder.loaderDocument.setVisibility(View.GONE);
                        holder.downloadDocument.setVisibility(View.VISIBLE);
                        String filename = FilenameUtils.getName(data.getUrl());
                        File file = new File(Utils.getDirectoryPath(filename) + filename);
                        file.delete();
                    }
                });
            });
        }
    }

    private void handleLinkMessages(ViewHolder holder, int position) {

        holder.layoutText.setVisibility(View.GONE);
        holder.layoutLink.setVisibility(View.VISIBLE);

        link = null;
        getLink(data.getText());

        if (data.getStatus().equals("sending")) {
            holder.statusLinkk.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            holder.timeLinkk.setVisibility(View.GONE);
            SpannableString content = new SpannableString(data.getText());
            content.setSpan(new UnderlineSpan(), 0, data.getText().length(), 0);
            holder.linkText.setText(content);
            uploadMessage.saveMessage(position, data);
        } else if (data.getStatus().equals("cancelled")) {
            holder.statusLinkk.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            holder.timeLinkk.setVisibility(View.GONE);
            SpannableString content = new SpannableString(data.getText());
            content.setSpan(new UnderlineSpan(), 0, data.getText().length(), 0);
            holder.linkText.setText(content);
            uploadMessage.saveMessage(position, data);
        } else {

            holder.timeLinkk.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutLink.setBackgroundResource(R.drawable.bubble_left);
                holder.layoutLink.setLayoutParams(params);
                holder.textMessage.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.timeLinkk.setTextColor(ContextCompat.getColor(activity, R.color.white));
                holder.statusLinkk.setVisibility(View.GONE);
            }
            holder.timeLinkk.setText(getTime(data.getDateTime()));

            switch (data.getStatus()) {
                case "send":
                    holder.statusLinkk.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusLinkk.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusLinkk.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusLinkk.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }
        }

        String linkk = data.getText();
        holder.linkText.setText(linkk);
        if (link == null) {
            WebView webView = new WebView(activity);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    Link link = new Link();
                    link.setTitle(view.getTitle());
                    link.setUrl(linkk);
                    saveLink(link);

                }
            });
            webView.loadUrl(data.getText());
        }

        holder.layoutLink.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            if (!data.getText().contains("http://") && !data.getText().contains("https://"))
                data.setText("http://" + data.getText());
            i.setData(Uri.parse(data.getText()));
            activity.startActivity(Intent.createChooser(i, "Open Link"));
        });

        holder.layoutLink.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("text", data.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "Link copied!", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void handleGifMessages(ViewHolder holder, int position) {

        holder.layoutImage.setVisibility(View.VISIBLE);
        holder.layoutText.setVisibility(View.GONE);
        if (data.getStatus().equals("sending")) {
            holder.sendVideoLayout.setVisibility(View.VISIBLE);
            holder.timeImg.setVisibility(View.GONE);
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            Glide.with(activity).load(data.getUrl()).into(holder.imageMessage);
            uploadMessage.uploadMessage(position, data);

            holder.cancelSendImgVideo.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendVideoLayout.setVisibility(View.GONE);
                holder.retryIV.setVisibility(View.VISIBLE);

                holder.retryIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.retryIV.setVisibility(View.GONE);
                        holder.sendVideoLayout.setVisibility(View.VISIBLE);

                        uploadMessage.uploadMessage(position, data);
                    }
                });
            });

        } else if (data.getStatus().equals("cancelled")) {
            holder.retryIV.setVisibility(View.VISIBLE);
            holder.timeImg.setVisibility(View.GONE);
            holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            Glide.with(activity).load(data.getUrl()).into(holder.imageMessage);

            holder.retryIV.setOnClickListener(v -> {
                holder.retryIV.setVisibility(View.GONE);
                holder.sendVideoLayout.setVisibility(View.VISIBLE);

                uploadMessage.uploadMessage(position, data);
            });

            holder.cancelSendImgVideo.setOnClickListener(v -> {
                cancelSendingFiles.onCancel(position);
                holder.sendVideoLayout.setVisibility(View.GONE);
                holder.retryIV.setVisibility(View.VISIBLE);
            });

        } else {
            holder.sendVideoLayout.setVisibility(View.GONE);
            holder.timeImg.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutImage.setCardBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                holder.layoutImage.setLayoutParams(params);
                holder.timeImg.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusImg.setVisibility(View.GONE);
            }

            boolean isPresent = Utils.isFilePresent(data.getUrl());
            if (isPresent) {
                String filename = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                Uri uri = Uri.fromFile(new File(Utils.getDirectoryPath(filename) + filename));
                Glide.with(activity)
                        .load(uri)
                        .into(holder.imageMessage);
            } else {

                holder.imageMessage.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.blur));
                holder.fileSizeVideo.setVisibility(View.GONE);
//                    holder.downloadVideo.setVisibility(View.VISIBLE);
                holder.gifLayout.setVisibility(View.VISIBLE);

//                    Glide.with(activity)
//                            .load(data.getUrl())
//                            .into(holder.imageMessage);
//                    new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
//                        @Override
//                        public void onPostExecute() {
//                            super.onPostExecute();
//                        }
//                    }).execute();
            }

            holder.timeImg.setText(getTime(data.getDateTime()));

            switch (data.getStatus()) {
                case "send":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusImg.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }

            holder.imageMessage.setOnClickListener(v -> {
                if (isPresent) {
                    String fileName = data.getUrl().substring(data.getUrl().lastIndexOf('/') + 1);
                    File file = new File(Utils.getDirectoryPath(fileName) + fileName);
//                            Utils.openFile(file, activity);
                    imageViewer.viewImage(FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file).toString());
                }
            });

            holder.gifLayout.setOnClickListener(v -> {
                downloader = new DownloadFile(data.getUrl(), !isRight, new DownloadListener() {
                    @Override
                    public void onPreExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.VISIBLE);
                                holder.gifLayout.setVisibility(View.GONE);
                                holder.progressBarVideo.setSecondaryProgress(0);
                            }
                        });
                        super.onPreExecute();
                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.progressBarVideo.setSecondaryProgress(progress);
                            }
                        });
                        super.onProgressUpdate(progress);
                    }

                    @Override
                    public void onFailure() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.GONE);
//                                    holder.downloadVideo.setVisibility(View.VISIBLE);
                                holder.gifLayout.setVisibility(View.VISIBLE);
                                try {
                                    String filename = FilenameUtils.getName(data.getUrl());
                                    File file = new File(Utils.getDirectoryPath(filename) + filename);
                                    file.delete();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        super.onFailure();
                    }

                    @Override
                    public void onPostExecute() {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.loaderVideo.setVisibility(View.GONE);
                                notifyItemChanged(position);
                            }
                        });
                        super.onPostExecute();
                    }

                });
                downloader.setContext(activity);
                downloader.execute();
            });

            holder.cancelVideo.setOnClickListener(v -> {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloader.cancel(true);
                        holder.loaderVideo.setVisibility(View.GONE);
                        holder.gifLayout.setVisibility(View.VISIBLE);
                        String filename = FilenameUtils.getName(data.getUrl());
                        File file = new File(Utils.getDirectoryPath(filename) + filename);
                        file.delete();
                    }
                });
            });

//                holder.imageMessage.setOnClickListener(v -> {
////                    imageViewer.viewImage(data.getUrl());
//                    new DialogReviewSendMedia(activity, false, null, data.getUrl()).loadGif(null);
//                });
        }
    }

    private void handleContactMessages(ViewHolder holder, int position) {

        String image = data.getText().split(";")[0];
        String number = data.getText().split(";")[1];
        String phoneId = data.getText().split(";")[2];

        holder.layoutText.setVisibility(View.GONE);
        holder.layoutContact.setVisibility(View.VISIBLE);
        holder.nameContact.setText(number);
        Glide.with(activity).load(image).into(holder.dpContact);
        if (data.getStatus().equals("sending")) {
            holder.statusContact.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            holder.timeContact.setVisibility(View.GONE);
            uploadMessage.saveMessage(position, data);
        } else if (data.getStatus().equals("cancelled")) {
            holder.statusContact.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            holder.timeContact.setVisibility(View.GONE);
            uploadMessage.saveMessage(position, data);
        } else {

            holder.timeContact.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutContact.setBackgroundResource(R.drawable.bubble_left_primary);
                holder.layoutContact.setLayoutParams(params);
//                    holder.nameContact.setTextColor(ContextCompat.getColor(activity, R.color.black));
//                    holder.messageContact.setTextColor(ContextCompat.getColor(activity, R.color.black));
//                    holder.saveContact.setTextColor(ContextCompat.getColor(activity, R.color.black));
//                    holder.timeContact.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusContact.setVisibility(View.GONE);
            }
            holder.timeContact.setText(getTime(data.getDateTime()));

            Database db = Database.getInstance(activity);
            if (db.isContactExists(number)) {
                holder.nameContact.setText(db.getContact(number).getName());
                holder.saveContact.setVisibility(View.GONE);
                holder.divContact.setVisibility(View.GONE);
            }

            switch (data.getStatus()) {
                case "send":
                    holder.statusContact.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusContact.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusContact.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusContact.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }
        }
    }

    private void handleTextMessages(ViewHolder holder, int position) {

        holder.layoutText.setVisibility(View.VISIBLE);
        holder.textMessage.setText(data.getText());
        if (data.getStatus().equals("sending")) {
            holder.statusTv.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            holder.timeTv.setVisibility(View.GONE);
            uploadMessage.saveMessage(position, data);
        } else if (data.getStatus().equals("cancelled")) {
            holder.statusTv.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_error));
            holder.timeTv.setVisibility(View.GONE);
            uploadMessage.saveMessage(position, data);
        } else {

            holder.timeTv.setVisibility(View.VISIBLE);
            if (!isRight) {
                holder.layoutText.setBackgroundResource(R.drawable.bubble_left);
                holder.layoutText.setLayoutParams(params);
                holder.textMessage.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.timeTv.setTextColor(ContextCompat.getColor(activity, R.color.black));
                holder.statusTv.setVisibility(View.GONE);
            }
            holder.timeTv.setText(getTime(data.getDateTime()));

            switch (data.getStatus()) {
                case "send":
                    holder.statusTv.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_single_check));
                    break;
                case "delivered":
                    holder.statusTv.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    break;
                case "seen":
                    holder.statusTv.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_double_check));
                    holder.statusTv.setColorFilter(ContextCompat.getColor(activity, R.color.colorGreenDark), PorterDuff.Mode.SRC_IN);
                    break;
            }
        }

    }

    @Override
    public long getItemId(int position) {
        return mMessageData.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mMessageData.size();
    }

    public ChatAdapter(List<MessageData> arrayList, Activity activity) {
        mMessageData = arrayList;
        this.activity = activity;
        db = Database.getInstance(activity);
    }

    public ScrollToLast getScrollToLast() {
        return scrollToLast;
    }

    public void setScrollToLast(ScrollToLast scrollToLast) {
        this.scrollToLast = scrollToLast;
    }

    public UploadMessage getUploadMessage() {
        return uploadMessage;
    }

    public UpdateMessage getUpdateMessage() {
        return updateMessage;
    }

    public void setCancelSendingFiles(CancelSendingFiles cancelSendingFiles) {
        this.cancelSendingFiles = cancelSendingFiles;
    }

    public void setUpdateMessage(UpdateMessage updateMessage) {
        this.updateMessage = updateMessage;
    }

    public void setUploadMessage(UploadMessage uploadMessage) {
        this.uploadMessage = uploadMessage;
    }

    public void setImageViewer(ImageViewer imageViewer) {
        this.imageViewer = imageViewer;
    }

    public void setGetProfile(GetProfile getProfile) {
        this.getProfile = getProfile;
    }

    public void setConversationType(String conversationType) {
        this.conversationType = conversationType;
    }

    public List<MessageData> getmMessageData() {
        return mMessageData;
    }

    public void setmMessageData(List<MessageData> mMessageData) {
        this.mMessageData = mMessageData;
    }

    private void getLink(String url) {
        link = db.getLink(url);
    }

    private void saveLink(Link link) {
        db.insertLink(link);
    }

    private void playAudio(String url, ViewHolder holder) throws Exception {
        killMediaPlayer();

        holder.liveDuration.setText(R.string.zero_time);
        holder.seekBarAudio.setProgress(0);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                handler.removeCallbacksAndMessages(null);
                holder.liveDuration.setText(Utils.msToTimeConverter(mediaPlayer.getDuration()));
                holder.seekBarAudio.setProgress(mediaPlayer.getDuration());

                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                holder.liveDuration.setText(R.string.zero_time);
                holder.seekBarAudio.setProgress(0);

                holder.btnPlay.setVisibility(View.VISIBLE);
                holder.btnPause.setVisibility(View.GONE);
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                holder.seekBarAudio.setMax(mediaPlayer.getDuration());
                holder.seekBarAudio.setProgress(0);
            }
        });
        mediaPlayer.setDataSource(url);
        mediaPlayer.prepare();
        mediaPlayer.start();

        handler = new Handler();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    count += 1;
                    int currentDuration = mediaPlayer.getCurrentPosition();
                    if (count == 10) {
                        holder.liveDuration.setText(Utils.msToTimeConverter(currentDuration));
                        count = 0;
                    }
                    holder.seekBarAudio.setProgress(currentDuration);
                }
                handler.postDelayed(this, 100);
            }
        });

    }

    private void killMediaPlayer() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getTime(String createdAt) {
        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(createdAt);
            SimpleDateFormat pstFormat = new SimpleDateFormat("h:mm aa");
            pstFormat.setTimeZone(TimeZone.getDefault());
            return pstFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getPositionForMessage(String id) {
        for (int i = 0; i < mMessageData.size(); i++) {
            if (mMessageData.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }

    public void showBackgroundTransition(Integer integer) {
        performBackgroundTransistion = integer;
        notifyItemChanged(integer);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textMessage, timeTv, linkText, timeLinkk, timeImg, timeContact, timeAudio, tDuration, liveDuration,
                nameTv, tvInfo, timeDocument, fileSizeDocument, fileSizeVideo, seperator, videoDuration, audioFileSize,
                nameContact, messageContact, saveContact;
        private final View divContact;
        private final LinearLayout layoutText, layoutVoice, videoDurationLayout;
        private final ImageView imageMessage, downloadAudio, statusTv, statusContact, statusLinkk, statusImg, statusAudio, btnPlay,
                videoPlay, btnPause, cancelDocument, cancelAudio, cancelVideo, cancelSendImgVideo, cancelSendAudio, cancelSendDocument,
                documentImage, statusDocument, dpContact;
        private final CardView layoutImage, layoutInfo, layoutDocument, loaderAudio, loaderDocument, loaderVideo,
                downloadDocument, downloadVideo, playVideo, sendVideoLayout, sendAudioLayout, sendDocLayout, gifLayout,
                retryIV, retryA, retryD;
        private final LinearLayout msgLayout, layoutTimerAudio, layoutLink, layoutContact;
        private final ProgressBar seekBarAudio, progressBarAudio, progressBarDocument, progressBarVideo,
                sendImgVideo, sendAudio, sendDoc;
        private final WebView webView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessage = itemView.findViewById(R.id.tv_text_message);
            linkText = itemView.findViewById(R.id.linkText);
            webView = itemView.findViewById(R.id.webView);
            timeLinkk = itemView.findViewById(R.id.timeLinkk);
            layoutContact = itemView.findViewById(R.id.layoutContact);
            nameContact = itemView.findViewById(R.id.nameContact);
            messageContact = itemView.findViewById(R.id.messageContact);
            saveContact = itemView.findViewById(R.id.saveContact);
            dpContact = itemView.findViewById(R.id.dpContact);
            timeContact = itemView.findViewById(R.id.timeContact);
            statusContact = itemView.findViewById(R.id.statusContact);
            divContact = itemView.findViewById(R.id.divContact);
            statusLinkk = itemView.findViewById(R.id.statusLinkk);
            layoutLink = itemView.findViewById(R.id.layout_link);
            fileSizeDocument = itemView.findViewById(R.id.fileSizeDocument);
            sendImgVideo = itemView.findViewById(R.id.pbSendVideo);
            sendAudio = itemView.findViewById(R.id.pbSendAudio);
            sendDoc = itemView.findViewById(R.id.pbSendDoc);
            sendVideoLayout = itemView.findViewById(R.id.sendLayout);
            sendAudioLayout = itemView.findViewById(R.id.sendALayout);
            sendDocLayout = itemView.findViewById(R.id.sendDLayout);
            layoutTimerAudio = itemView.findViewById(R.id.layoutTimer);
            fileSizeVideo = itemView.findViewById(R.id.fileSizeVideo);
            gifLayout = itemView.findViewById(R.id.gifLayout);
            loaderVideo = itemView.findViewById(R.id.loaderVideo);
            loaderAudio = itemView.findViewById(R.id.loaderAudio);
            downloadVideo = itemView.findViewById(R.id.downloadVideo);
            videoPlay = itemView.findViewById(R.id.ic_play);
            playVideo = itemView.findViewById(R.id.playVideo);
            seperator = itemView.findViewById(R.id.seperator);
            layoutImage = itemView.findViewById(R.id.layout_image);
            tDuration = itemView.findViewById(R.id.tDuration);
            nameTv = itemView.findViewById(R.id.nameTv);
            liveDuration = itemView.findViewById(R.id.liveDuration);
            timeTv = itemView.findViewById(R.id.timeTv);
            timeImg = itemView.findViewById(R.id.timeImg);
            timeAudio = itemView.findViewById(R.id.timeAudio);
            layoutText = itemView.findViewById(R.id.layout_text);
            layoutInfo = itemView.findViewById(R.id.layoutInfo);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            imageMessage = itemView.findViewById(R.id.image_chat);
            layoutDocument = itemView.findViewById(R.id.layout_document);
            downloadDocument = itemView.findViewById(R.id.downloadDocument);
            downloadAudio = itemView.findViewById(R.id.downloadAudio);
            timeDocument = itemView.findViewById(R.id.timeDocument);
            statusDocument = itemView.findViewById(R.id.statusDocument);
            documentImage = itemView.findViewById(R.id.documentImage);
            loaderDocument = itemView.findViewById(R.id.loaderDocument);
            statusTv = itemView.findViewById(R.id.statusTv);
            statusImg = itemView.findViewById(R.id.statusImg);
            statusAudio = itemView.findViewById(R.id.statusAudio);
            layoutVoice = itemView.findViewById(R.id.layout_voice);
            btnPlay = itemView.findViewById(R.id.btn_play_audio);
            btnPause = itemView.findViewById(R.id.btn_pause_audio);
            msgLayout = itemView.findViewById(R.id.msgLayout);
            seekBarAudio = itemView.findViewById(R.id.progressBar);
            progressBarDocument = itemView.findViewById(R.id.progressBarDocument);
            progressBarVideo = itemView.findViewById(R.id.progressBarVideo);
            progressBarAudio = itemView.findViewById(R.id.progressBarAudio);
            cancelDocument = itemView.findViewById(R.id.cancelDocument);
            cancelVideo = itemView.findViewById(R.id.cancelVideo);
            cancelAudio = itemView.findViewById(R.id.cancelAudio);
            cancelSendImgVideo = itemView.findViewById(R.id.cancelSendImgVideo);
            cancelSendAudio = itemView.findViewById(R.id.cancelSendAudio);
            cancelSendDocument = itemView.findViewById(R.id.cancelSendDocument);
            videoDurationLayout = itemView.findViewById(R.id.durationLayout);
            videoDuration = itemView.findViewById(R.id.videoDuration);
            audioFileSize = itemView.findViewById(R.id.audioFileSize);
            retryIV = itemView.findViewById(R.id.retryIVLayout);
            retryA = itemView.findViewById(R.id.retryA);
            retryD = itemView.findViewById(R.id.retryDLayout);
        }
    }

    public static class DownloadFile extends AsyncTask<String, String, String> {
        String folder;
        boolean scanFileToGallery;
        String urlString;
        String fileName;
        DownloadListener downloadListener;
        Context context;
        private File directory;

        public DownloadFile(String urlString, boolean scanFileToGallery, DownloadListener downloadListener) {
            this.scanFileToGallery = scanFileToGallery;
            this.downloadListener = downloadListener;
            this.urlString = urlString;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            downloadListener.onPreExecute();
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            downloadListener.onProgressUpdate(Integer.parseInt(values[0]));
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(ResourcePlusApplication.TAG, "file saved");
            downloadListener.onPostExecute();

//            if (folder.contains(".mp4")) {
//                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(folder, MediaStore.Video.Thumbnails.MINI_KIND);
//                Utils.saveBitmapToLocation(bitmap, folder.replace(".mp4", "_thumbnail.jpeg"), context);
//            }

            if (scanFileToGallery) {
                MediaScannerConnection.scanFile(ResourcePlusApplication.getContext(),
                        new String[]{folder}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String newpath, Uri newuri) {

                            }
                        });
            }
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            int count = 0;
            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                connection.connect();
                int lengthOfFile = connection.getContentLength();
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                folder = Utils.getDirectoryPath(fileName) + fileName;
                directory = new File(folder);
                if (directory.exists()) {
                    new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(ResourcePlusApplication.getContext(), "Already Downloaded", Toast.LENGTH_SHORT).show());
                } else {
                    directory = new File(folder.substring(0, folder.lastIndexOf('/')));
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    OutputStream output = new FileOutputStream(folder);
                    byte[] data = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress("" + (int) ((total * 100) / lengthOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                    return "Downloaded..." + fileName;
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                directory.delete();
                downloadListener.onFailure();
            }
            return "Something went wrong";
        }
    }

    public interface ImageViewer {
        void viewImage(String url);
    }

    public interface GetProfile {
        Users getUser(String phoneId);
    }

    public interface UploadMessage {
        void uploadMessage(int pos, MessageData messageData);

        void saveMessage(int pos, MessageData messageData);
    }

    public interface ScrollToLast {
        void scroll();
    }

    public interface UpdateMessage {
        void update(MessageData messageData, String status);
    }

    public interface CancelSendingFiles {
        void onCancel(int pos);
    }
}

package com.mr_w.resourceplus.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.ResourcePlusApplication;
import com.mr_w.resourceplus.utils.Utils;
import com.potyvideo.library.AndExoPlayerView;

import java.io.File;
import java.util.Objects;

public class DialogReviewSendMedia {
    private String gifUrl;
    private Context context;
    private Dialog dialog;
    private Bitmap bitmap;
    private ZoomageView image;
    private ImageView btn_back, btnBack, btnEdit, btnShare;
    private CardView btnSend;
    private boolean showButton;
    private RelativeLayout imgLayout, videoLayout;
    private AndExoPlayerView videoView;
    private Toolbar toolbar, toolbarGroup;
    private TextView type;
    private Uri gifUri;
    private Drawable drawable;

    public DialogReviewSendMedia(Context context, Bitmap bitmap, boolean showButton) {
        this.context = context;
        this.bitmap = bitmap;
        this.showButton = showButton;
        this.dialog = new Dialog(context);
        initialize();
    }

    public DialogReviewSendMedia(Context context, boolean showButton, Uri gifUri, String url) {
        this.context = context;
        this.gifUri = gifUri;
        this.showButton = showButton;
        this.dialog = new Dialog(context);
        this.gifUrl = url;
        initialize();
    }

    public DialogReviewSendMedia(Context context) {
        this.context = context;
        this.dialog = new Dialog(context);
        showButton = true;
        initialize();
        type.setText("Video");
    }

    public DialogReviewSendMedia(Context context, Drawable drawable) {
        this.context = context;
        this.dialog = new Dialog(context);
        showButton = false;
        initialize();
        type.setText("Image");
        this.drawable = drawable;
    }

    public void initialize() {

        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR); // before
        dialog.setContentView(R.layout.review_send_image);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

        videoView = dialog.findViewById(R.id.videoView);
        imgLayout = dialog.findViewById(R.id.imgLayout);
        videoLayout = dialog.findViewById(R.id.videoLayout);
        type = dialog.findViewById(R.id.type);
        btn_back = dialog.findViewById(R.id.btn_backk);
        btnBack = dialog.findViewById(R.id.btnBack);
        btnEdit = dialog.findViewById(R.id.btnEdit);
        btnShare = dialog.findViewById(R.id.btnShare);
        toolbar = dialog.findViewById(R.id.toolbar);
        toolbarGroup = dialog.findViewById(R.id.toolbarGroup);
        image = dialog.findViewById(R.id.imageView);
        btnSend = dialog.findViewById(R.id.btn_send);

        if (!showButton)
            btnSend.setVisibility(View.GONE);

    }

    public void show() {

        dialog.show();
        image.setImageDrawable(drawable);

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    public void show(String path, final OnCallBack onCallBack) {

        imgLayout.setVisibility(View.GONE);
        videoLayout.setVisibility(View.VISIBLE);
        dialog.show();

        videoView.setSource(path);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBack.onButtonSendClick();
                dialog.dismiss();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    public void show(final OnCallBack onCallBack) {
        dialog.show();

        this.type.setText("Image");
        image.setImageBitmap(bitmap);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBack.onButtonSendClick();
                dialog.dismiss();
            }
        });

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    public void show(String uri) {
        dialog.show();

        if (Utils.isFilePresent(uri)) {
            String filename = uri.substring(uri.lastIndexOf('/') + 1);
            Uri path = FileProvider.getUriForFile(context, ResourcePlusApplication.getContext().getPackageName() + ".provider",
                    new File(Utils.getDirectoryPath(filename) + filename));
            if (path.getPath().contains(".gif")) {
                Glide.with(context).load(path).into(image);
            } else
                image.setImageURI(path);
        } else
            Glide.with(context).load(uri).into(image);

        btn_back.setOnClickListener(v -> dialog.dismiss());
    }

    public void show(String uri, String title, String type) {
        dialog.show();

        this.type.setText(title);
        if (type.equals("image")) {
            if (Utils.isFilePresent(uri)) {
                String filename = uri.substring(uri.lastIndexOf('/') + 1);
                Uri path = FileProvider.getUriForFile(context, ResourcePlusApplication.getContext().getPackageName() + ".provider",
                        new File(Utils.getDirectoryPath(filename) + filename));
                image.setImageURI(path);
            } else
                Glide.with(context).load(uri).into(image);
        } else {
            imgLayout.setVisibility(View.GONE);
            videoLayout.setVisibility(View.VISIBLE);
            dialog.show();

            videoView.setSource(uri);

            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }

        btn_back.setOnClickListener(v -> dialog.dismiss());
    }

    public void show(Uri uri) {
        dialog.show();

        image.setImageURI(uri);
        btn_back.setOnClickListener(v -> dialog.dismiss());
    }

    public void loadGif(OnCallBack onCallBack) {
        dialog.show();

        type.setText("GIF");
        if (gifUri != null)
            Glide.with(context).load(gifUri).into(image);
        else
            Glide.with(context).load(gifUrl).into(image);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBack.onButtonSendClick();
                dialog.dismiss();
            }
        });

        btn_back.setOnClickListener(v -> dialog.dismiss());
    }

    public void loadImage(OnCallBack onCallBack) {
        dialog.show();

        Glide.with(context).load(gifUrl).into(image);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBack.onButtonSendClick();
                dialog.dismiss();
            }
        });

        btn_back.setOnClickListener(v -> dialog.dismiss());
    }

    public interface OnCallBack {
        void onButtonSendClick();
    }
}

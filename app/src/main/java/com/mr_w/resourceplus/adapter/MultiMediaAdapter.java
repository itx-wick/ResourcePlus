package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.dialog.DialogReviewSendMedia;
import com.mr_w.resourceplus.model.Media;
import com.mr_w.resourceplus.model.MessageData;
import com.mr_w.resourceplus.utils.Utils;

import java.io.File;
import java.util.List;

public class MultiMediaAdapter extends RecyclerView.Adapter<MultiMediaAdapter.ViewHolder> {

    private Context context;
    private List<Media> mediaList;

    public MultiMediaAdapter(Context context, List<Media> mediaList) {
        this.context = context;
        this.mediaList = mediaList;
    }

    public List<Media> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MultiMediaAdapter.ViewHolder holder, int position) {
        Media media = mediaList.get(position);

        String fileName = media.getPath().substring(media.getPath().lastIndexOf('/') + 1);
        String fullPath = Utils.getDirectoryPath(fileName) + fileName;
        File file = new File(fullPath);
        Uri uri = Uri.fromFile(file);

        if (media.getType().equals(MessageData.TYPE.VIDEO.toString())) {
            holder.playerLayout.setVisibility(View.VISIBLE);

            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(uri.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            holder.media.setImageBitmap(bitmap);

            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(fullPath);
            String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            holder.duration.setText(Utils.msToTimeConverter(Integer.parseInt(duration)));

        } else if (media.getType().equals(MessageData.TYPE.PICTURE.toString())) {
            if (Utils.isFilePresent(media.getPath())) {
                holder.media.setImageURI(uri);
            } else
                Glide.with(context).load(media.getPath()).into(holder.media);
        } else if (media.getType().equals(MessageData.TYPE.AUDIO.toString())) {

            holder.media.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_audio_file));
            holder.playerLayout.setVisibility(View.VISIBLE);
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(fullPath);
            String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            holder.duration.setText(Utils.msToTimeConverter(Integer.parseInt(duration)));

            int padding = (int) Utils.dpToPx(10f);
            holder.media.setPadding(padding, padding, padding, padding);

        } else if (media.getType().equals(MessageData.TYPE.GIF.toString())) {
            if (Utils.isFilePresent(media.getPath())) {
                Glide.with(context).load(uri).into(holder.media);
            } else
                Glide.with(context).load(media.getPath()).into(holder.media);
        }

        holder.root.setOnClickListener(v -> {
            if (media.getType().equals(MessageData.TYPE.VIDEO.toString())) {
                new DialogReviewSendMedia(context, null, false).show(uri.getPath(), "Video", "video");
            } else if (media.getType().equals(MessageData.TYPE.PICTURE.toString())) {
                new DialogReviewSendMedia(context, null, false).show(media.getPath(), "Image", "image");
            } else if (media.getType().equals(MessageData.TYPE.AUDIO.toString())) {
                Utils.openFile(file, context);
            } else if (media.getType().equals(MessageData.TYPE.GIF.toString())) {
                if (Utils.isFilePresent(media.getPath()))
                    new DialogReviewSendMedia(context, false, uri, null).loadGif(null);
                else
                    new DialogReviewSendMedia(context, false, null, media.getPath()).loadGif(null);
            }
        });

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        ImageView media;
        LinearLayout playerLayout;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            media = itemView.findViewById(R.id.media);
            playerLayout = itemView.findViewById(R.id.playerLayout);
            duration = itemView.findViewById(R.id.duration);
        }
    }

}

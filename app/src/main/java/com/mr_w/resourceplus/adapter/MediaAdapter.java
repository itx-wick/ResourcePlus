package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    private Context context;
    private List<Media> mediaList;
    private String fileName;
    private String fullPath;
    private File file;
    private Uri uri;

    public MediaAdapter(Context context, List<Media> mediaList) {
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaAdapter.ViewHolder holder, int position) {
        Media media = mediaList.get(position);

        if (!media.getType().equals(MessageData.TYPE.LINK.toString())) {

            fileName = media.getPath().substring(media.getPath().lastIndexOf('/') + 1);
            fullPath = Utils.getDirectoryPath(fileName) + fileName;
            file = new File(fullPath);
            uri = Uri.fromFile(file);
        }

        if (media.getType().equals(MessageData.TYPE.VIDEO.toString())) {

            holder.videoThings.setVisibility(View.VISIBLE);
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
        } else if (media.getType().equals(MessageData.TYPE.DOCUMENTS.toString())) {
            holder.media.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.document));

            holder.media.setBackgroundResource(R.color.gray);
            int padding = (int) Utils.dpToPx(10f);
            holder.media.setPadding(padding, padding, padding, padding);
        } else if (media.getType().equals(MessageData.TYPE.AUDIO.toString())) {

            holder.videoThings.setVisibility(View.VISIBLE);

            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(fullPath);
            String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            holder.duration.setText(Utils.msToTimeConverter(Integer.parseInt(duration)));

            holder.media.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_audio_file));
            int padding = (int) Utils.dpToPx(5f);
            holder.media.setPadding(padding, padding, padding, padding);
        } else if (media.getType().equals(MessageData.TYPE.GIF.toString())) {
            Glide.with(context).load(media.getPath()).into(holder.media);
        } else if (media.getType().equals(MessageData.TYPE.LINK.toString())) {
//            Glide.with(context).load(media.getPath().split(";")[1]).into(holder.media);
            holder.media.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_invite_link));

            holder.media.setBackgroundResource(R.color.gray);
            int padding = (int) Utils.dpToPx(10f);
            holder.media.setPadding(padding, padding, padding, padding);
        }

        holder.root.setOnClickListener(v -> {
            if (Utils.isFilePresent(media.getPath())) {
                if (media.getType().equals(MessageData.TYPE.VIDEO.toString())) {
                    new DialogReviewSendMedia(context, null, false).show(uri.getPath(), "Video", "video");
                } else if (media.getType().equals(MessageData.TYPE.PICTURE.toString())) {
                    new DialogReviewSendMedia(context, null, false).show(media.getPath(), "Image", "image");
                } else if (media.getType().equals(MessageData.TYPE.DOCUMENTS.toString())) {
                    Utils.openFile(file, context);
                } else if (media.getType().equals(MessageData.TYPE.AUDIO.toString())) {
                    Utils.openFile(file, context);
                } else if (media.getType().equals(MessageData.TYPE.GIF.toString())) {
                    if (Utils.isFilePresent(media.getPath()))
                        new DialogReviewSendMedia(context, false, uri, null).loadGif(null);
                    else
                        new DialogReviewSendMedia(context, false, null, media.getPath()).loadGif(null);
                }
            } else if (media.getType().equals(MessageData.TYPE.LINK.toString())) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(media.getPath().split(";")[0]));
                context.startActivity(i);
            } else
                Toast.makeText(context, "Media not downloaded.", Toast.LENGTH_SHORT).show();
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
        LinearLayout videoThings;
        TextView duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            media = itemView.findViewById(R.id.media);
            videoThings = itemView.findViewById(R.id.videoThings);
            duration = itemView.findViewById(R.id.duration);
        }
    }

}

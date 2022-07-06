package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.database.Database;
import com.mr_w.resourceplus.model.Link;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class LinkAdapter extends RecyclerView.Adapter<LinkAdapter.ViewHolder> {

    private final Context context;
    private List<String> linkList;
    private Database db;

    public LinkAdapter(Context context, List<String> linkList) {
        this.context = context;
        this.linkList = linkList;
        db = Database.getInstance(context);
    }

    public List<String> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<String> linkList) {
        this.linkList = linkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.link_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LinkAdapter.ViewHolder holder, int position) {
        final String[] urls = {linkList.get(position)};
        final String[] url = {urls[0].split(";")[0]};
        String date = urls[0].split(";")[1];

        getLink(url[0], holder);
        holder.date.setText(getTime(date));

        holder.root.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            if (!url[0].contains("http://") && !url[0].contains("https://"))
                url[0] = "http://" + url[0];
            i.setData(Uri.parse(url[0]));
            context.startActivity(i);
        });

    }

    public String getTime(String createdAt) {
        try {
            SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = utcFormat.parse(createdAt);
            SimpleDateFormat pstFormat = new SimpleDateFormat("h:mm aa - dd/MM/yyyy");
            pstFormat.setTimeZone(TimeZone.getDefault());
            return pstFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getLink(String url, ViewHolder holder) {
        Link link = db.getLink(url);
        holder.title.setText(link.getTitle());
        holder.url.setText(link.getUrl());
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
        return linkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout root;
        TextView title, url, date;
        WebView webView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            url = itemView.findViewById(R.id.url);
        }
    }

}

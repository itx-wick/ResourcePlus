package com.mr_w.resourceplus.adapter;

import android.content.Context;
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
import com.mr_w.resourceplus.model.CommonGroup;

import java.util.List;

public class CommonGroupAdapter extends RecyclerView.Adapter<CommonGroupAdapter.ViewHolder> {

    private Context context;
    private List<CommonGroup> commonGroups;
    ClickListener clickListener;

    public CommonGroupAdapter(Context context, List<CommonGroup> commonGroups) {
        this.context = context;
        this.commonGroups = commonGroups;
    }

    public List<CommonGroup> getCommonGroups() {
        return commonGroups;
    }

    public void setCommonGroups(List<CommonGroup> commonGroups) {
        this.commonGroups = commonGroups;
    }

    public ClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.common_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommonGroupAdapter.ViewHolder holder, int position) {
        CommonGroup commonGroup = commonGroups.get(position);

        if (commonGroup.getImage() != null && !commonGroup.getImage().equals("")) {
            Glide.with(context).load(commonGroup.getImage()).into(holder.image);
        } else
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.no_pic));
        holder.title.setText(commonGroup.getTitle());
        holder.allMembers.setText(commonGroup.getAllMembers());

        holder.root.setOnClickListener(v -> {
            clickListener.onClick(position);
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
        return commonGroups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout root;
        ImageView image;
        TextView title, allMembers;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            root = itemView.findViewById(R.id.root);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            allMembers = itemView.findViewById(R.id.allMembers);
        }
    }

    public interface ClickListener {
        void onClick(int pos);
    }

}

package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.fragments.gallery_picker.GalleryPickerFragment;

import java.io.File;
import java.util.ArrayList;

public class GalleryPickerAdapter extends RecyclerView.Adapter<GalleryPickerAdapter.ViewHolder> {

    public ArrayList<String> mData;
    private LayoutInflater mInflater;
    Context context;

    public GalleryPickerAdapter(Context context, ArrayList<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mData = data;
    }

    @Override
    public GalleryPickerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_view_item_gallery, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryPickerAdapter.ViewHolder holder, final int position) {

        final String url = mData.get(position);
        File file = new File(url);
        Uri imageUri = Uri.fromFile(file);

        Glide.with(context)
                .load(imageUri)
                .into(holder.myTextView);

        holder.myTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.select.getVisibility() == TextView.GONE) {
                    if (GalleryPickerFragment.getTotalSelect() < 5) {
//                        view.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        holder.select.setVisibility(View.VISIBLE);
                        GalleryPickerFragment.setTotalSelect();
                        GalleryPickerFragment.addSelectItemList(url);
                    } else {
                        Toast.makeText(context, "Limit exceed", Toast.LENGTH_SHORT).show();
                    }
                } else {
//                    view.setBackgroundColor(Color.TRANSPARENT);
                    holder.select.setVisibility(View.GONE);
                    GalleryPickerFragment.setTotal();
                    GalleryPickerFragment.removeItem(url);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView myTextView, select;
        RelativeLayout view;
        TextView check;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.imageView);
            select = itemView.findViewById(R.id.selected);
            view = itemView.findViewById(R.id.main_layout);
        }
    }

}

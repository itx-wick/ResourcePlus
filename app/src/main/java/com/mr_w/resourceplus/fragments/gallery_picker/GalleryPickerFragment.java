package com.mr_w.resourceplus.fragments.gallery_picker;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.activities.chat_activity.ChatActivity;
import com.mr_w.resourceplus.adapter.GalleryPickerAdapter;
import com.mr_w.resourceplus.databinding.FragmentGalleryPickerBinding;

import java.util.ArrayList;

public class GalleryPickerFragment extends Fragment {

    FragmentGalleryPickerBinding mBinding;
    static int total_select = 0;
    static ArrayList<String> selectItem;
    static public TextView selected, done;

    public GalleryPickerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery_picker, container, false);
        selected = mBinding.selectedCount;
        done = mBinding.done;
        int orientation = StaggeredGridLayoutManager.VERTICAL;
        selectItem = new ArrayList<>();
        GalleryPickerAdapter gallery_viewAdapter = new GalleryPickerAdapter(getContext(), getAllShownImagesPath(getActivity()));
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        mBinding.gallery.setLayoutManager(layoutManager);
        mBinding.gallery.setAdapter(gallery_viewAdapter);

        mBinding.back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        done.setOnClickListener(view -> {
            if (total_select > 0) {
                ((ChatActivity) getActivity()).loadGif(selectItem);
                total_select = 0;
                selectItem.clear();
                getActivity().onBackPressed();
            }
        });

        return mBinding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private ArrayList<String> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            String extension = absolutePathOfImage.substring(absolutePathOfImage.lastIndexOf("."));
            if (extension.equals(".gif")) {
                listOfAllImages.add(absolutePathOfImage);
            }
        }
        return listOfAllImages;
    }

    public static void setTotalSelect() {
        total_select++;
        selected.setText(total_select + " Selected");
        if (total_select > 0) {
            selected.setVisibility(View.VISIBLE);
            done.setVisibility(View.VISIBLE);
        }
    }

    public static void setTotal() {
        total_select--;
        selected.setText(total_select + " Selected");
        if (total_select <= 0) {
            selected.setVisibility(View.GONE);
            done.setVisibility(View.GONE);
        }
    }

    static public int getTotalSelect() {
        return total_select;
    }

    static public void addSelectItemList(String url) {
        selectItem.add(url);
    }

    static public void removeItem(String url) {
        for (int i = 0; i < selectItem.size(); i++) {
            String urls = selectItem.get(i);
            if (urls.equals(url)) {
                selectItem.remove(i);
            }
        }
    }
}
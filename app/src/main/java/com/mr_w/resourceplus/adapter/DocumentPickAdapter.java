package com.mr_w.resourceplus.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mr_w.resourceplus.R;
import com.mr_w.resourceplus.utils.DocumentFile;
import com.mr_w.resourceplus.utils.ToastUtil;
import com.mr_w.resourceplus.utils.Util;

import java.util.ArrayList;

public class DocumentPickAdapter extends BaseAdapter<DocumentFile, DocumentPickAdapter.DocumentPickViewHolder> {
    private int mMaxNumber;
    private int mCurrentNumber = 0;

    public DocumentPickAdapter(Context ctx, int max) {
        this(ctx, new ArrayList<>(), max);
    }

    public DocumentPickAdapter(Context ctx, ArrayList<DocumentFile> list, int max) {
        super(ctx, list);
        mMaxNumber = max;
    }

    @Override
    public DocumentPickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_layout_item_normal_file_pick, parent, false);
        return new DocumentPickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final DocumentPickViewHolder holder, final int position) {
        final DocumentFile file = mList.get(position);

        holder.mTvTitle.setText(Util.extractFileNameWithSuffix(file.getPath()));
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth(mContext) - Util.dip2px(mContext, 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        holder.mCbx.setSelected(file.isSelected());

        if (file.getPath().endsWith("xls") || file.getPath().endsWith("xlsx")) {
            holder.mIvIcon.setImageResource(R.drawable.vw_ic_excel);
        } else if (file.getPath().endsWith("doc") || file.getPath().endsWith("docx")) {
            holder.mIvIcon.setImageResource(R.drawable.vw_ic_word);
        } else if (file.getPath().endsWith("ppt") || file.getPath().endsWith("pptx")) {
            holder.mIvIcon.setImageResource(R.drawable.vw_ic_ppt);
        } else if (file.getPath().endsWith("pdf")) {
            holder.mIvIcon.setImageResource(R.drawable.vw_ic_pdf);
        } else if (file.getPath().endsWith("txt")) {
            holder.mIvIcon.setImageResource(R.drawable.vw_ic_txt);
        } else {
            holder.mIvIcon.setImageResource(R.drawable.vw_ic_file);
        }

        holder.mCbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isSelected() && isUpToMax()) {
                    ToastUtil.getInstance(mContext).showToast(R.string.vw_up_to_max);
                    return;
                }

                if (v.isSelected()) {
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                } else {
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                }

                mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (!holder.mCbx.isSelected() && isUpToMax()) {
                ToastUtil.getInstance(mContext).showToast(R.string.vw_up_to_max);
                return;
            }

            if (holder.mCbx.isSelected()) {
                holder.mCbx.setSelected(false);
                mCurrentNumber--;
            } else {
                holder.mCbx.setSelected(true);
                mCurrentNumber++;
            }

            mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

            if (mListener != null) {
                mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class DocumentPickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private ImageView mCbx;

        public DocumentPickViewHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.ic_file);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_file_title);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
        }
    }

    private boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

}
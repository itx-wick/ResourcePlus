package com.mr_w.resourceplus.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mr_w.resourceplus.R;

public class RecyclerViewDividerLinear extends RecyclerView.ItemDecoration {

    // Used to draw item split line.
    protected Drawable itemDividerDrawable;

    // Item split line orientation.
    protected int itemDividerOrientation;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RecyclerViewDividerLinear(Context ctx, int orientation) {
        itemDividerDrawable = ctx.getDrawable(R.drawable.gradient);
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != LinearLayoutManager.HORIZONTAL && orientation != LinearLayoutManager.VERTICAL) {
            throw new IllegalArgumentException("invalid orientation");
        }
        itemDividerOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (itemDividerOrientation == LinearLayoutManager.VERTICAL) {
            drawVertical(canvas, parent);
        } else {
            drawHorizontal(canvas, parent);
        }
    }

    public void drawVertical(Canvas canvas, RecyclerView parent) {
        int leftPoint = parent.getPaddingLeft();
        int rightPoint = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            //android.support.v7.widget.RecyclerView v = new android.support.v7.widget.RecyclerView(parent.getContext());
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            int topPoint = child.getBottom() + layoutParams.bottomMargin;
            int bottomPoint = topPoint + itemDividerDrawable.getIntrinsicHeight();

            itemDividerDrawable.setBounds(leftPoint, topPoint, rightPoint, bottomPoint);
            itemDividerDrawable.draw(canvas);
        }
    }

    public void drawHorizontal(Canvas canvas, RecyclerView parent) {
        int topPoint = parent.getPaddingTop();
        int bottomPoint = parent.getHeight() - parent.getPaddingBottom();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();

            int leftPoint = child.getRight() + layoutParams.rightMargin;
            int rightPoint = leftPoint + itemDividerDrawable.getIntrinsicHeight();

            itemDividerDrawable.setBounds(leftPoint, topPoint, rightPoint, bottomPoint);
            itemDividerDrawable.draw(canvas);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (this.itemDividerOrientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, this.itemDividerDrawable.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, this.itemDividerDrawable.getIntrinsicWidth(), 0);
        }
    }
}

package com.monowar.firebasechatapp.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;


/**
 * Created by mm on 8/11/2016.
 */
public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private final int mVerticalSpaceHeight;
    private Context context;

    public VerticalSpaceItemDecoration(Context context, int mVerticalSpaceHeight) {
        this.context = context;
        this.mVerticalSpaceHeight = mVerticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int reduceGap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
        outRect.bottom = mVerticalSpaceHeight;
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = mVerticalSpaceHeight;
        }
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = mVerticalSpaceHeight;
        }
    }
}
package com.vstudio.filtercamera;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterScaleLayoutManager extends LinearLayoutManager {

    private final float shrinkAmount = 0.3f;
    private final float shrinkDistance = 0.9f;

    public CenterScaleLayoutManager(Context context) {
        super(context, HORIZONTAL, false);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        scaleChildren();
    }

    @Override
    public int scrollHorizontallyBy(
            int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
        scaleChildren();
        return scrolled;
    }

    private void scaleChildren() {
        float mid = getWidth() / 2f;
        float d0 = 0f;
        float d1 = shrinkDistance * mid;
        float s0 = 1f;
        float s1 = 1f - shrinkAmount;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            float childMid =
                    (getDecoratedLeft(child) + getDecoratedRight(child)) / 2f;
            float d = Math.min(d1, Math.abs(mid - childMid));
            float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
            child.setScaleX(scale);
            child.setScaleY(scale);
        }
    }
}


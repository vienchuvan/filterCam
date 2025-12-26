package com.vstudio.filtercamera.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.vstudio.filtercamera.R;
import com.vstudio.filtercamera.modal.FilterItem;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.Holder> {

    public interface OnFilterSelect {
        void onSelect(GPUImageFilter filter, int position);
    }

    private final List<FilterItem> list;
    private final OnFilterSelect callback;
    private int selectedPosition = 0;

    private static final int PAYLOAD_SELECT = 1;

    public FilterAdapter(List<FilterItem> list, OnFilterSelect cb) {
        this.list = list;
        this.callback = cb;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder h, int pos) {
        h.bind(list.get(pos), pos == selectedPosition, false);
    }

    @Override
    public void onBindViewHolder(
            Holder h,
            int pos,
            List<Object> payloads) {

        if (!payloads.isEmpty()) {
            h.bind(list.get(pos), pos == selectedPosition, true);
        } else {
            onBindViewHolder(h, pos);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setSelectedPosition(int position) {
        int old = selectedPosition;
        selectedPosition = position;

        notifyItemChanged(old, PAYLOAD_SELECT);
        notifyItemChanged(position, PAYLOAD_SELECT);
    }

    class Holder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name;

        Holder(View v) {
            super(v);
            img = v.findViewById(R.id.imgPreviewFiller);
            name = v.findViewById(R.id.txtNameFiller);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;

                setSelectedPosition(pos);
                callback.onSelect(list.get(pos).filter, pos);
            });
        }

        void bind(FilterItem f, boolean selected, boolean payloadOnly) {
            if (!payloadOnly) {
                img.setImageBitmap(f.thumbnail);
                name.setText(f.name);
            }

            img.animate().cancel();
            itemView.animate().cancel();

            if (selected) {
                itemView.setAlpha(1f);
                name.setTextColor(Color.WHITE);

                img.animate()
                        .scaleX(1.15f)
                        .scaleY(1.15f)
                        .setDuration(160)
                        .start();
            } else {
                itemView.setAlpha(0.55f);
                name.setTextColor(Color.parseColor("#AAAAAA"));

                img.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .start();
            }
        }
    }
}
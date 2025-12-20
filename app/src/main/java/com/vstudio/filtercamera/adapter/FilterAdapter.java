package com.vstudio.filtercamera.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vstudio.filtercamera.R;
import com.vstudio.filtercamera.modal.FilterItem;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class FilterAdapter
        extends RecyclerView.Adapter<FilterAdapter.Holder> {

    // ===== CALLBACK =====
    public interface OnFilterSelect {
        void onSelect(GPUImageFilter filter);
    }

    private final List<FilterItem> list;
    private final OnFilterSelect callback;

    public FilterAdapter(List<FilterItem> list,
                         OnFilterSelect callback) {
        this.list = list;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_filter, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull Holder holder, int position) {

        FilterItem item = list.get(position);
        holder.tv.setText(item.name);

        holder.itemView.setOnClickListener(v -> {
            if (callback != null) {
                callback.onSelect(item.filter);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ===== VIEW HOLDER =====
    static class Holder extends RecyclerView.ViewHolder {
        TextView tv;
        ImageView img;

        Holder(View v) {
            super(v);
            tv = v.findViewById(R.id.tv);
            img = v.findViewById(R.id.img);
        }
    }
}

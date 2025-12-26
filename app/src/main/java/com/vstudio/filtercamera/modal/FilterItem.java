package com.vstudio.filtercamera.modal;

import android.graphics.Bitmap;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class FilterItem {
    public String name;
    public GPUImageFilter filter;
    public Bitmap thumbnail;
    public FilterCategory category;

    public FilterItem(String name, GPUImageFilter filter, FilterCategory category) {
        this.name = name;
        this.filter = filter;
        this.category = category;
    }
}
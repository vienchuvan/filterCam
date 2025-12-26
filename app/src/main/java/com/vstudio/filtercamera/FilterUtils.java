package com.vstudio.filtercamera;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorBalanceFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageRGBFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSmoothToonFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVibranceFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageWhiteBalanceFilter;

public class FilterUtils {
    public static Bitmap brightness(Bitmap src, float value) {

        // ⚠️ BẮT BUỘC: convert sang ARGB_8888
        Bitmap safeBitmap = src.copy(Bitmap.Config.ARGB_8888, true);

        Bitmap bmp = Bitmap.createBitmap(
                safeBitmap.getWidth(),
                safeBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix(new float[]{
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(safeBitmap, 0, 0, paint);

        return bmp;
    }

    public static Bitmap blackWhite(Bitmap src) {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        return applyMatrix(src, cm);
    }

    public static Bitmap vintage(Bitmap src) {
        ColorMatrix cm = new ColorMatrix(new float[]{
                1.2f, 0, 0, 0, 10,
                0, 1.0f, 0, 0, 5,
                0, 0, 0.8f, 0, 0,
                0, 0, 0, 1, 0
        });
        return applyMatrix(src, cm);
    }

    private static Bitmap applyMatrix(Bitmap src, ColorMatrix cm) {

        Bitmap safeBitmap = src.copy(Bitmap.Config.ARGB_8888, true);

        Bitmap bmp = Bitmap.createBitmap(
                safeBitmap.getWidth(),
                safeBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(safeBitmap, 0, 0, paint);

        return bmp;
    }

// chân dung
//
//
//
//    public static GPUImageFilterGroup portraitSoft() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageBrightnessFilter(0.1f));
//        g.addFilter(new GPUImageSaturationFilter(1.2f));
//        g.addFilter(new GPUImageContrastFilter(1.1f));
//        return g;
//    }
//
//    public static GPUImageFilterGroup portraitWarm() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageWhiteBalanceFilter(5000f, 1f));
//        g.addFilter(new GPUImageSmoothToonFilter());
//        return g;
//    }
//
//    public static GPUImageFilterGroup landscapeHDR() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageContrastFilter(1.4f));
//        g.addFilter(new GPUImageSaturationFilter(1.5f));
//        g.addFilter(new GPUImageSharpenFilter(1.2f));
//        return g;
//    }
//
//    public static GPUImageFilterGroup landscapeClear() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageExposureFilter(0.2f));
//        g.addFilter(new GPUImageSharpenFilter(1.0f));
//        return g;
//    }
//
//
//    public static GPUImageFilterGroup natureGreen() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageHueFilter(10f));
//        g.addFilter(new GPUImageSaturationFilter(1.6f));
//        return g;
//    }
//
//    public static GPUImageFilterGroup vintage70() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageSepiaToneFilter(0.3f));
//        g.addFilter(new GPUImageVignetteFilter());
//        return g;
//    }
//
//    public static GPUImageFilterGroup vintageFilm() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageGammaFilter(1.3f));
////        g.addFilter(new GPUImageColorBalanceFilter( new float[]{0.05f, 0f, -0.05f} ));
//        return g;
//    }
//
//    public static GPUImageFilterGroup bwContrast() {
//        GPUImageFilterGroup g = new GPUImageFilterGroup();
//        g.addFilter(new GPUImageGrayscaleFilter());
//        g.addFilter(new GPUImageContrastFilter(1.6f));
//        return g;
//    }

    // 1️⃣ Mềm da tự nhiên
    public static GPUImageFilterGroup portraitSoft() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageBrightnessFilter(0.1f));
        g.addFilter(new GPUImageSaturationFilter(1.15f));
        g.addFilter(new GPUImageContrastFilter(1.1f));
        return g;
    }

    // 2️⃣ Da ấm
    public static GPUImageFilterGroup portraitWarm() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageWhiteBalanceFilter(5200f, 1.0f));
        g.addFilter(new GPUImageSmoothToonFilter());
        return g;
    }

    // 3️⃣ Sáng mặt (beauty nhẹ)
    public static GPUImageFilterGroup portraitBright() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageExposureFilter(0.15f));
        g.addFilter(new GPUImageContrastFilter(1.1f));
        return g;
    }

    // 4️⃣ Tông lạnh Hàn Quốc
    public static GPUImageFilterGroup portraitCool() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageWhiteBalanceFilter(7000f, 0.8f));
        g.addFilter(new GPUImageSaturationFilter(1.1f));
        return g;
    }

    // 5️⃣ Da mịn + sáng
    public static GPUImageFilterGroup portraitGlow() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageGaussianBlurFilter(1.2f));
        g.addFilter(new GPUImageBrightnessFilter(0.08f));
        return g;
    }

    // 1️⃣ HDR mạnh
    public static GPUImageFilterGroup landscapeHDR() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageContrastFilter(1.4f));
        g.addFilter(new GPUImageSaturationFilter(1.5f));
        g.addFilter(new GPUImageSharpenFilter(1.2f));
        return g;
    }

    // 2️⃣ Trời xanh
    public static GPUImageFilterGroup landscapeClear() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageExposureFilter(0.2f));
        g.addFilter(new GPUImageSharpenFilter(1.0f));
        return g;
    }

    // 3️⃣ Xanh tươi
    public static GPUImageFilterGroup landscapeVivid() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageSaturationFilter(1.6f));
        g.addFilter(new GPUImageContrastFilter(1.2f));
        return g;
    }

    // 4️⃣ Điện ảnh
    public static GPUImageFilterGroup landscapeCinematic() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();

        // Giảm sáng nhẹ (cinematic)
        g.addFilter(new GPUImageBrightnessFilter(-0.05f));

        // Tăng tương phản
        g.addFilter(new GPUImageContrastFilter(1.35f));

        // Giảm saturation cho tone điện ảnh
        g.addFilter(new GPUImageSaturationFilter(0.85f));

        // Color grading nhẹ (teal & orange an toàn)
        g.addFilter(new GPUImageRGBFilter(
                1.05f,   // R
                1.0f,    // G
                0.95f    // B
        ));

        return g;
    }

    // 5️⃣ Sắc nét
    public static GPUImageFilterGroup landscapeSharp() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageSharpenFilter(1.5f));
        return g;
    }


    // 1️⃣ Xanh lá
    public static GPUImageFilterGroup natureGreen() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageHueFilter(10f));
        g.addFilter(new GPUImageSaturationFilter(1.6f));
        return g;
    }

    // 2️⃣ Rừng sâu
    public static GPUImageFilterGroup natureForest() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageContrastFilter(1.2f));
        g.addFilter(new GPUImageSaturationFilter(1.4f));
        return g;
    }

    // 3️⃣ Tươi mát
    public static GPUImageFilterGroup natureFresh() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageExposureFilter(0.1f));
        g.addFilter(new GPUImageSaturationFilter(1.3f));
        return g;
    }

    // 4️⃣ Nắng nhẹ
    public static GPUImageFilterGroup natureSunny() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageBrightnessFilter(0.12f));
        g.addFilter(new GPUImageVibranceFilter(0.6f));
        return g;
    }


    // 1️⃣ Vintage 70s
    public static GPUImageFilterGroup vintage70() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageSepiaToneFilter(0.3f));
        g.addFilter(new GPUImageVignetteFilter());
        return g;
    }

    // 2️⃣ Phim cũ
    public static GPUImageFilterGroup vintageFilm() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageGammaFilter(1.3f));
        return g;
    }

    // 3️⃣ Retro nâu
    public static GPUImageFilterGroup vintageBrown() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageSepiaToneFilter(0.4f));
        g.addFilter(new GPUImageContrastFilter(1.1f));
        return g;
    }

    // 4️⃣ Ám vàng
    public static GPUImageFilterGroup vintageWarm() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageWhiteBalanceFilter(4500f, 1f));
        return g;
    }


    // 1️⃣ Tương phản mạnh
    public static GPUImageFilterGroup bwContrast() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageGrayscaleFilter());
        g.addFilter(new GPUImageContrastFilter(1.6f));
        return g;
    }

    // 2️⃣ Mềm
    public static GPUImageFilterGroup bwSoft() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageGrayscaleFilter());
        g.addFilter(new GPUImageGammaFilter(1.2f));
        return g;
    }

    // 3️⃣ Điện ảnh
    public static GPUImageFilterGroup bwCinema() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageGrayscaleFilter());
        g.addFilter(new GPUImageVignetteFilter());
        return g;
    }

    // 4️⃣ Sắc nét
    public static GPUImageFilterGroup bwSharp() {
        GPUImageFilterGroup g = new GPUImageFilterGroup();
        g.addFilter(new GPUImageGrayscaleFilter());
        g.addFilter(new GPUImageSharpenFilter(1.3f));
        return g;
    }




    public static int calculateInSampleSize(
            BitmapFactory.Options options,
            int reqWidth,
            int reqHeight) {

        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        while ((height / inSampleSize) > reqHeight
                || (width / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }
}

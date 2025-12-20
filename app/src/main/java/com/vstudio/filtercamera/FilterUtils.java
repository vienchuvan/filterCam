package com.vstudio.filtercamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

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
}

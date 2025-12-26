package com.vstudio.filtercamera.modal;

import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class ImageUtils {
    public static byte[] yuv420ToNV21(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        ImageProxy.PlaneProxy yPlane = planes[0];
        ImageProxy.PlaneProxy uPlane = planes[1];
        ImageProxy.PlaneProxy vPlane = planes[2];

        ByteBuffer yBuffer = yPlane.getBuffer();
        ByteBuffer uBuffer = uPlane.getBuffer();
        ByteBuffer vBuffer = vPlane.getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + (ySize / 2)];

        // Copy Plane Y
        yBuffer.get(nv21, 0, ySize);

        // Đan xen V và U (NV21 yêu cầu định dạng V-U-V-U)
        // Lưu ý: Chúng ta lấy dữ liệu từ Plane V và Plane U để đan xen vào mảng nv21
        int vRowStride = vPlane.getRowStride();
        int vPixelStride = vPlane.getPixelStride();
        int uRowStride = uPlane.getRowStride();
        int uPixelStride = uPlane.getPixelStride();

        int width = image.getWidth();
        int height = image.getHeight();

        int pos = ySize;
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                // Lấy V
                nv21[pos++] = vBuffer.get(row * vRowStride + col * vPixelStride);
                // Lấy U
                nv21[pos++] = uBuffer.get(row * uRowStride + col * uPixelStride);
            }
        }
        return nv21;
    }
}

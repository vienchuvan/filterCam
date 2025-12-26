package com.vstudio.filtercamera;

import android.graphics.SurfaceTexture;
import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageRenderer;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public class CustomGPUImageRenderer extends GPUImageRenderer {
    private SurfaceTexture mSurfaceTexture;
    private int mTextureId = -1;

    public CustomGPUImageRenderer(GPUImageFilter filter) {
        super(filter);
    }

//    public CustomGPUImageRenderer(GPUImage gpuImage) {
//        super(gpuImage);
//    }

    // Hàm này sẽ trả về SurfaceTexture mà Renderer đang dùng
    public SurfaceTexture getCustomSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 unused, javax.microedition.khronos.egl.EGLConfig config) {
        super.onSurfaceCreated(unused, config);
        // Sau khi super chạy, nó sẽ tạo ra Texture ID và SurfaceTexture bên trong
        // Chúng ta cần soi vào mã nguồn để lấy nó ra hoặc tự tạo ở đây
    }
}
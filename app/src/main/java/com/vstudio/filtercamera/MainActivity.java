package com.vstudio.filtercamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.vstudio.filtercamera.adapter.FilterAdapter;
import com.vstudio.filtercamera.modal.FilterItem;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Environment;
import android.widget.ImageView;

import androidx.camera.core.ImageCaptureException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import androidx.camera.core.Camera;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ScaleGestureDetector;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSepiaToneFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageVignetteFilter;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 1001;
    private ImageView btnShutter;
    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ScaleGestureDetector scaleGestureDetector;
    ActivityResultLauncher<String> imagePicker;
    private List<FilterItem> filters = Arrays.asList(
            new FilterItem("Normal", new GPUImageFilter()),
            new FilterItem("Bright", new GPUImageBrightnessFilter(0.2f)),
            new FilterItem("Contrast", new GPUImageContrastFilter(1.4f)),
            new FilterItem("BW", new GPUImageGrayscaleFilter()),
            new FilterItem("Sepia", new GPUImageSepiaToneFilter()),
            new FilterItem("Sketch", new GPUImageSketchFilter()),
            new FilterItem("Vignette", new GPUImageVignetteFilter())
    );
    RecyclerView filterRecycler;
    PagerSnapHelper snapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        scaleGestureDetector = new ScaleGestureDetector(
                this,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {

                        if (camera == null) return false;

                        float scale = detector.getScaleFactor();

                        float currentZoom =
                                camera.getCameraInfo()
                                        .getZoomState()
                                        .getValue()
                                        .getZoomRatio();

                        float newZoom = currentZoom * scale;

                        camera.getCameraControl().setZoomRatio(newZoom);

                        return true;
                    }
                }
        );
        previewView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
        btnShutter = findViewById(R.id.btnShutter);
        LinearLayout btnPick = findViewById(R.id.btnPick);

        // ===== Pick image from gallery =====
        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        Intent intent = new Intent(this, EditImageActivity.class);
                        intent.putExtra("image", uri.toString());
                        startActivity(intent);
                    }
                });

        btnPick.setOnClickListener(v -> imagePicker.launch("image/*"));

        // ===== Camera permission =====
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CAMERA
            );
        }


        btnShutter.setOnClickListener(v -> {

            // 👉 Hiệu ứng bấm nút
            btnShutter.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(80)
                    .withEndAction(() ->
                            btnShutter.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .start()
                    ).start();

            // 👉 Chụp ảnh
            takePhoto();
        });
        // ===== Insets =====

        LinearLayout filterPanel = findViewById(R.id.filterPanel);
        filterRecycler = findViewById(R.id.filterRecycler);

// LayoutManager dạng carousel
        CenterScaleLayoutManager lm =
                new CenterScaleLayoutManager(this
                );

        filterRecycler.setLayoutManager(lm);

// Snap helper (GIỐNG TikTok)
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(filterRecycler);

// Adapter (KHÔNG lambda Consumer)
        filterRecycler.setAdapter(
                new FilterAdapter(filters,
                        new FilterAdapter.OnFilterSelect() {
                            @Override
                            public void onSelect(GPUImageFilter filter) {
//                                gpuImage.setFilter(filter);
                            }
                        })
        );
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(filterRecycler);
        LinearLayout btnFilter = findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(v -> {

            if (filterPanel.getVisibility() == View.VISIBLE) {
                filterPanel.setVisibility(View.GONE);
            } else {
                filterPanel.setVisibility(View.VISIBLE);
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // ================= CAMERA =================
    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Image Capture (chuẩn bị cho nút chụp)
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }, ContextCompat.getMainExecutor(this));
    }

    // ================= PERMISSION =================
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CAMERA &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        }
    }

    private void takePhoto() {

        if (imageCapture == null) return;

        File photoDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                "FilterCamera"
        );

        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        String fileName = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
        ).format(System.currentTimeMillis()) + ".jpg";

        File photoFile = new File(photoDir, fileName);

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults output) {

                        Uri uri = Uri.fromFile(photoFile);

                        // 👉 Mở màn chỉnh ảnh
                        Intent intent = new Intent(
                                MainActivity.this,
                                EditImageActivity.class
                        );
                        intent.putExtra("image", uri.toString());
                        startActivity(intent);
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                }
        );
    }

}

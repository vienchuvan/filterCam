package com.vstudio.filtercamera;

import static com.vstudio.filtercamera.FilterUtils.calculateInSampleSize;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.vstudio.filtercamera.adapter.FilterAdapter;
import com.vstudio.filtercamera.modal.CameraMode;
import com.vstudio.filtercamera.modal.FilterCategory;
import com.vstudio.filtercamera.modal.FilterItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Environment;
import android.widget.ImageView;

import androidx.camera.core.ImageCaptureException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ScaleGestureDetector;
import android.widget.TextView;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import jp.co.cyberagent.android.gpuimage.filter.*;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 1001;
    private static final String TAG = "MainActivity";

    private ImageView btnShutter;
    private GPUImageView gpuImageView;
    private ImageCapture imageCapture;
    private Camera camera;
    private ScaleGestureDetector scaleGestureDetector;
    private ActivityResultLauncher<String> imagePicker;
    private List<FilterItem> filters;
    private RecyclerView filterRecycler;
    private LinearLayout filterPanel;
    private PagerSnapHelper snapHelper = new PagerSnapHelper();
    private FrameLayout cameraPreviewFrame;
private TextView btnFilm, btnPhoto, btnVideo;
    private boolean isRecording = false;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
private ImageView btnLatCamera;
    private GPUImageFilter currentFilter;
    private int selectedFilterPosition = 0;
    private FilterAdapter filterAdapter;
    private CameraMode currentMode = CameraMode.PHOTO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        cameraPreviewFrame = findViewById(R.id.cameraPreview);

        // ✨ TẠO GPUImageView để hiển thị camera với filter
        gpuImageView = new GPUImageView(this);
        gpuImageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        cameraPreviewFrame.addView(gpuImageView, 0);

        currentFilter = new GPUImageFilter();
        gpuImageView.setFilter(currentFilter);

        btnShutter = findViewById(R.id.btnShutter);
        LinearLayout btnPick = findViewById(R.id.btnPick);

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
            if (currentMode == CameraMode.VIDEO) {

            } else { btnShutter.animate()
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
                takePhoto();
//                takePhoto();
            }

        });



        filterRecycler = findViewById(R.id.filterRecycler);
        filterPanel = findViewById(R.id.filterPanel);

        filterRecycler.setLayoutManager(
                new LinearLayoutManager(this,
                        RecyclerView.HORIZONTAL, false));

        loadFilters();

        LinearLayout btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> {
            if (filterPanel.getVisibility() == View.VISIBLE) {
                filterPanel.setVisibility(View.GONE);
            } else {
                filterPanel.setVisibility(View.VISIBLE);
            }
        });

         btnFilm  = findViewById(R.id.btnFilm);
         btnPhoto = findViewById(R.id.btnPhoto);
         btnVideo = findViewById(R.id.btnVideo);
        View.OnClickListener modeClick = v -> {
            if (v == btnFilm)  switchMode(CameraMode.FILM);
            if (v == btnPhoto) switchMode(CameraMode.PHOTO);
            if (v == btnVideo) switchMode(CameraMode.VIDEO);
        };

        btnFilm.setOnClickListener(modeClick);
        btnPhoto.setOnClickListener(modeClick);
        btnVideo.setOnClickListener(modeClick);

         btnLatCamera = findViewById(R.id.btnLatCamera);

        btnLatCamera.setOnClickListener(v -> {
            btnLatCamera.animate()
                    .rotationBy(180f)
                    .setDuration(300)
                    .start();

            switchCamera();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startCamera() {
        try {
            camera = Camera.open(currentCameraId);

            Camera.Parameters params = camera.getParameters();

            Camera.Size bestSize = params.getSupportedPreviewSizes().get(0);
            for (Camera.Size size : params.getSupportedPreviewSizes()) {
                if (size.width >= 1280 && size.width <= 1920) {
                    bestSize = size;
                    break;
                }
            }

            params.setPreviewSize(bestSize.width, bestSize.height);

            if (params.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            camera.setParameters(params);

            // ⭐ BẮT BUỘC startPreview trước
            camera.startPreview();

            boolean isFront =
                    currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;

            // ⭐ FIX ĐEN CAMERA TRƯỚC
            gpuImageView.setUpCamera(
                    camera,
                    isFront ? 270 : 90, // ⬅️ QUAN TRỌNG
                    isFront,            // mirror preview cho cam trước
                    false
            );

        } catch (Exception e) {
            Log.e("Camera", "startCamera failed", e);
        }
    }
    private Bitmap getBaseBitmap(FilterCategory category) {
        int resId;

        switch (category) {
            case PORTRAIT:
                resId = R.drawable.chandung;
                break;
            case LANDSCAPE:
                resId = R.drawable.phongcanh;
                break;
            case NATURE:
                resId = R.drawable.caycoi;
                break;
            case VINTAGE:
                resId = R.drawable.chill;
                break;
            case BW:
                resId = R.drawable.dentrang;
                break;
            default:
                resId = R.drawable.thum;
        }

        return decodeThumb(resId, 200);
    }
    private Bitmap decodeThumb(int resId, int reqSize) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), resId, opts);

        opts.inSampleSize = calculateInSampleSize(opts, reqSize, reqSize);
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig = Bitmap.Config.RGB_565; // giảm 1/2 RAM

        return BitmapFactory.decodeResource(getResources(), resId, opts);
    }
    private void loadFilters() {
        Executors.newSingleThreadExecutor().execute(() -> {

            filters = new ArrayList<>();

            // ===== PORTRAIT =====
            filters.add(new FilterItem("Soft Skin",
                    FilterUtils.portraitSoft(), FilterCategory.PORTRAIT));
//            filters.add(new FilterItem("Warm Face",
//                    FilterUtils.portraitWarm(), FilterCategory.PORTRAIT));
            filters.add(new FilterItem("Bright Face",
                    FilterUtils.portraitBright(), FilterCategory.PORTRAIT));
            filters.add(new FilterItem("Cool Face",
                    FilterUtils.portraitCool(), FilterCategory.PORTRAIT));
            filters.add(new FilterItem("Glow Face",
                    FilterUtils.portraitGlow(), FilterCategory.PORTRAIT));
            // ===== LANDSCAPE =====
            filters.add(new FilterItem("HDR Land",
                    FilterUtils.landscapeHDR(), FilterCategory.LANDSCAPE));
            filters.add(new FilterItem("Clear Sky",
                    FilterUtils.landscapeClear(), FilterCategory.LANDSCAPE));
            filters.add(new FilterItem("Vivid Sky",
                    FilterUtils.landscapeVivid(), FilterCategory.LANDSCAPE));
            filters.add(new FilterItem("Cinematic",
                    FilterUtils.landscapeCinematic(), FilterCategory.LANDSCAPE));
            filters.add(new FilterItem("Sharp",
                    FilterUtils.landscapeSharp(), FilterCategory.LANDSCAPE));
            // ===== NATURE =====
            filters.add(new FilterItem("Green Boost",
                    FilterUtils.natureGreen(), FilterCategory.NATURE));
            filters.add(new FilterItem("Green Fresh",
                    FilterUtils.natureFresh(), FilterCategory.NATURE));
            filters.add(new FilterItem("Green Sunny",
                    FilterUtils.natureSunny(), FilterCategory.NATURE));

            // ===== VINTAGE =====
            filters.add(new FilterItem("Vintage 70s",
                    FilterUtils.vintage70(), FilterCategory.VINTAGE));
            filters.add(new FilterItem("Old Film",
                    FilterUtils.vintageFilm(), FilterCategory.VINTAGE));
            filters.add(new FilterItem("Vintage Brown",
                    FilterUtils.vintageBrown(), FilterCategory.VINTAGE));
            filters.add(new FilterItem("Warm",
                    FilterUtils.vintageWarm(), FilterCategory.VINTAGE));
            // ===== BW =====
            filters.add(new FilterItem("Classic BW",
                    new GPUImageGrayscaleFilter(), FilterCategory.BW));
            filters.add(new FilterItem("High Contrast BW",
                    FilterUtils.bwContrast(), FilterCategory.BW));
            filters.add(new FilterItem("Soft BW",
                    FilterUtils.bwSoft(), FilterCategory.BW));
            filters.add(new FilterItem("Cinema BW",
                    FilterUtils.bwCinema(), FilterCategory.BW));
            filters.add(new FilterItem("Sharp BW",
                    FilterUtils.bwSharp(), FilterCategory.BW));
            // ✅ TẠO THUMBNAIL AN TOÀN
            for (FilterItem f : filters) {
                Bitmap base = getBaseBitmap(f.category);

                GPUImage gpu = new GPUImage(this);
                gpu.setImage(base);
                gpu.setFilter(f.filter);

                Bitmap thumb = gpu.getBitmapWithFilterApplied();
                f.thumbnail = Bitmap.createScaledBitmap(thumb, 160, 160, true);
            }

            runOnUiThread(this::setupRecycler);
        });
    }

    private void setupRecycler() {
        LinearLayoutManager lm = new LinearLayoutManager(
                this,
                RecyclerView.HORIZONTAL,
                false);

        filterRecycler.setLayoutManager(lm);

        filterAdapter = new FilterAdapter(filters, new FilterAdapter.OnFilterSelect() {
            @Override
            public void onSelect(GPUImageFilter filter, int position) {
                currentFilter = filter;
                selectedFilterPosition = position;

                // ✨ ÁP DỤNG FILTER REAL-TIME
                gpuImageView.setFilter(filter);

                Log.d(TAG, "Filter applied: " + filters.get(position).name);
                filterAdapter.setSelectedPosition(position);
            }
        });

        filterRecycler.setAdapter(filterAdapter);
        snapHelper.attachToRecyclerView(filterRecycler);
    }

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
        // ✨ Chụp ảnh với filter hiện tại
        gpuImageView.saveToPictures("FilterCamera",
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                        .format(System.currentTimeMillis()) + ".jpg",
                new GPUImageView.OnPictureSavedListener() {
                    @Override
                    public void onPictureSaved(Uri uri) {
                        // Mở màn edit với ảnh đã chụp
                        Intent intent = new Intent(
                                MainActivity.this,
                                EditImageActivity.class
                        );
                        intent.putExtra("image", uri.toString());
                        intent.putExtra("filterPosition", selectedFilterPosition);
                        startActivity(intent);
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCameraSafe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            recreateGPUImageView();
            startCamera();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (camera != null) {
            camera.release();
            camera = null;
        }
    }



    private void toggleVideo() {
        if (!isRecording) {
            // startRecordVideo();
            isRecording = true;
            btnShutter.setImageResource(R.drawable.ic_recording);
        } else {
            // stopRecordVideo();
            isRecording = false;
            btnShutter.setImageResource(R.drawable.ic_shutter_video);
        }
    }
    private void switchMode(CameraMode mode) {
        currentMode = mode;

        btnFilm.setTextColor(mode == CameraMode.FILM ? Color.BLACK : 0x88AAA);
        btnPhoto.setTextColor(mode == CameraMode.PHOTO ? Color.BLACK : 0x88AAA);
        btnVideo.setTextColor(mode == CameraMode.VIDEO ? Color.BLACK : 0x88AAA);

        if (mode == CameraMode.FILM) {
            applyFilmPreset();
            btnShutter.setImageResource(R.drawable.bg_shutter);
        }

        if (mode == CameraMode.PHOTO) {
            gpuImageView.setFilter(currentFilter);
            btnShutter.setImageResource(R.drawable.bg_shutter);
        }

        if (mode == CameraMode.VIDEO) {
            btnShutter.setImageResource(R.drawable.ic_shutter_video);
        }
    }
    private void applyFilmPreset() {
        GPUImageFilterGroup film = new GPUImageFilterGroup();
        film.addFilter(new GPUImageContrastFilter(1.2f));
        film.addFilter(new GPUImageSaturationFilter(1.3f));
        film.addFilter(new GPUImageSepiaToneFilter(0.2f));
        film.addFilter(new GPUImageVignetteFilter());

        gpuImageView.setFilter(film);
    }


    private boolean isSwitching = false;

    private void switchCamera() {
        if (isSwitching) return;
        isSwitching = true;

        runOnUiThread(() -> {
            // 1️⃣ Release camera an toàn
            releaseCameraSafe();

            // 2️⃣ Đổi camera
            currentCameraId =
                    (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
                            ? Camera.CameraInfo.CAMERA_FACING_FRONT
                            : Camera.CameraInfo.CAMERA_FACING_BACK;

            // 3️⃣ Recreate GPUImageView (QUAN TRỌNG)
            recreateGPUImageView();

            // 4️⃣ Open camera mới
            startCamera();

            isSwitching = false;
        });
    }



    private int getCameraRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: return 90;
            case Surface.ROTATION_90: return 0;
            case Surface.ROTATION_180: return 270;
            case Surface.ROTATION_270: return 180;
        }
        return 90;
    }
    private void recreateGPUImageView() {
        cameraPreviewFrame.removeAllViews();

        gpuImageView = new GPUImageView(this);
        gpuImageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        gpuImageView.setFilter(currentFilter);

        cameraPreviewFrame.addView(gpuImageView, 0);
    }
    private void releaseCameraSafe() {
        try {
            if (camera != null) {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        } catch (Exception ignored) {}
    }
}
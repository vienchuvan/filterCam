package com.vstudio.filtercamera;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditImageActivity extends AppCompatActivity {

    ImageView imageView;
    SeekBar seek;

    Bitmap originalBitmap;
    Bitmap currentBitmap;
    Bitmap previewBitmap;

    EditMode currentMode = EditMode.BRIGHTNESS;

    enum EditMode {
        BRIGHTNESS,
        CONTRAST,
        SATURATION,
        SHARPNESS,
        TEMPERATURE,
        FADE,
        BLUR,
        GRAIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_image);

        imageView = findViewById(R.id.imageView);
        seek = findViewById(R.id.seekBrightness);

        LinearLayout btnBrightness = findViewById(R.id.btnBrightness);
        LinearLayout btnContrast = findViewById(R.id.btnContrast);
        LinearLayout btnColor = findViewById(R.id.btnColor);
        LinearLayout btnSharp = findViewById(R.id.btnSharp);
        LinearLayout btnWarm = findViewById(R.id.btnWarm);
        LinearLayout btnBlur = findViewById(R.id.btnBlur);
        LinearLayout btnGrain = findViewById(R.id.btnGrain);

        TextView btnSave = findViewById(R.id.btnSave);

        try {
            Uri uri = Uri.parse(getIntent().getStringExtra("image"));
            if (Build.VERSION.SDK_INT >= 28) {
                originalBitmap = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(getContentResolver(), uri));
            } else {
                originalBitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), uri);
            }
            currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(currentBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        seek.setMax(200);
        seek.setProgress(100);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float value = progress - 100;

                switch (currentMode) {
                    case BRIGHTNESS:
                        previewBitmap = FilterUtils.brightness(currentBitmap, value);
                        break;
                    case CONTRAST:
                        previewBitmap = FilterUtils.contrast(currentBitmap, progress / 100f);
                        break;
                    case SATURATION:
                        previewBitmap = FilterUtils.saturation(currentBitmap, progress / 100f);
                        break;
                    case SHARPNESS:
                        previewBitmap = FilterUtils.sharpness(currentBitmap, progress / 50f);
                        break;
                    case TEMPERATURE:
                        previewBitmap = FilterUtils.temperature(currentBitmap, value);
                        break;
                    case FADE:
                        previewBitmap = FilterUtils.fade(currentBitmap, progress / 100f);
                        break;
                    case BLUR:
                        previewBitmap = FilterUtils.blur(currentBitmap, progress);
                        break;
                    case GRAIN:
                        previewBitmap = FilterUtils.grain(currentBitmap, progress);
                        break;
                }
                imageView.setImageBitmap(previewBitmap);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                currentBitmap = previewBitmap;
            }
        });

        btnBrightness.setOnClickListener(v -> setMode(EditMode.BRIGHTNESS));
        btnContrast.setOnClickListener(v -> setMode(EditMode.CONTRAST));
        btnColor.setOnClickListener(v -> setMode(EditMode.SATURATION));
        btnSharp.setOnClickListener(v -> setMode(EditMode.SHARPNESS));
        btnWarm.setOnClickListener(v -> setMode(EditMode.TEMPERATURE));
        btnBlur.setOnClickListener(v -> setMode(EditMode.BLUR));
        btnGrain.setOnClickListener(v -> setMode(EditMode.GRAIN));

        btnSave.setOnClickListener(v -> {
            MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    currentBitmap,
                    "Edited_" + System.currentTimeMillis(),
                    "Offline Editor"
            );
            Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void setMode(EditMode mode) {
        currentMode = mode;
        seek.setProgress(100);
    }
}

package com.vstudio.filtercamera;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
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
    Bitmap originalBitmap, editedBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_image);

        imageView = findViewById(R.id.imageView);
        SeekBar seek = findViewById(R.id.seekBrightness);
        LinearLayout btnVintage = findViewById(R.id.btnVintage);
        LinearLayout btnBW = findViewById(R.id.btnBW);
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
            editedBitmap = originalBitmap;
            imageView.setImageBitmap(editedBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                float value = p - 100;
                editedBitmap = FilterUtils.brightness(originalBitmap, value);
                imageView.setImageBitmap(editedBitmap);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        btnVintage.setOnClickListener(v -> {
            editedBitmap = FilterUtils.vintage(originalBitmap);
            imageView.setImageBitmap(editedBitmap);
        });

        btnBW.setOnClickListener(v -> {
            editedBitmap = FilterUtils.blackWhite(originalBitmap);
            imageView.setImageBitmap(editedBitmap);
        });

        btnSave.setOnClickListener(v -> {
            MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    editedBitmap,
                    "Edited_" + System.currentTimeMillis(),
                    "Offline Editor");
            Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
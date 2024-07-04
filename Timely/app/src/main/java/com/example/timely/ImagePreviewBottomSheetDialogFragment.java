package com.example.timely;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ImagePreviewBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private static final String ARG_PHOTO_PATH = "photo_path";
    private static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 123;

    private String photoPath;

    public ImagePreviewBottomSheetDialogFragment() {
        // Required empty public constructor
    }

    public static ImagePreviewBottomSheetDialogFragment newInstance(String photoPath) {
        ImagePreviewBottomSheetDialogFragment fragment = new ImagePreviewBottomSheetDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PHOTO_PATH, photoPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            photoPath = getArguments().getString(ARG_PHOTO_PATH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_preview_bottom_sheet, container, false);

        // Check if permission is granted before loading the image
        if (hasReadExternalStoragePermission()) {
            loadImage(view);
        } else {
            requestReadExternalStoragePermission();
        }

        return view;
    }

    private boolean hasReadExternalStoragePermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, load the image
                loadImage(getView());
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImage(View view) {
        ImageView imageView = view.findViewById(R.id.imageView);
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(this)
                    .load(photoPath)
                    .into(imageView);
        }
    }
}

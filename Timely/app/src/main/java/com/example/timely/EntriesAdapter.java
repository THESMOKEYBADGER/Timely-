package com.example.timely;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    private List<TimeRecording> timeRecordings;

    public EntriesAdapter(List<TimeRecording> timeRecordings) {
        this.timeRecordings = timeRecordings;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTextView;
        TextView recordedTimeTextView;
        ImageButton imageButton; // Add ImageButton

        public ViewHolder(View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.text_view_category);
            recordedTimeTextView = itemView.findViewById(R.id.text_view_recorded_time);
            imageButton = itemView.findViewById(R.id.imagePreviewImageView); // Initialize ImageButton
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeRecording timeRecording = timeRecordings.get(position);

        holder.categoryTextView.setText(timeRecording.getCategoryName());
        holder.recordedTimeTextView.setText("Recorded Time: " + formatTime(timeRecording.getRecordedTime()));

        // Check if photoPath is valid
        if (timeRecording.getPhotoPath() != null && !timeRecording.getPhotoPath().isEmpty()) {
            // Load image using Glide or any other image loading library
            Glide.with(holder.itemView.getContext())
                    .load(timeRecording.getPhotoPath())
                    .into(holder.imageButton);

            // Set OnClickListener for the ImageButton
            holder.imageButton.setOnClickListener(v -> {
                // Open bottom sheet dialog fragment with image preview
                showImagePreviewDialog(holder.itemView.getContext(), timeRecording.getPhotoPath());
            });

            // Make sure the ImageButton is visible
            holder.imageButton.setVisibility(View.VISIBLE);
        } else {

            holder.imageButton.setVisibility(View.GONE);
        }
    }

    private void showImagePreviewDialog(Context context, String photoPath) {
        // Create instance of the bottom sheet dialog fragment
        ImagePreviewBottomSheetDialogFragment dialogFragment = ImagePreviewBottomSheetDialogFragment.newInstance(photoPath);
        dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), dialogFragment.getTag());
    }

    private String formatTime(int recordedTimeInSeconds) {
        int hours = recordedTimeInSeconds / 3600;
        int minutes = (recordedTimeInSeconds % 3600) / 60;
        int seconds = recordedTimeInSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public int getItemCount() {
        return timeRecordings.size();
    }
}

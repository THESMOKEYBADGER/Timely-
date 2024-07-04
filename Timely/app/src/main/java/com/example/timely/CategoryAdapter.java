package com.example.timely;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;

    // Constructor to initialize the list of categories
    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    // Method to remove a category from the adapter's list
    public void removeCategory(int position) {
        categories.remove(position);
        notifyItemRemoved(position);
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView goalTimeTextView;
        ProgressBar progressBarGoal;
        ImageButton deleteButton; // Add delete button

        public ViewHolder(View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.text_view_category_name);
            goalTimeTextView = itemView.findViewById(R.id.text_view_goal_time);
            progressBarGoal = itemView.findViewById(R.id.progress_bar_goal);
            deleteButton = itemView.findViewById(R.id.imageButton); // Initialize delete button
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the category item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to views
        Category category = categories.get(position);
        holder.categoryNameTextView.setText(category.getCategoryName());
        holder.goalTimeTextView.setText("Goal Time: " + category.getGoalTime() + " minutes");
        // Set other category details as needed
        holder.progressBarGoal.setProgress(category.getPercentage());

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call deleteCategoryFromFirestore() method with context from itemView
                category.deleteCategoryFromFirestore(holder.itemView.getContext(), CategoryAdapter.this, position);
            }
        });
    }




    @Override
    public int getItemCount() {
        return categories.size();
    }
}

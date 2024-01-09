package com.example.eduhub.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.CategoryClickListener;
import com.example.eduhub.R;
import com.example.eduhub.model.Category;

import java.util.List;

public class user_AdapterHomeFragmentCategory extends RecyclerView.Adapter<user_AdapterHomeFragmentCategory.ViewHolder>{
    private Context context;
    private static final String TAG = "CategoryAdapter";
    private List<Category> categoryList;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private CategoryClickListener categoryClickListener;

    public user_AdapterHomeFragmentCategory(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public void setCategoryClickListener(CategoryClickListener categoryClickListener) {
        this.categoryClickListener = categoryClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_home_categories, parent ,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.bind(category,position);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryTextView;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryBtn);

            categoryTextView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int adapterPosition = getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        // Update the selected position
                        selectedPosition = adapterPosition;
                        notifyDataSetChanged(); // Notify the adapter that the data set has changed

                        // Invoke the listener with the selected category
                        if (categoryClickListener != null){
                            String category_id = categoryList.get(adapterPosition).getCategory_id();
                            String category_name = categoryList.get(adapterPosition).getCategory_name();
                            categoryClickListener.onCategoryClick(category_id);

                            // Use the context from the categoryTextView to display the toast
                            Log.d(TAG, category_name);
                            Toast.makeText(categoryTextView.getContext(), category_name, Toast.LENGTH_SHORT).show();
                        }
                    }
                    return false; // indicate that the touch event is not consumed
                }
            });
        }

        public void bind(Category category, int position) {
            categoryTextView.setText(category.getCategory_name());

            // Reset the text color and background color for all categories
            categoryTextView.setTextColor(Color.parseColor("#686BFF"));
            categoryTextView.setBackgroundColor(Color.TRANSPARENT);

            // Update the UI based on the selected position
            if (position == selectedPosition) {
                // Change the text color and background color for the selected category
                categoryTextView.setTextColor(Color.WHITE);
                categoryTextView.setBackgroundColor(Color.parseColor("#686BFF"));
            }
        }
    }
}
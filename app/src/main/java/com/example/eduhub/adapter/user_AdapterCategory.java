package com.example.eduhub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.databinding.RowCategoriesBinding;
import com.example.eduhub.model.Category;
import com.example.eduhub.user_edit_notes;
import com.example.eduhub.user_filterCategory;
import com.example.eduhub.user_uploadNotes;

import java.util.ArrayList;

public class user_AdapterCategory extends RecyclerView.Adapter<user_AdapterCategory.HolderCategory> implements Filterable {
    public Context context;
    public ArrayList<Category> categoryArrayList,filterList;
    private OnItemClickListener listener;

    //view binding
    private RowCategoriesBinding binding;

    //instance of our filter class
    private user_filterCategory filter;
    //declare a variable to store the selected position
    private int selectedCategoryPosition = RecyclerView.NO_POSITION;
    private String actionType, noteId;


    public user_AdapterCategory (Context context, ArrayList<Category> categoryArrayList){
        this.context = context;
        this.categoryArrayList = categoryArrayList;
        this.filterList = categoryArrayList;
        this.actionType = "upload";
        this.noteId = "";
        //Register your class with EventBus
        //EventBus.getDefault().register(this);
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public interface OnItemClickListener{
        void onItemClick(Category category);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public HolderCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //bind row_category.xml
        binding = RowCategoriesBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull user_AdapterCategory.HolderCategory holder, int position) {
        //get data
        Category model = categoryArrayList.get(position);
//        String category = model.getCategory();
//        String uid = model.getUid();
//        long timestamp = model.getTimestamp();
        String category_id = model.getCategory_id();
        String category_name = model.getCategory_name();

        //set data
        holder.categoryTv.setText(category_name);

        //handle choose Category
        holder.categoryTv.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Maximum time between clicks for a double-click
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();

                // Check if the category is already selected
                if (selectedCategoryPosition == holder.getAdapterPosition()) {
                    // Check for a double-click
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        // Treat it as a double-click, unselect the item
                        selectedCategoryPosition = RecyclerView.NO_POSITION;
                        holder.categoryTv.setBackgroundColor(0xFFE1E1E1); // Set the default color
                        holder.categoryTv.setTextColor(0xFF000000); // Set the default text color

                        // Pass the category id and category name to another class
                        String categoryId = categoryArrayList.get(holder.getAdapterPosition()).getCategory_id();
                        String categoryName = categoryArrayList.get(holder.getAdapterPosition()).getCategory_name();
                        passCategoryNameToOtherClass(categoryName, categoryId, actionType);
                    }
                } else {
                    // A new category is selected, update the selection
                    int previousSelected = selectedCategoryPosition;
                    selectedCategoryPosition = holder.getAdapterPosition();

                    // Reset the previously selected item
                    if (previousSelected != RecyclerView.NO_POSITION) {
                        // Reset the color of the previously selected item
                        notifyItemChanged(previousSelected);
                    }

                    // Update the UI for the newly selected item
                    holder.categoryTv.setBackgroundColor(0xFF686BFF); // Set the selected color
                    holder.categoryTv.setTextColor(0xFFFFFFFF); // Set the selected text color

                    // Pass the category id and category name to another class
                    String categoryId = categoryArrayList.get(holder.getAdapterPosition()).getCategory_id();
                    String categoryName = categoryArrayList.get(holder.getAdapterPosition()).getCategory_name();
                    passCategoryNameToOtherClass(categoryName, categoryId, actionType);
                    Toast.makeText(v.getContext(),"Notes category chosen",Toast.LENGTH_SHORT).show();
                }
                lastClickTime = clickTime;
            }
        });

        //handle click, delete category
//        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               //confirm delete dialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                builder.setTitle("Delete")
//                        .setMessage("Are you sure you want to delete this category?")
//                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //begin delete
//                                deleteCategory(model, holder);
//                            }
//                        })
//                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .show();
//            }
//        });
    }

    private void passCategoryNameToOtherClass(String category_name, String category_id, String action_type) {
        //You can use Intent to pass data to another class/activity
        Intent intent;
        if (action_type.equals("edit")){
            intent = new Intent(context, user_edit_notes.class);
        } else {
            intent = new Intent(context, user_uploadNotes.class);
        }
//        intent.putExtra("NOTE_TITLE",title);
//        intent.putExtra("NOTE_DESCRIPTION",description);
//        intent.putExtra("PDF_URL",pdfUri);
        intent.putExtra("CATEGORY_NAME", category_name);
        intent.putExtra("CATEGORY_ID", category_id);
        intent.putExtra("noteId", noteId);
        context.startActivity(intent);
    }

    //For Admin Usage: Delete Category
//    private void deleteCategory(user_ModelCategory model, HolderCategory holder) {
//        //get id of category to delete
//        String id = model.getId();
//        //Firebase DB > Categories > categoryId
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//        ref.child(id)
//                .removeValue()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        //deleted successfully
//                        Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    public Filter getFilter() {
        if (filter == null){
            filter = new user_filterCategory(filterList, this);
        }
        return filter;
    }

    /*View holder class to hold UI views for row_category.xml*/
    class HolderCategory extends RecyclerView.ViewHolder{
        //ui views of row_category.xml
        TextView categoryTv;
        //ImageButton deleteBtn;

        public HolderCategory(@NonNull View itemView){
            super(itemView);

            //init ui views
            categoryTv = binding.categoryTv;
            //deleteBtn = binding.deleteBtn;
        }
    }
}
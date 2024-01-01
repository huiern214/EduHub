package com.example.eduhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.eduhub.MainActivity;
import com.example.eduhub.user_AdapterCategory;
import com.example.eduhub.databinding.ActivityCategoryAddBinding;
import com.example.eduhub.user_ModelCategory;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class user_CategoryAdd extends AppCompatActivity {

    // View binding
    private ActivityCategoryAddBinding binding;
    private ProgressDialog progressDialog;

    // Firebase auth
    private FirebaseAuth firebaseAuth;

    // Arraylist to store category
    private ArrayList<user_ModelCategory> categoryArrayList;
    // Adapter
    private user_AdapterCategory adapterCategory;

    // Dialog add category
    Dialog addNewCategoryDialog;
    ImageButton closeBtn;
    Button addBtn;
    TextInputEditText categoryNameEt;
    EditText searchCategoryEt;
    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Check user authentication status
        checkUser();
        loadCategories();

        //edit text change listener, search
        searchCategoryEt = findViewById(R.id.searchCategoryEt);
        searchCategoryEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    // Assuming you have an instance of user_AdapterCategory named adapterCategory
                    adapterCategory.getFilter().filter(s);
                } catch (Exception e) {

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //pop up dialog handle
        progressDialog = new ProgressDialog(this);
        addNewCategoryDialog = new Dialog(this);
        //Inflate the dialog layout
        addNewCategoryDialog.setContentView(R.layout.dialog_add_category_details);
        //Find views from addNewCategoryDialog
        closeBtn = addNewCategoryDialog.findViewById(R.id.closeBtn);
        addBtn = addNewCategoryDialog.findViewById(R.id.addBtn);
        addNewCategoryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //handle click, start category add screen
        binding.addCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the dialog layout
                addNewCategoryDialog.setContentView(R.layout.dialog_add_category_details);
                addNewCategoryDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Find views from addNewCategoryDialog
                closeBtn = addNewCategoryDialog.findViewById(R.id.closeBtn);
                addBtn = addNewCategoryDialog.findViewById(R.id.addBtn);

                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addNewCategoryDialog.dismiss(); // Dismiss the dialog
                    }
                });

                addBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Handle the add button click
                        validateData();
                    }
                });

                // Show the dialog
                addNewCategoryDialog.show();
            }
        });

        //Find the back button by its ID
        ImageButton backButton = findViewById(R.id.backBtn);
        //Set an OnClickListener to handle the back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonClicked();
            }
        });
    }

    private void loadCategories() {
        // Initialize ArrayList
        categoryArrayList = new ArrayList<>();

        // Get reference to the "Categories" node in Firebase
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear ArrayList before adding data into it
                categoryArrayList.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Get data
                    user_ModelCategory model = ds.getValue(user_ModelCategory.class);
                    // Add to ArrayList
                    categoryArrayList.add(model);
                }

                // Setup adapter
                adapterCategory = new user_AdapterCategory(user_CategoryAdd.this, categoryArrayList);
                // Set adapter to recyclerView
                binding.categoriesRv.setAdapter(adapterCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled
            }
        });
    }

    private void validateData() {
        // Initialize views
        categoryNameEt = addNewCategoryDialog.findViewById(R.id.categoryNameEt);
        category = categoryNameEt.getText().toString();
        // If validation not empty
        if (TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please enter category", Toast.LENGTH_SHORT).show();
        } else {
            addCategoryFirebase();
        }
    }

    private void addCategoryFirebase() {
        // Show progress
        progressDialog.setMessage("Adding category...");
        progressDialog.show();

        // Get timestamp
        long timestamp = System.currentTimeMillis();

        // Setup info to add in Firebase DB
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("id", "" + timestamp);
        hashMap.put("category", "" + category);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", "" + firebaseAuth.getUid());

        // Add to Firebase DB... Database Root > Categories > categoryId > category info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Categories");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        // Category added successfully
                        progressDialog.dismiss();
                        Toast.makeText(user_CategoryAdd.this, "Category added successfully...", Toast.LENGTH_SHORT).show();
                        // Dismiss the dialog or handle UI accordingly
                        addNewCategoryDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Category add failed
                        progressDialog.dismiss();
                        Toast.makeText(user_CategoryAdd.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onBackButtonClicked() {
        //Implement the desired action when the back button is clicked
        //startActivity(new Intent(CategoryAdd.this,HomeFragment.class));
        onBackPressed();
        finish();
    }

    private void checkUser() {
        // Get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // User not logged in, go to the main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
            // Add a debug statement
            Log.d("CategoryAddActivity", "User not logged in. Redirecting to MainActivity.");
        } else {
            // User logged in, get user info
            String email = firebaseUser.getEmail();
            // You can uncomment the following line if you want to set the email in your view
            // binding.subTitleTv.setText(email);
            // Add a debug statement
            Log.d("CategoryAddActivity", "User logged in. Email: " + email);
        }
    }
}
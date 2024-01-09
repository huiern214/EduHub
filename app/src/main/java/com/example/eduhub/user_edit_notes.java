package com.example.eduhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.eduhub.databinding.ActivityUserEditNotesBinding;
import com.example.eduhub.databinding.ActivityUserUploadNotesBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class user_edit_notes extends AppCompatActivity {

    AppCompatButton chooseCategoryBtn;
    String noteId, noteTitle, categoryName, categoryId;
    private String title = "", description = "", category = "", id="";
    //Passed Data
    String tempTitle, tempDescription, tempPdf;

    private static final String KEY_TITLE = "key_title";
    private static final String KEY_DESCRIPTION = "key_description";
    private static final String KEY_CATEGORY_NAME = "key_category_name";
    private static final String KEY_CATEGORY_ID = "key_category_id";
    private static final String KEY_PDF_URI = "key_pdf_uri";

    //set view binding
    private ActivityUserEditNotesBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //uri of picked pdf
    private Uri pdfUri = null;
    private static final int PDF_PICK_CODE = 1000;

    //TAG for debugging
    private static final String TAG = "ADD_PDF_TAG";

    //ViewModel instance
    private user_UploadNotesViewModel userUploadNotesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserEditNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //Initialize ViewModel
        userUploadNotesViewModel = new ViewModelProvider(this).get(user_UploadNotesViewModel.class);

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //handle click, pick category
        chooseCategoryBtn = findViewById(R.id.chooseCategoryBtn);
        chooseCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // All data is valid, create an Intent
                Intent intent = new Intent(user_edit_notes.this, user_CategoryAdd.class);

                // Put the data as extras in the Intent
                String title = binding.notesNameEt.getText().toString();
                intent.putExtra("TITLE", title);
                String description = binding.descriptionEt.getText().toString();
                intent.putExtra("DESCRIPTION", description);
                intent.putExtra("ACTION_TYPE", "edit");
                intent.putExtra("noteId", noteId);
                // Start the next activity
                startActivity(intent);
            }
        });

        //Retrieve the noteID from the intent
        noteId = getIntent().getStringExtra("noteId");
        categoryId = getIntent().getStringExtra("CATEGORY_ID");

        loadNoteDetails(noteId, categoryId);

        binding.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data;
                validateData();
                Intent intent = new Intent(user_edit_notes.this, user_notesDetails.class);
                intent.putExtra("noteId", noteId);
                startActivity(intent);
            }
        });

    }

    private void validateData() {
        //Step 1: Validate data
        Log.d(TAG,"validateData: validating data...");

        //get data
        title = binding.notesNameEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        category = categoryName;
        id = categoryId;

        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this,"Enter description",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(category)){
            Toast.makeText(this,"Pick category",Toast.LENGTH_SHORT).show();
        } else{
            // All data is valid, store in ViewModel
            userUploadNotesViewModel.getTitleLiveData().setValue(title);
            userUploadNotesViewModel.getDescriptionLiveData().setValue(description);
            userUploadNotesViewModel.getCategoryLiveData().setValue(category);
            userUploadNotesViewModel.getCategoryIdLiveData().setValue(id);

            //all data is valid, can upload now
            updateNoteInfoToDb();
        }
    }

    private void updateNoteInfoToDb() {
        // Update notes info in Firestore
        Log.d(TAG, "updateNoteInfoToDb: Updating note info in Firestore...");
        progressDialog.setMessage("Updating note info");

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a DocumentReference for the existing note document you want to update
        DocumentReference notesRef = db.collection("resource").document(noteId);

        // Create a DocumentReference for the category
        DocumentReference categoryRef = db.collection("category").document(categoryId); // Replace categoryId with the actual category ID

        // Setup data to update (change the fields you want to update)
        Map<String, Object> noteUpdates = new HashMap<>();
        noteUpdates.put("resource_description", description);
        noteUpdates.put("resource_name", title);
        noteUpdates.put("category_id", categoryRef); // Include the updated category reference

        // Update the document in Firestore
        notesRef.update(noteUpdates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document was successfully updated
                        Log.d(TAG, "onSuccess: Successfully updated.");
                        progressDialog.dismiss();
                        Toast.makeText(user_edit_notes.this, "Successfully updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "onFailure: Failed to update Firestore due to " + e.getMessage());
                        Toast.makeText(user_edit_notes.this, "Failed to update Firestore due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadNoteDetails(String noteId, String categoryId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference noteRef = db.collection("resource").document(noteId);

        noteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Note exists, retrieve its data
                title = documentSnapshot.getString("resource_name");
                description = documentSnapshot.getString("resource_description");

                // Set the title after data is loaded
                binding.notesNameEt.setText(title);
                binding.descriptionEt.setText(description);
            } else {
                // Note with the given ID does not exist
                Log.d("Note details", "Note with ID " + noteId + " does not exist");
            }
        }).addOnFailureListener(e -> {
            // Handle errors if any
            Log.e("Note details", "Error loading note details: " + e.getMessage());
        });

        DocumentReference categoryRef = db.collection("category").document(categoryId);

        categoryRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Note exists, retrieve its data
                categoryName = documentSnapshot.getString("category_name");

                // Set the title after data is loaded
                binding.chooseCategoryBtn.setText(categoryName);
                binding.chooseCategoryBtn.setTextColor(0xFF000000);
            } else {
                // Note with the given ID does not exist
                Log.d("Category details", "Category with ID " + categoryId + " does not exist");
            }
        }).addOnFailureListener(e -> {
            // Handle errors if any
            Log.e("Category details", "Error loading category details: " + e.getMessage());
        });
    }
}
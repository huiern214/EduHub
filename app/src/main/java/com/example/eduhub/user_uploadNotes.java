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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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

public class user_uploadNotes extends AppCompatActivity {

    ImageButton uploadDocumentBtn;
    AppCompatButton chooseCategoryBtn;
    String categoryName,categortId;
    TextView pdfName;
    EditText titleNote, descriptionNote;
    private String title = "", description = "", category = "", id="";
    int likes=0, views=0, download=0;
    //Passed Data
    String tempTitle, tempDescription, tempPdf;

    private static final String KEY_TITLE = "key_title";
    private static final String KEY_DESCRIPTION = "key_description";
    private static final String KEY_CATEGORY_NAME = "key_category_name";
    private static final String KEY_CATEGORY_ID = "key_category_id";
    private static final String KEY_PDF_URI = "key_pdf_uri";

    //set view binding
    private ActivityUserUploadNotesBinding binding;

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
        binding = ActivityUserUploadNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //Initialize ViewModel
        userUploadNotesViewModel = new ViewModelProvider(this).get(user_UploadNotesViewModel.class);

        //Retrieve stored data and update UI
        observeViewModel();

        // Retrieve the data from SharedPreferences
        restoreDataFromSharedPreferences();

        //handle click, go to previous activity
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(user_uploadNotes.this,user_DashboardActivity.class));
            }
        });

        //handle click, attach pdf
        uploadDocumentBtn = findViewById(R.id.uploadDocument);
        uploadDocumentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pdfPickIntent();
            }
        });

        //handle click, pick category
        chooseCategoryBtn = findViewById(R.id.chooseCategoryBtn);
        chooseCategoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // All data is valid, create an Intent
                Intent intent = new Intent(user_uploadNotes.this, user_CategoryAdd.class);

                // Put the data as extras in the Intent
                String title = binding.notesNameEt.getText().toString();
                intent.putExtra("TITLE", title);
                String description = binding.descriptionEt.getText().toString();
                intent.putExtra("DESCRIPTION", description);
                intent.putExtra("PDF",pdfUri);

                // Start the next activity
                startActivity(intent);
            }
        });

        //Retrieve the data from Intent
        Intent intent = getIntent();
        if(intent != null){
            categoryName = intent.getStringExtra("CATEGORY_NAME");
            categortId = intent.getStringExtra("CATEGORY_ID");
            tempTitle = intent.getStringExtra("NOTE_TITLE");
            tempDescription = intent.getStringExtra("NOTE_DESCRIPTION");
            tempPdf = intent.getStringExtra("PDF_URL");
            //Now we can use the categoryName as needed
            if (categoryName != null){
                //Do something with the categoryName
                chooseCategoryBtn.setText(categoryName);
                chooseCategoryBtn.setTextColor(0xFF000000);
                binding.notesNameEt.setText(tempTitle);
                binding.descriptionEt.setText(tempDescription);
            }
        }

        // Restore saved data if available
        if (savedInstanceState != null) {
            title = savedInstanceState.getString(KEY_TITLE, "");
            description = savedInstanceState.getString(KEY_DESCRIPTION, "");
            categoryName = savedInstanceState.getString(KEY_CATEGORY_NAME, "");
            categortId = savedInstanceState.getString(KEY_CATEGORY_ID, "");
            pdfUri = savedInstanceState.getParcelable(KEY_PDF_URI);

            // Update UI with restored data
            binding.notesNameEt.setText(title);
            binding.descriptionEt.setText(description);
            // Update other UI elements as needed
        }

        //handle click, upload pdf
        binding.uploadNotesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //validate data;
                validateData();
            }
        });

    }

    private void restoreDataFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        title = sharedPreferences.getString(KEY_TITLE, "");
        description = sharedPreferences.getString(KEY_DESCRIPTION, "");
        category = sharedPreferences.getString(KEY_CATEGORY_NAME, "");
        id = sharedPreferences.getString(KEY_CATEGORY_ID, "");

        // Update UI with the restored data
        binding.notesNameEt.setText(title);
        binding.descriptionEt.setText(description);
    }

    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save data to be restored later
        outState.putString(KEY_TITLE, title);
        outState.putString(KEY_DESCRIPTION,description);
        outState.putString(KEY_DESCRIPTION,category);
        outState.putString(KEY_CATEGORY_ID,categortId);
        outState.putParcelable(KEY_PDF_URI,pdfUri);
    }

    private void observeViewModel() {
        //Observe change in LiveData and update UI accordingly
        //Observe title changes
        userUploadNotesViewModel.getTitleLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                //Update UI with the title if needed
                binding.notesNameEt.setText(s);
            }
        });

        // Observe description changes
        userUploadNotesViewModel.getDescriptionLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String description) {
                // Update UI with the description if needed
                binding.descriptionEt.setText(description);
            }
        });

        // Observe category changes
        userUploadNotesViewModel.getCategoryLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String category) {
                // Update UI with the category if needed
                //binding.chooseCategoryBtn.setText(category);
            }
        });

        // Observe categoryId changes
        userUploadNotesViewModel.getCategoryIdLiveData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String categoryId) {
                // Update UI with the categoryId if needed
            }
        });

        // Observe pdfUri changes
        userUploadNotesViewModel.getPdfUriLiveData().observe(this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri pdfUri) {
                // Update UI with the pdfUri if needed
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
        id = categortId;

        //validate data
        if (TextUtils.isEmpty(title)){
            Toast.makeText(this, "Enter title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this,"Enter description",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(category)){
            Toast.makeText(this,"Pick category",Toast.LENGTH_SHORT).show();
        } else if (pdfUri == null){
            Toast.makeText(this, "Pick pdf", Toast.LENGTH_SHORT).show();
        } else{
            // All data is valid, store in ViewModel
            userUploadNotesViewModel.getTitleLiveData().setValue(title);
            userUploadNotesViewModel.getDescriptionLiveData().setValue(description);
            userUploadNotesViewModel.getCategoryLiveData().setValue(category);
            userUploadNotesViewModel.getCategoryIdLiveData().setValue(id);

            //all data is valid, can upload now
            uploadNotesToStorage();
        }
    }

    private void uploadNotesToStorage() {
        //Step 2: Upload Notes to firebase storage
        Log.d(TAG,"uploadNotesToStorage: uploading to storage...");

        //show progress
        progressDialog.setMessage("Upload notes");
        progressDialog.show();

        //timestamp
        long timestamp = System.currentTimeMillis();
        
        //path of notes in firebase storage
        String filePathAndName = "notes/"+timestamp;
        //storage reference
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: notes uploaded to storage...");
                        Log.d(TAG, "onSuccess: getting note url");

                        //get note url 
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadNoteUrl = "" +uriTask.getResult();

                        Timestamp firebaseTimestamp = new Timestamp(timestamp / 1000, (int) ((timestamp % 1000) * 1000000));
                        
                        //upload to firebase db
                        uploadNoteInfoToDb(uploadNoteUrl, firebaseTimestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Note upload failed due to "+e.getMessage());
                        Toast.makeText(user_uploadNotes.this, "Note upload failed due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getPDFDisplayName(Uri pdfUri) {
        String displayName = null;

        // Try to query the file name from the content resolver
        Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                // Check if the DISPLAY_NAME column is available
                int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (displayNameIndex != -1) {
                    displayName = cursor.getString(displayNameIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // If the display name is still null, extract it from the Uri
        if (displayName == null) {
            displayName = pdfUri.getLastPathSegment();
        }

        return displayName;
    }

    private void uploadNoteInfoToDb(String uploadNoteUrl, Timestamp timestamp) {
        // Step 3: Upload notes info to Firestore
        Log.d(TAG, "uploadNoteToStorage: Uploading note info to Firestore...");
        progressDialog.setMessage("Uploading note info");
    
        String uid = firebaseAuth.getUid();
    
        // Create DocumentReference for category and user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference categoryRef = db.collection("category").document(id);
        DocumentReference userRef = db.collection("user").document(uid);
    
        // Setup data to upload
        Map<String, Object> noteData = new HashMap<>();
        noteData.put("category_id", categoryRef); // Use DocumentReference
        noteData.put("resource_description", description);
        noteData.put("resource_downloads", download);
        noteData.put("resource_file", uploadNoteUrl);
        noteData.put("resource_likes", likes);
        noteData.put("resource_name", title);
        noteData.put("resource_type", "notes");
        noteData.put("resource_upload_datetime", timestamp);
        noteData.put("resource_views", views);
        noteData.put("user_id", userRef); // Use DocumentReference
    
        CollectionReference notesRef = db.collection("resource");

        // Add the note data to Firestore
        notesRef.add(noteData)
                .addOnSuccessListener(documentReference -> {
                    // Document was successfully added, and documentReference now contains the document ID
                    String noteId = documentReference.getId();
                    Log.d(TAG, "onSuccess: Successfully uploaded. Document ID: " + noteId);

                    progressDialog.dismiss();
                    Toast.makeText(user_uploadNotes.this, "Successfully uploaded", Toast.LENGTH_SHORT).show();

                    // Pass the document ID to the next activity
                    Intent intent1 = new Intent(user_uploadNotes.this, user_notesDetails.class);
                    intent1.putExtra("noteId", noteId); // Pass the document ID
                    startActivity(intent1);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "onFailure: Failed to upload to Firestore due to " + e.getMessage());
                    Toast.makeText(user_uploadNotes.this, "Failed to upload to Firestore due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting pdf pick intent");
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PDF_PICK_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "onActivityResult: PDF Picked");
            pdfUri = data.getData();
            Log.d(TAG, "onActivityResult: URI " + pdfUri);

            // Retrieve the name of the PDF file
            String pdfName = getPDFDisplayName(pdfUri);

            // Set the name to the TextView
            TextView pdfNameTextView = findViewById(R.id.pdfName);
            pdfNameTextView.setText(pdfName);
        } else {
            Log.d(TAG, "onActivityResult: Cancelled picking pdf");
            Toast.makeText(this, "Cancelled picking pdf", Toast.LENGTH_SHORT).show();
        }
    }
}
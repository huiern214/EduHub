package com.example.eduhub;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.adapter.admin_AdapterReport;
import com.example.eduhub.databinding.ActivityAdminReportDetailsBinding;
import com.example.eduhub.model.Report;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class admin_report_details extends AppCompatActivity {

    private static final String TAG = "ADMIN_REPORT_DETAILS";
    private ActivityAdminReportDetailsBinding binding;
    private FirebaseAuth firebaseAuth;
    String noteId;
    TextView noteTitleTv, authorTv, categoryTv, totalReportCases, pendingReportCases, isDeletedTv;
    PDFView pdfView;
    CardView noteCard;
    RecyclerView reportRv;
    private admin_AdapterReport reportAdapter;
    private ArrayList<Report> reportList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReportDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();

        noteTitleTv = binding.titleTv;
        authorTv = binding.authorNameTv;
        categoryTv = binding.categoryNameTv;
        totalReportCases = binding.totalReportCases;
        pendingReportCases = binding.pendingReportCases;
        isDeletedTv = binding.isDeletedTv;
        pdfView = binding.pdfView;
        noteCard = binding.noteCard;

        //Retrieve the noteID from the intent
        noteId = getIntent().getStringExtra("noteId");
        loadNoteDetails(noteId);

        //Load reports from database
        reportRv = findViewById(R.id.report_details_rv);
        reportRv.setLayoutManager(new LinearLayoutManager(this));
        reportList = new ArrayList<>();
        reportAdapter = new admin_AdapterReport(this, reportList);
        reportRv.setAdapter(reportAdapter);
        loadReportsForSpecificNotes(noteId);

        noteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass the note ID to other activity
                Intent intent = new Intent(admin_report_details.this, user_notesDetails.class);
                intent.putExtra("noteId", noteId);
                startActivity(intent);
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.deleteNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.deleteNote(getApplicationContext(), noteId);
                MyApplication.removeReferenceFromUserLikes(noteId);
                MyApplication.removeReferenceFromUserFavourite(noteId);
                isDeletedTv.setVisibility(View.VISIBLE);
                binding.deleteNoteBtn.setClickable(false);
                setReportSolvedStatusForSpecificNotes();
                finish();
            }
        });

        binding.ignoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReportSolvedStatusForSpecificNotes();
                finish();
            }
        });
    }

    private void loadNoteDetails(String resourceId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference resourceRef = db.collection("resource");

        resourceRef.document(resourceId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //Get authorName, category, title
                            String noteTitle = document.getString("resource_name");
                            String noteCategoryId = Objects.requireNonNull(document.getDocumentReference("category_id")).getId();
                            String authorUserId = Objects.requireNonNull(document.getDocumentReference("user_id")).getId();
                            String pdfUrl = document.getString("resource_file");
                            Boolean is_deleted = document.getBoolean("is_deleted");

                            if (Boolean.TRUE.equals(is_deleted)) {
                                isDeletedTv.setVisibility(View.VISIBLE);
                                binding.deleteNoteBtn.setClickable(false);
                            }

                            noteTitleTv.setText(noteTitle);
                            loadNoteCategory(noteCategoryId);
                            loadAuthor(authorUserId);
                            loadPdfUrl(pdfUrl);

                        } else {
                            Log.d(TAG, "No such resource document");
                        }
                    } else {
                        Log.w(TAG, "Error getting resource document", task.getException());
                    }
                });
    }

    private void loadAuthor(String authorUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference userRef = db.collection("user");

        userRef.document(authorUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            //Get author name
                            String authorName = document.getString("user_name"); //The field is named "user_name"
                            //Set to author text view
                            authorTv.setText(authorName);
                        } else{
                            //Handle the case where the user document does not exist
                            Log.d(TAG, "No such user document ");
                        }
                    }else{
                        //Handle errors here
                        Log.w(TAG, "Error getting user document",task.getException());
                    }
                });
    }

    private void loadNoteCategory(String noteCategoryId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoryRef = db.collection("category");

        categoryRef.document(noteCategoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()){
                            //Get category name
                            String categoryName = document.getString("category_name");
                            //Set to category text view
                            categoryTv.setText(categoryName);
                        } else {
                            //Handle the case where the category document does not exist
                            Log.d(TAG, "No such category document");
                        }
                    }else{
                        Log.w(TAG, "Error getting category document", task.getException());
                    }
                });
    }

    private void loadPdfUrl(String pdfUrl) {
        // Using URL we can get file and its metadata from Firebase Storage
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: successfully got the file");
                        // Set to pdfView
                        pdfView.fromBytes(bytes)
                                .pages(0) // Show only the first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.e(TAG, "loadPdfUrl:onError", t);
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.e(TAG, "loadPdfUrl:onPageError", t);
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "loadPdfUrl:onFailure", e);
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadReportsForSpecificNotes(String noteId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reportRef = db.collection("report");

        reportRef.whereEqualTo("resource_id", db.collection("resource").document(noteId))
                .orderBy("report_timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(admin_report_details.this, "reports: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    // Clear the ArrayList before adding data into it
                    reportList.clear();

                    if (value != null) {
                        int pendingCount = 0;
                        for (DocumentSnapshot document : value.getDocuments()) {
                            // Log the data to check if it's correct
                            Log.d("ReportData", document.getData().toString());

                            if (Boolean.FALSE.equals(document.getBoolean("is_solved"))){
                                pendingCount++;
                            }

                            Report report = new Report(document.getId(),
                                    document.getString("report_details"),
                                    document.getTimestamp("report_timestamp"),
                                    noteId,
                                    Objects.requireNonNull(document.getDocumentReference("user_id")).getId(),
                                    document.getBoolean("is_solved"));
                            reportList.add(report);
                        }
                        pendingReportCases.setText(String.valueOf(pendingCount));
                        totalReportCases.setText(String.valueOf(reportList.size()));
                        if(pendingCount == 0){
                            binding.ignoreBtn.setClickable(false);
                        } else {
                            binding.ignoreBtn.setClickable(true);
                        }
                        reportAdapter.setReportArrayList(reportList);
                        reportAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setReportSolvedStatusForSpecificNotes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reportRef = db.collection("report");

        reportRef.whereEqualTo("resource_id", db.collection("resource").document(noteId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot reportSnapshot : queryDocumentSnapshots) {
                        // Update the is_solved field to true
                        reportSnapshot.getReference().update("is_solved", true);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors gracefully
                    Log.w("Firestore", "Error updating report status: ", e);
                    // Consider notifying the user or taking appropriate actions
                });
    }
}
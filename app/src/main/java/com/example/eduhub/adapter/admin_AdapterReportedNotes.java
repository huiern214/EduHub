package com.example.eduhub.adapter;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.admin_report_details;
import com.example.eduhub.databinding.RowReportsBinding;
import com.example.eduhub.model.ReportedNotes;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class admin_AdapterReportedNotes extends RecyclerView.Adapter<admin_AdapterReportedNotes.HolderReport> {
    public Context context;
    public ArrayList<ReportedNotes> reportNotesArrayList;
    private final String TAG= "REPORT_ADAPTER_TAG";

    public admin_AdapterReportedNotes(Context context, ArrayList<ReportedNotes> reportNotesArrayList) {
        this.context = context;
        this.reportNotesArrayList = reportNotesArrayList;
    }

    public void setReportNotesArrayList(ArrayList<ReportedNotes> reportNotesArrayList) {
        this.reportNotesArrayList = reportNotesArrayList;
    }

    @NonNull
    @Override
    public HolderReport onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowReportsBinding binding = RowReportsBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderReport(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull admin_AdapterReportedNotes.HolderReport holder, int position) {
        // get data
        ReportedNotes model = reportNotesArrayList.get(holder.getAdapterPosition());
        String resource_id = model.getNotes_id();

        //Load Note Details
        loadNoteDetails(resource_id, holder);

        holder.totalReportCases.setText(String.valueOf(model.getTotal_report_cases()));
        holder.pendingReportCases.setText(String.valueOf(model.getPending_report_cases()));

    }

    private void loadNoteDetails(String resourceId, HolderReport holder) {
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
                                holder.isDeletedTv.setVisibility(View.VISIBLE);
                            }

                            holder.noteTitleTv.setText(noteTitle);
                            loadNoteCategory(noteCategoryId, holder);
                            loadAuthor(authorUserId, holder);
                            loadPdfUrl(pdfUrl, holder);

                        } else {
                            Log.d(TAG, "No such resource document");
                        }
                    } else {
                        Log.w(TAG, "Error getting resource document", task.getException());
                    }
                });
    }

    private void loadAuthor(String authorUserId, HolderReport holder) {
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
                            holder.authorTv.setText(authorName);
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

    private void loadNoteCategory(String noteCategoryId, HolderReport holder) {
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
                            holder.categoryTv.setText(categoryName);
                        } else {
                            //Handle the case where the category document does not exist
                            Log.d(TAG, "No such category document");
                        }
                    }else{
                        Log.w(TAG, "Error getting category document", task.getException());
                    }
                });
    }

    private void loadPdfUrl(String pdfUrl, admin_AdapterReportedNotes.HolderReport holder) {
        // Using URL we can get file and its metadata from Firebase Storage
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: successfully got the file");
                        // Set to pdfView
                        holder.pdfView.fromBytes(bytes)
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

    @Override
    public int getItemCount() {
        return reportNotesArrayList.size();
    }

    public class HolderReport extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView noteTitleTv, authorTv, categoryTv, totalReportCases, pendingReportCases, isDeletedTv;
        RowReportsBinding binding;
        PDFView pdfView;

        public HolderReport(RowReportsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            noteTitleTv = binding.titleTv;
            authorTv = binding.authorNameTv;
            categoryTv = binding.categoryNameTv;
            totalReportCases = binding.totalReportCases;
            pendingReportCases = binding.pendingReportCases;
            isDeletedTv = binding.isDeletedTv;
            pdfView = binding.pdfView;

            // Set click listener on the entire item view
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Handle item click here
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                ReportedNotes clickedNote = reportNotesArrayList.get(position);

                // Pass the note ID to other activity
                Intent intent = new Intent(context, admin_report_details.class);
                intent.putExtra("noteId", clickedNote.getNotes_id());
                context.startActivity(intent);
            }
        }
    }
}
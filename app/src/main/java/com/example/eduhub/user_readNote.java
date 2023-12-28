package com.example.eduhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.eduhub.databinding.ActivityUserReadNoteBinding;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class user_readNote extends AppCompatActivity {

    // View binding
    private ActivityUserReadNoteBinding binding;
    private String noteId;
    private static final String TAG = "PDF_VIEW_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserReadNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get noteId from intent that we passed in intent
        Intent intent = getIntent();
        noteId = intent.getStringExtra("noteId");
        //Toast.makeText(user_readNote.this, "NoteId: " + noteId, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: NoteId: " + noteId);

        loadNoteDetails();

        // Handle click, go back
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadNoteDetails() {
        Log.d(TAG, "loadNoteDetails: Get Pdf URL...");
        // Database Reference to get note details e.g. get note url using noteId
        // Step (1) Get Note Url using noteId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes");
        ref.child(noteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get book url
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        Log.d(TAG, "onDataChange: PDF URL" + pdfUrl);
                        // Step (2) load Pdf using that url from firebase storage
                        loadNoteFromUrl(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadNoteFromUrl(String pdfUrl) {
        Log.d(TAG, "loadNoteFromUrl: Get PDF from storage");
        StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        reference.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Load pdf using bytes
                        binding.notePdfView.fromBytes(bytes)
                                .swipeHorizontal(false) // Set false to scroll vertical, set true to swipe horizontal
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        // Set current and total pages in toolbar subtitles
                                        int currentPage = (page + 1); // Do +1 because page starts from 0
                                        binding.toolbarSubtitleTv.setText(currentPage + "/" + pageCount); // e.g. 3/10
                                        Log.d(TAG, "onPageChanged " + currentPage + "/" + pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d(TAG, "onError: " + t.getMessage());
                                        Toast.makeText(user_readNote.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d(TAG, "onPageError: " + t.getMessage());
                                        Toast.makeText(user_readNote.this, "Error on page" + page + " " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .load();
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.getMessage());
                        // Failed to load note
                        binding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
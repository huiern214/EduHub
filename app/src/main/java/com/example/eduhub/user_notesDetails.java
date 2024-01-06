package com.example.eduhub;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.eduhub.adapter.user_AdapterComment;
import com.example.eduhub.databinding.ActivityUserNotesDetailsBinding;

import com.example.eduhub.model.Comment;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class user_notesDetails extends AppCompatActivity {
    private ActivityUserNotesDetailsBinding binding;
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private static final int SAF_REQUEST_CODE = 0;
    private String title, description, authorName, dateUploaded, categoryName, authorID, url, noteId, matchAuthorId;
    private Timestamp timestamp;
            TextView noteTitle, noteDescription, noteCategory, noteDate, author,sizeTv, numberOfViews, numberOfDownloads, numberOfLikes;
            PDFView noteImg;
            ImageButton backBtn, downloadBtn, addCommentBtn, shareBtn, reportBtn, editBtn;
            ToggleButton likeBtn, favouriteBtn;
            Button readBtn;
            boolean isInMyFavourite=false;
            boolean isInMyLike=false;
    private FirebaseAuth firebaseAuth;
    //private dialog
    private ProgressDialog progressDialog;
    //arraylist to hold comments
    private ArrayList<Comment> commentArrayList;
    //adapter to set recyclerview
    private user_AdapterComment adapterComment;
    RecyclerView commentRv;
    Dialog addCommentDialog, addReportDialog;
    TextView commentTv, reportNoteTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityUserNotesDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseAuth = FirebaseAuth.getInstance();

        //Retrieve the noteID from the intent
        noteId = getIntent().getStringExtra("noteId");
        loadNoteDetails(noteId);

        //Load comments from database
        commentRv = findViewById(R.id.commentsRV);
        commentRv.setLayoutManager(new LinearLayoutManager(this));
        commentArrayList = new ArrayList<>();
        adapterComment = new user_AdapterComment(this,commentArrayList);
        commentRv.setAdapter(adapterComment);
        loadComments(noteId);
        
        // Toast.makeText(this, "noteID: " + noteId, Toast.LENGTH_SHORT).show();

        //init progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            checkIsFavourite();
            checkIsLike();
        }

        noteTitle = findViewById(R.id.titleTv);
        noteDescription = findViewById(R.id.noteDescription);
        noteCategory = findViewById(R.id.categoryNameTv);
        noteDate = findViewById(R.id.date);
        author = findViewById(R.id.authorNameTv);
        sizeTv = findViewById(R.id.size);
        noteImg = findViewById(R.id.pdfView);
        numberOfDownloads = findViewById(R.id.numberOfDownloads);
        numberOfViews = findViewById(R.id.numberOfViews);
        backBtn = findViewById(R.id.backNotesBtn);
        readBtn = findViewById(R.id.readBtn);
        downloadBtn = findViewById(R.id.downloadBtn);
        likeBtn = findViewById(R.id.LikeBtn);
        favouriteBtn = findViewById(R.id.favouriteBtn);
        numberOfLikes = findViewById(R.id.numberOfLikesTv);
        addCommentBtn = findViewById(R.id.addCommentBtn);
        commentTv = findViewById(R.id.commentOfUserComment);
        shareBtn = findViewById(R.id.shareBtn);
        reportBtn = findViewById(R.id.reportNote);
        reportNoteTv = findViewById(R.id.reportNoteEt);

        //handle click, go back
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // startActivity(new Intent(user_notesDetails.this, user_DashboardActivity.class));
                finish();
            }
        });
        // backBtn.setOnClickListener(v -> startActivity(new Intent(user_notesDetails.this, user_HomeFragment.class)));

        //handle click, open to view notes
        readBtn.setOnClickListener(v -> {
            // Increment the number of views
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference noteRef = db.collection("resource").document(noteId);

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(noteRef);

                // Retrieve current views
                Integer currentViews = Objects.requireNonNull(snapshot.getLong("resource_views")).intValue();

                // Increment views by 1
                currentViews++;

                // Update the view count in the document
                transaction.update(noteRef, "resource_views", currentViews);

                return null;
            }).addOnSuccessListener(aVoid -> {
                // Views updated successfully
                Intent intent = new Intent(user_notesDetails.this, user_readNote.class);
                intent.putExtra("noteId", noteId);
                startActivity(intent);
            }).addOnFailureListener(e -> {
                // Views update failed
                Toast.makeText(user_notesDetails.this, "Failed to update views", Toast.LENGTH_SHORT).show();
            });
        });

        downloadBtn.setOnClickListener(v -> {
            // Increment the number of downloads
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference noteRef = db.collection("resource").document(noteId);

            db.runTransaction((Transaction.Function<Void>) transaction -> {
                DocumentSnapshot snapshot = transaction.get(noteRef);

                // Retrieve current downloads
                Integer currentDownloads = Objects.requireNonNull(snapshot.getLong("resource_downloads")).intValue();

                // Increment download by 1
                currentDownloads++;

                // Update the download count in the document
                transaction.update(noteRef, "resource_downloads", currentDownloads);

                return null;
            }).addOnSuccessListener(aVoid -> {
                // Downloads updated successfully
                downloadPdf(url);
                Toast.makeText(user_notesDetails.this, "Downloaded successfully", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                // Downloads update failed
                Toast.makeText(user_notesDetails.this, "Failed to update downloads", Toast.LENGTH_SHORT).show();
            });
        });

        //handle toggle, like or unlike notes
        likeBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(user_notesDetails.this, "You're not logged in", Toast.LENGTH_SHORT).show();
            } else{
                checkIsLike();
                if (isInMyLike){
                    //in like, remove from like
                    MyApplication.removeFromLikeNote(user_notesDetails.this, noteId);
                } else{
                    //not in like, add to like
                    MyApplication.addToLikeNote(user_notesDetails.this, noteId);
                }
            }
        });

        //handle toggle, favourite notes
        favouriteBtn.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() == null){
                Toast.makeText(user_notesDetails.this, "You're not logged in", Toast.LENGTH_SHORT).show();
            } else{
                checkIsFavourite();
                if (isInMyFavourite){
                    //in favourite, remove from favourite
                    MyApplication.removeFromFavouriteNote(user_notesDetails.this, noteId);
                } else{
                    //not in favourite, add to favourite
                    MyApplication.addToFavouriteNote(user_notesDetails.this, noteId);
                }
            }
        });

        //Comments
        //pop up dialog handle
        progressDialog = new ProgressDialog(this);
        addCommentDialog = new Dialog(this);
        //Inflate the dialog layout
        addCommentDialog.setContentView(R.layout.dialog_add_comment);
        //Find views from the addCommentDialog
        ImageButton closeBtn = addCommentDialog.findViewById(R.id.closeBtn);
        Button submitCommentBtn = addCommentDialog.findViewById(R.id.submitCommentBtn);
        addCommentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //handle click, start comment add screen
        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Requirements: User must be logged in to add comment */
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(user_notesDetails.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else {
                    addCommentDialog();
                }
            }
        });

        //handle click, share url
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = url;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT,message);
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent,"Share to: "));
            }
        });
        progressDialog = new ProgressDialog(this);
        addReportDialog = new Dialog(this);
        addReportDialog.setContentView(R.layout.dialog_report);
        addReportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //handle click, start report add screen
        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReportDialog();
            }
        });
    }

    private void addReportDialog() {
        addReportDialog.setContentView(R.layout.dialog_report);
        addReportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageButton closeBtn = addReportDialog.findViewById(R.id.closeBtn);
        Button submitReportBtn = addReportDialog.findViewById(R.id.submitReportBtn);
        EditText reportTv = addReportDialog.findViewById(R.id.reportNoteEt);

        //Handle click, dismiss dialog
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReportDialog.dismiss();
            }
        });
        // Handle click, add comment
        submitReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate and add comment
                String reportDetails = reportTv.getText().toString().trim();
                validateReportData(reportDetails);
            }
        });

        // Show the dialog
        addReportDialog.show();
    }

    private void validateReportData(String reportDetails) {
        // If validation not empty
        if (TextUtils.isEmpty(reportDetails)) {
            Toast.makeText(this, "Please enter report details", Toast.LENGTH_SHORT).show();
        } else {
            addReportFirebase(reportDetails);
        }
    }

    private void addReportFirebase(String reportDetails) {
        // Show progress
        progressDialog.setMessage("Adding report...");
        progressDialog.show();

        // Timestamp for comment id, comment time
        long timestamp = System.currentTimeMillis();
        Timestamp firebaseTimestamp = new Timestamp(timestamp / 1000, (int) ((timestamp % 1000) * 1000000));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(firebaseAuth.getUid());
        DocumentReference notesRef = db.collection("resource").document(noteId);

        // Setup data to add in Firestore
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("report_details", reportDetails);
        reportData.put("report_timestamp", firebaseTimestamp);
        reportData.put("report_type", "notes");
        reportData.put("resource_id", notesRef);
        reportData.put("user_id", userRef);

        // Get a reference to the Firestore collection "report"
        CollectionReference reportRef = db.collection("report");

        // Add the report data with an auto-generated document ID
        reportRef.add(reportData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(user_notesDetails.this, "Reported on " + MyApplication.formatTimestamp(firebaseTimestamp), Toast.LENGTH_SHORT).show();
                    addReportDialog.dismiss();
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    // Failed to add report
                    progressDialog.dismiss();
                    Toast.makeText(user_notesDetails.this, "Failed to add report due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //load comments
    @SuppressLint("NotifyDataSetChanged")
    private void loadComments(String noteId) {
        // Initialize the ArrayList before adding data into it
        commentArrayList = new ArrayList<>();

        // Get a reference to the Firestore collection "resource"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference resourceRef = db.collection("resource");

        // Get a reference to the specific resource document
        DocumentReference noteRef = resourceRef.document(noteId);

        // Get a reference to the "comments" subcollection within the document
        CollectionReference commentsRef = noteRef.collection("comments");

        commentsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(user_notesDetails.this, "Error loading comments: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Clear the ArrayList before adding data into it
            commentArrayList.clear();

            if (value != null) {
                for (DocumentSnapshot document : value.getDocuments()) {
                    // Log the data to check if it's correct
                    Log.d("CommentData", document.getData().toString());

                    // Create a model class to represent your comments data
                    Comment comment = new Comment(document.getId(),
                            noteId,
                            document.getTimestamp("comment_timestamp"),
                            document.getString("comment_details"),
                            Objects.requireNonNull(document.getDocumentReference("user_id")).getId());

                    // Add the comment model to the ArrayList
                    commentArrayList.add(comment);
                }
                // Notify the adapter that the data has changed
                adapterComment.setCommentArrayList(commentArrayList);
                adapterComment.notifyDataSetChanged();
            }
        });
    }

    //Add comment to firebase
    private void addCommentDialog() {
        // Inflate the dialog layout
        addCommentDialog.setContentView(R.layout.dialog_add_comment);
        addCommentDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Find views from addCommentDialog
        ImageButton closeBtn = addCommentDialog.findViewById(R.id.closeBtn);
        Button submitCommentBtn = addCommentDialog.findViewById(R.id.submitCommentBtn);
        EditText commentTv = addCommentDialog.findViewById(R.id.commentEt);

        // Handle click, dismiss dialog
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCommentDialog.dismiss();
            }
        });

        // Handle click, add comment
        submitCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate and add comment
                String comment = commentTv.getText().toString().trim();
                validateCommentData(comment);
            }
        });

        // Show the dialog
        addCommentDialog.show();
    }

    private void validateCommentData(String comment) {
        // If validation not empty
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Please enter comment", Toast.LENGTH_SHORT).show();
        } else {
            addCommentFirebase(comment);
        }
    }

    private void addCommentFirebase(String comment) {
        // Show progress dialog
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        // Timestamp for comment id
        Timestamp timestamp = Timestamp.now();

        // Get a reference to the Firestore collection "resource"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference resourceRef = db.collection("resource");

        // Get a reference to the specific resource document
        DocumentReference noteRef = resourceRef.document(noteId);

        // Get a reference to the "comments" subcollection within the document
        CollectionReference commentsRef = noteRef.collection("comments");

        // Create a new comment document with an auto-generated ID
        DocumentReference newCommentRef = commentsRef.document();

        // Setup data to add in the comment document
        HashMap<String, Object> commentData = new HashMap<>();
        commentData.put("comment_timestamp", timestamp);
        commentData.put("comment_details", comment);
        commentData.put("user_id", db.collection("user").document(firebaseAuth.getUid())); // Reference type

        // Set the data for the new comment document
        newCommentRef.set(commentData)
                .addOnSuccessListener(aVoid -> {
                    // Comment added successfully
                    Toast.makeText(user_notesDetails.this, "Comment Added", Toast.LENGTH_SHORT).show();
                    addCommentDialog.dismiss();
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    // Failed to add comment
                    progressDialog.dismiss();
                    Toast.makeText(user_notesDetails.this, "Failed to add comment due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    //Method to download the PDF file using DownloadManager
    private void downloadPdf(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // HTTP/HTTPS URL: Use DownloadManager
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(title); // Set the title of the download notification
            request.setDescription("Downloading"); // Set the description of the download notification
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title + ".pdf");

            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (downloadManager != null) {
                downloadManager.enqueue(request);
            } else {
                Toast.makeText(this, "DownloadManager is not available", Toast.LENGTH_SHORT).show();
            }
        } else if (url.startsWith("gs://")) {
            // GCS URL: Use Firebase Storage to download
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl(url);

            File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title + ".pdf");

            storageRef.getFile(localFile)
                    .addOnSuccessListener(taskSnapshot -> {
                        // File downloaded successfully
                        Toast.makeText(this, "Download complete", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(exception -> {
                        // Handle errors
                        Toast.makeText(this, "Download failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Unsupported URL format
            Toast.makeText(this, "Unsupported URL format", Toast.LENGTH_SHORT).show();
        }
    }

    //request storage permission
    private void loadNoteDetails(String noteId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference noteRef = db.collection("resource").document(noteId);

        noteRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {

                // Assuming the categoryName is stored as a DocumentReference
                DocumentReference categoryRef = documentSnapshot.getDocumentReference("category_id");

                assert categoryRef != null;
                categoryRef.get().addOnSuccessListener(categorySnapshot -> {
                    if (categorySnapshot.exists()) {
                        categoryName = categorySnapshot.getString("category_name");
                        noteCategory.setText(categoryName);
                    } else {
                        Log.d("Note details", "Category document does not exist");
                    }
                }).addOnFailureListener(e -> Log.e("Note details", "Error loading category details: " + e.getMessage()));

                // Note exists, retrieve its data
                title = documentSnapshot.getString("resource_name");
                description = documentSnapshot.getString("resource_description");

                // date
                timestamp = documentSnapshot.getTimestamp("resource_upload_datetime");
                String formattedDate = MyApplication.formatTimestamp(timestamp);

                // author
                authorID = Objects.requireNonNull(documentSnapshot.getDocumentReference("user_id")).getId();
                loadAuthor(authorID);
                // Pdf url
                url = documentSnapshot.getString("resource_file");
                loadPdfSize(url);
                loadPdfUrl(url);

                // Increment the number of views
                // number of views, downloads, likes
                Integer views = Objects.requireNonNull(documentSnapshot.getLong("resource_views")).intValue();
                Integer downloads = Objects.requireNonNull(documentSnapshot.getLong("resource_downloads")).intValue();
                Integer likes = Objects.requireNonNull(documentSnapshot.getLong("resource_likes")).intValue();

                // Convert integer values to strings
                String viewsString = String.valueOf(views);
                String downloadsString = String.valueOf(downloads);
                String likesString = String.valueOf(likes);

                // Set the values to TextViews
                numberOfDownloads.setText(downloadsString);
                numberOfViews.setText(viewsString);
                numberOfLikes.setText(String.format("%s likes", likesString));

                // Set the title after data is loaded
                noteTitle.setText(title);
                noteDescription.setText(description);
                noteDate.setText(formattedDate);
            } else {
                // Note with the given ID does not exist
                Log.d("Note details", "Note with ID " + noteId + " does not exist");
            }
        }).addOnFailureListener(e -> {
            // Handle errors if any
            Log.e("Note details", "Error loading note details: " + e.getMessage());
        });
    }

    private void loadPdfUrl(String url) {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            ref.getBytes(MAX_BYTES_PDF)
            .addOnSuccessListener(bytes -> {
                Log.d("Picture", "onSuccess: "+title+" successfully got the file");
                //set to pdfView
                noteImg.fromBytes(bytes)
                .pages(0) //show only the first page
                .spacing(0)
                .swipeHorizontal(false)
                .enableSwipe(false)
                .onError(t -> Log.d("Picture", "onError: "+t.getMessage()))
                .onPageError((page, t) -> Log.d("Picture", "onPageError: "+t.getMessage()))
                .load();
            })
            .addOnFailureListener(e -> {
                    //Log.d(TAG, "onFailure: failed getting file from url due to "+e.getMessage());
            });
        }

    private void loadPdfSize(String url) {
            //using url we can get file and its metadata from firebase storage
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
            ref.getMetadata()
            .addOnSuccessListener(storageMetadata -> {
                //get size in bytes
                double bytes = storageMetadata.getSizeBytes();
                //Log.d(TAG, "onSuccess: "+note.getTitle()+ " "+bytes);
                //convert bytes to KB, MB
                double kb = bytes/1024;
                double mb = kb/1024;
                if (mb>=1){
                    sizeTv.setText(String.format("%.2f",mb)+" MB");
                } else if (kb>=1) {
                    sizeTv.setText(String.format("%.2f",kb)+" KB");
                } else{
                    sizeTv.setText(String.format("%.2f",bytes)+" bytes");
                }
            })
            .addOnFailureListener(e -> {
                //failed getting metadata
                Toast.makeText(user_notesDetails.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

    private void loadAuthor(String authorID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference authorRef = db.collection("user").document(authorID);

        authorRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                authorName = documentSnapshot.getString("user_name");
                author.setText(authorName); // Assuming 'author' is the TextView
            } else {
                Log.d("Note details", "Author document does not exist");
            }
        }).addOnFailureListener(e -> Log.e("Note details", "Error loading author name: " + e.getMessage()));
    }

    private void checkIsFavourite() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(Objects.requireNonNull(firebaseAuth.getUid()));
    
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<DocumentReference> favouriteNotesRefs = (List<DocumentReference>) documentSnapshot.get("favourite_notes");
                
                // Check if the noteId exists in the array of references
                isInMyFavourite = favouriteNotesRefs != null && favouriteNotesRefs.contains(db.collection("resource").document(noteId));

                // Exists in favorite
                // Not exists in favorite
                favouriteBtn.setChecked(isInMyFavourite);
            }
        }).addOnFailureListener(e -> Log.e("Note details", "Error checking favorite: " + e.getMessage()));
    }
    
    private void checkIsLike() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(Objects.requireNonNull(firebaseAuth.getUid()));
    
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<DocumentReference> likedNotesRefs = (List<DocumentReference>) documentSnapshot.get("like_notes");
                
                // Check if the noteId exists in the array of references
                isInMyLike = likedNotesRefs != null && likedNotesRefs.contains(db.collection("resource").document(noteId));

                // Exists in liked notes
                // Not exists in liked notes
                likeBtn.setChecked(isInMyLike);
            }
        }).addOnFailureListener(e -> Log.e("Note details", "Error checking like: " + e.getMessage()));
    }
}
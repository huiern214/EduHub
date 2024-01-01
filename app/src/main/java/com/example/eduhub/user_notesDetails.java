package com.example.eduhub;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.eduhub.databinding.ActivityUserNotesDetailsBinding;
import com.example.eduhub.databinding.DialogAddCommentBinding;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class user_notesDetails extends AppCompatActivity {
    private ActivityUserNotesDetailsBinding binding;
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private static final int SAF_REQUEST_CODE = 0;
    private String title, description, authorName, dateUploaded, categoryName, authorID, url, noteId;
    private long timestamp;
    TextView noteTitle, noteDescription, noteCategory, noteDate, author,sizeTv, numberOfViews, numberOfDownloads, numberOfLikes;
    PDFView noteImg;
    ImageButton backBtn, downloadBtn, addCommentBtn, shareBtn, reportBtn;
    ToggleButton likeBtn, favouriteBtn;
    Button readBtn;
    boolean isInMyFavourite=false;
    boolean isInMyLike=false;
    private FirebaseAuth firebaseAuth;
    //private dialog
    private ProgressDialog progressDialog;
    //arraylist to hold comments
    private ArrayList<user_modelComment> commentArrayList;
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
        
        Toast.makeText(this, "noteID: " + noteId, Toast.LENGTH_SHORT).show();

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
        noteCategory = findViewById(R.id.categoryName);
        noteDate = findViewById(R.id.date);
        author = findViewById(R.id.authorName);
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
                startActivity(new Intent(user_notesDetails.this, user_DashboardActivity.class));
            }
        });

        //handle click, open to view notes
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the number of views
                DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(noteId);
                noteRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        // Retrieve current views
                        Integer currentViews = mutableData.child("Views").getValue(Integer.class);
                        if (currentViews == null) {
                            currentViews = 0; // If the field is null, default to 0
                        }

                        // Increment views by 1
                        mutableData.child("Views").setValue(currentViews + 1);

                        // Set value back to the database
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                        // Handle completion
                        if (committed && dataSnapshot != null) {
                            //Add the note to the user's read notes
                            MyApplication.addToReadNote(user_notesDetails.this,noteId);
                            // Views updated successfully
                            Intent intent = new Intent(user_notesDetails.this, user_readNote.class);
                            intent.putExtra("noteId", noteId);
                            startActivity(intent);
                        } else {
                            // Views update failed
                            Toast.makeText(user_notesDetails.this, "Failed to update views", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //handle click, download notes
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the number of downloads
                DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(noteId);
                noteRef.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        // Retrieve current downloads
                        Integer currentDownloads = mutableData.child("Download").getValue(Integer.class);
                        if (currentDownloads == null) {
                            currentDownloads = 0; // If this field is null, default to 0
                        }

                        // Increment download by 1
                        mutableData.child("Download").setValue(currentDownloads + 1);

                        // Set value back to the database
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                        if (committed) {
                            // Downloads updated successfully
                            downloadPdf(url);
                            Toast.makeText(user_notesDetails.this, "Downloaded successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Downloads update failed
                            Toast.makeText(user_notesDetails.this, "Failed to update downloads", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //handle toggle, like or unlike notes
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(user_notesDetails.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else{
                    DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(noteId);
                    if (isInMyLike){
                        //User unlike the note, decrement likes
                        noteRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Integer currentLikes = mutableData.child("Likes").getValue(Integer.class);
                                if (currentLikes == null){
                                    currentLikes = 0;
                                }

                                //Decrement likes by 1
                                mutableData.child("Likes").setValue(Math.max(currentLikes-1,0));

                                //Set value back to the database
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                if (!committed){
                                    Log.e("LikeButton", "Failed to update likes:"+error.getMessage());
                                }
                            }
                        });
                        //in like, remove from like
                        MyApplication.removeFromLikeNote(user_notesDetails.this, noteId);
                    } else{
                        //User liked the note, increment likes
                        noteRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Integer currentLikes = currentData.child("Likes").getValue(Integer.class);
                                if (currentLikes== null){
                                    currentLikes =0;
                                }

                                //Increment likes by 1
                                currentData.child("Likes").setValue(currentLikes+1);

                                //Set value back to the database
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                                if(!committed){
                                    Log.e("LikeButton","Failed to update likes: "+error.getMessage());
                                }
                            }
                        });
                        //not in favourite, add to favourite
                        MyApplication.addToLikeNote(user_notesDetails.this, noteId);
                    }
                }
            }
        });

        //handle toggle, favourite notes
        favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(user_notesDetails.this, "You're not logged in", Toast.LENGTH_SHORT).show();
                } else{
                    if (isInMyFavourite){
                        //in favourite, remove from favourite
                        MyApplication.removeFromFavouriteNote(user_notesDetails.this, noteId);
                    } else{
                        //not in favourite, add to favourite
                        MyApplication.addToFavouriteNote(user_notesDetails.this, noteId);
                    }
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
        //show progress
        progressDialog.setMessage("Adding report...");
        progressDialog.show();

        //timestamp for comment id, comment time
        String timestamp = ""+System.currentTimeMillis();

        //Setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("reportId",""+timestamp);
        hashMap.put("noteId",""+noteId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("report",""+reportDetails);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //DB path to add data into it
        //Notes > NotesId > Comments > commentId >commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes");
        ref.child(noteId).child("Reports").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(user_notesDetails.this, "Reported on "+MyApplication.formatTimestamp(Long.parseLong(timestamp)),Toast.LENGTH_SHORT).show();
                        addReportDialog.dismiss();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add comment
                        progressDialog.dismiss();
                        Toast.makeText(user_notesDetails.this, "Failed to add report due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //load comments (bug)
    private void loadComments(String noteId) {
        //init arraylist before adding data into it
        commentArrayList = new ArrayList<>();
        //db path to load comments
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Notes").child(noteId).child("Comments");
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear arraylist before starting adding data into it
                commentArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    // Log the data to check if it's correct
                    Log.d("CommentData", ds.getValue().toString());
                    // Get data as model, spellings of variables in model must be the same as in Firebase
                    user_modelComment model = ds.getValue(user_modelComment.class);
                    // Add to arraylist
                    commentArrayList.add(model);
                }
                adapterComment.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(user_notesDetails.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        //show progress
        progressDialog.setMessage("Adding comment...");
        progressDialog.show();

        //timestamp for comment id, comment time
        String timestamp = ""+System.currentTimeMillis();

        //Setup data to add in db for comment
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("commentId",""+timestamp);
        hashMap.put("noteId",""+noteId);
        hashMap.put("timestamp",timestamp);
        hashMap.put("comment",""+comment);
        hashMap.put("uid",""+firebaseAuth.getUid());

        //DB path to add data into it
        //Notes > NotesId > Comments > commentId >commentData
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes");
        ref.child(noteId).child("Comments").child(timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(user_notesDetails.this, "Comment Added",Toast.LENGTH_SHORT).show();
                        addCommentDialog.dismiss();
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed to add comment
                        progressDialog.dismiss();
                        Toast.makeText(user_notesDetails.this, "Failed to add comment due to "+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    //Method to download the PDF file using DownloadManager
    private void downloadPdf(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title); //Set the title of the download notification
        request.setDescription("Downloading"); //Set the description of the download notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title+".pdf");

        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager != null){
            downloadManager.enqueue(request);
        } else{
            Toast.makeText(this,"DownloadManager is not available",Toast.LENGTH_SHORT).show();
        }
    }

    //request storage permission
    private void loadNoteDetails(String noteId) {
        //Log.d("Note details", "Receiver noteId: "+noteId);
        DatabaseReference noteRef = FirebaseDatabase.getInstance().getReference().child("Notes").child(noteId);
        noteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    //Note exists, retrieve its data
                    title= snapshot.child("title").getValue(String.class);
                    description = snapshot.child("description").getValue(String.class);
                    categoryName = snapshot.child("category").getValue(String.class);
                    //date
                    timestamp = snapshot.child("timestamp").getValue(Long.class);
                    dateUploaded = MyApplication.formatTimestamp(timestamp);
                    //author
                    authorID= snapshot.child("author_uid").getValue(String.class);
                    loadAuthor(authorID);
                    //Pdf url
                    url = snapshot.child("url").getValue(String.class);
                    loadPdfSize(url);
                    loadPdfUrl(url);

                    //Increment the number of views
                    //number of views, downloads, likes
                    Integer views = snapshot.child("Views").getValue(Integer.class);
                    Integer downloads = snapshot.child("Download").getValue(Integer.class);
                    Integer likes = snapshot.child("Likes").getValue(Integer.class);

                    // Convert integer values to strings with null checks
                    String viewsString = views != null ? String.valueOf(views) : "0";
                    String downloadsString = downloads != null ? String.valueOf(downloads) : "0";
                    String likesString = likes != null ? String.valueOf(likes) : "0";

                    // Set the values to TextViews
                    numberOfDownloads.setText(downloadsString);
                    numberOfViews.setText(viewsString);
                    numberOfLikes.setText(likesString+" likes");

                    //Set the title after data is loaded
                    noteTitle.setText(title);
                    noteDescription.setText(description);
                    noteCategory.setText(categoryName);
                    noteDate.setText(dateUploaded);
                }else{
                    //Note with the given ID does not exist
                    Log.d("Note details", "Note with ID  "+noteId+" does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //Handle errors if any
                Log.e("Note details", "Error loading note details: "+error.getMessage());
            }
        });
    }

    private void loadPdfUrl(String url) {
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d("Picture", "onSuccess: "+title+" successfully got the file");
                        //set to pdfView
                        noteImg.fromBytes(bytes)
                                .pages(0) //show only the first page
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Log.d("Picture", "onError: "+t.getMessage());
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Log.d("Picture", "onPageError: "+t.getMessage());
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.d(TAG, "onFailure: failed getting file from url due to "+e.getMessage());
                    }
                });
    }

    private void loadPdfSize(String url) {
        //using url we can get file and its metadata from firebase storage
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
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
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed getting metadata
                        Toast.makeText(user_notesDetails.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAuthor(String authorID) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(authorID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //get author Name
                        authorName = ""+snapshot.child("name").getValue();
                        //set to author text view
                        author.setText(authorName);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.d("Unable to load author name due to ",error.getMessage());
                    }
                });
    }

    private void checkIsFavourite(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("FavouriteNote").child(noteId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavourite = snapshot.exists(); // true if exists, false if not exists
                        if (isInMyFavourite){
                            //exists in favourite
                            favouriteBtn.setChecked(true);
                            //favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_border_24,0,0);
                            //Toast.makeText(user_notesDetails.this, "Remove favourite ", Toast.LENGTH_SHORT).show();
                        }else{
                            //not exists in favourite
                            favouriteBtn.setChecked(false);
                            //favouriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.baseline_favorite_24,0,0);
                            //Toast.makeText(user_notesDetails.this, "Add favourite", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(user_notesDetails.this, "Unable to check favourite list",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIsLike(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("LikeNote").child(noteId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyLike = snapshot.exists(); // true if exists, false if not exists
                        if (isInMyLike){
                            //exists in like
                            likeBtn.setChecked(true);
                            //Toast.makeText(user_notesDetails.this, "Unliked", Toast.LENGTH_SHORT).show();
                        }else{
                            //not exists in like
                            likeBtn.setChecked(false);
                            //Toast.makeText(user_notesDetails.this, "Liked", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(user_notesDetails.this, "Unable to check like list",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

package com.example.eduhub;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

public class user_notesDetails extends AppCompatActivity {
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    private static final int SAF_REQUEST_CODE = 0;
    private String title, description, authorName, dateUploaded, categoryName, authorID, url, noteId;
    private Timestamp timestamp;
            TextView noteTitle, noteDescription, noteCategory, noteDate, author,sizeTv, numberOfViews, numberOfDownloads, numberOfLikes;
            PDFView noteImg;
            ImageButton backBtn, downloadBtn;
            ToggleButton likeBtn, favouriteBtn;
            Button readBtn;
            boolean isInMyFavourite=false;
            boolean isInMyLike=false;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notes_details);

        //Retrieve the noteID from the intent
        noteId = getIntent().getStringExtra("noteId");
        loadNoteDetails(noteId);
        // Toast.makeText(this, "noteID: " + noteId, Toast.LENGTH_SHORT).show();

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

        //handle click, go back
        backBtn.setOnClickListener(v -> startActivity(new Intent(user_notesDetails.this, user_HomeFragment.class)));

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
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss"); // Define your desired date format
                String formattedDate = sdf.format(timestamp.toDate());

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
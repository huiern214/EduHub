package com.example.eduhub.adapter;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.MyApplication;
import com.example.eduhub.R;
import com.example.eduhub.model.Notes;
import com.example.eduhub.user_notesDetails;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

public class user_AdapterNote extends RecyclerView.Adapter<user_AdapterNote.ViewHolder> {
    private Context context;
    private List<Notes> noteList;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private FirebaseAuth firebaseAuth;
    boolean isInMyFavourite = false, isInMyLike = false;
    String noteId;

    public user_AdapterNote(Context context, List<Notes> noteList) {
        this.context = context;
        this.noteList = noteList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNoteList(List<Notes> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notes, parent, false);
        //debugging skill needed
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // Initialize noteId when creating the view holder
            noteId = noteList.get(viewType).getNotes_id();
            ViewHolder viewHolder = new ViewHolder(view);
            checkIsFavourite(viewHolder.favouriteBtn, noteId);
            checkIsLike(viewHolder.likeBtn, noteId);
            return viewHolder;
        }
        return new ViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        // Use position as viewType to set noteId in onCreateViewHolder
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull user_AdapterNote.ViewHolder holder, int position) {

        Notes note = noteList.get(position);
        holder.noteTitle.setText(note.getResource_name());
        holder.noteDescription.setText(note.getResource_description());
//        holder.noteCategory.setText(note.getCategory_id());
        noteId = note.getNotes_id();
        Log.d(TAG, "note id onbind" + noteId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoriesRef = db.collection("category");

        categoriesRef.document(note.getCategory_id())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Retrieve the category name
                            String category_name = document.getString("category_name");
                            // Now you have the category name
                            // You can use it as needed, for example, displaying it in a TextView
                            holder.noteCategory.setText(category_name);
                        } else {
                            // Handle the case where the category document doesn't exist
                            Log.d(TAG, "No such category document");
                        }
                    } else {
                        // Handle errors here
                        Log.w(TAG, "Error getting category document", task.getException());
                    }
                });

        Timestamp timestamp = note.getResource_upload_datetime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss"); // Define your desired date format
        String formattedDate = sdf.format(timestamp.toDate());
        holder.timestamp.setText(formattedDate);
        checkIsLike(holder.likeBtn, noteId);
        checkIsFavourite(holder.favouriteBtn, noteId);

        // Load further details like author, pdf from URL, pdf size in separate functions
        loadPdfUrl(note, holder);
        loadPdfSize(note, holder);
        loadAuthor(note, holder);

        // Setting the note ID as a tag to the itemView for easy retrieval in OnClick
        holder.itemView.setTag(note.getNotes_id());

        // Set click listeners for the like and favorite buttons/icons
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(holder.itemView.getContext(), "You're not logged in", Toast.LENGTH_SHORT).show();
                } else{
                    String clickedNoteId = noteList.get(holder.getAdapterPosition()).getNotes_id();
                    checkIsLike(holder.likeBtn, clickedNoteId);
                    if (isInMyLike){
                        //in like, remove from like
                        MyApplication.removeFromLikeNote(holder.itemView.getContext(), clickedNoteId);
                    } else{
                        //not in like, add to like
                        MyApplication.addToLikeNote(holder.itemView.getContext(), clickedNoteId);
                    }
                }
            }
        });

        holder.favouriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (firebaseAuth.getCurrentUser() == null){
                    Toast.makeText(holder.itemView.getContext(), "You're not logged in", Toast.LENGTH_SHORT).show();
                } else{
                    String clickedNoteId = noteList.get(holder.getAdapterPosition()).getNotes_id();
                    checkIsFavourite(holder.favouriteBtn, clickedNoteId);
                    if (isInMyFavourite){
                        //in favourite, remove from favourite
                        MyApplication.removeFromFavouriteNote(holder.itemView.getContext(), clickedNoteId);

                    } else{
                        //not in favourite, add to favourite
                        MyApplication.addToFavouriteNote(holder.itemView.getContext(), clickedNoteId);
                    }
                }
            }
        });
    }


    public void checkIsFavourite(ToggleButton favouriteBtn, String noteId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(firebaseAuth.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<DocumentReference> favouriteNotesRefs = (List<DocumentReference>) documentSnapshot.get("favourite_notes");

                // Check if the noteId exists in the array of references
                isInMyFavourite = favouriteNotesRefs != null && favouriteNotesRefs.contains(db.collection("resource").document(noteId));

                if (isInMyFavourite) {
                    // Exists in favorite
                    favouriteBtn.setChecked(true);
                } else {
                    // Not exists in favorite
                    favouriteBtn.setChecked(false);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Note details", "Error checking favorite: " + e.getMessage());
        });
    }

    public void checkIsLike(ToggleButton likeBtn, String noteId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(firebaseAuth.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<DocumentReference> likedNotesRefs = (List<DocumentReference>) documentSnapshot.get("like_notes");

                // Check if the noteId exists in the array of references
                isInMyLike = likedNotesRefs != null && likedNotesRefs.contains(db.collection("resource").document(noteId));

                if (isInMyLike) {
                    // Exists in liked notes
                    likeBtn.setChecked(true);
                } else {
                    // Not exists in liked notes
                    likeBtn.setChecked(false);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("Note details", "Error checking like: " + e.getMessage());
        });
    }

    private void loadAuthor(Notes note, ViewHolder holder) {
        // Get author using authorUid
        String author_uid = note.getUser_id();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("user"); // Assuming the collection is named 'user'

        usersRef.document(author_uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get author name
                            String authorName = document.getString("user_name"); // Assuming the field is named 'user_name'
                            // Set to author text view
                            holder.noteAuthor.setText(authorName);
                        } else {
                            // Handle the case where the user document doesn't exist
                            Log.d(TAG, "No such user document");
                        }
                    } else {
                        // Handle errors here
                        Log.w(TAG, "Error getting user document", task.getException());
                    }
                });
    }

    private void loadPdfSize(Notes note, ViewHolder holder) {
        // Using URL we can get file and its metadata from Firebase Storage
        String pdfUrl = note.getResource_file();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        // Get size in bytes
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: " + note.getResource_name() + " " + bytes);
                        // Convert bytes to KB, MB
                        double kb = bytes / 1024;
                        double mb = kb / 1024;
                        if (mb >= 1) {
                            holder.noteSize.setText(String.format("%.2f", mb) + " MB");
                        } else if (kb >= 1) {
                            holder.noteSize.setText(String.format("%.2f", kb) + " KB");
                        } else {
                            holder.noteSize.setText(String.format("%.2f", bytes) + " bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed getting metadata
                        Log.e(TAG, "loadPdfSize:onFailure", e);
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadPdfUrl(Notes note, ViewHolder holder) {
        // Using URL we can get file and its metadata from Firebase Storage
        String pdfUrl = note.getResource_file();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: " + note.getResource_name() + " successfully got the file");
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
        return noteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView noteTitle, noteDescription, noteCategory;
        private TextView timestamp, noteSize, noteAuthor;
        private PDFView pdfView;
        private ToggleButton favouriteBtn, likeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            noteTitle = itemView.findViewById(R.id.titleTv);
            noteDescription = itemView.findViewById(R.id.descriptionTv);
            noteCategory = itemView.findViewById(R.id.categoryTv);
            timestamp = itemView.findViewById(R.id.dateTv);
            noteSize = itemView.findViewById(R.id.sizeTv);
            pdfView = itemView.findViewById(R.id.pdfView);
            noteAuthor = itemView.findViewById(R.id.authorTv);
            favouriteBtn = itemView.findViewById(R.id.favouriteBtn);
            likeBtn = itemView.findViewById(R.id.LikeBtn);

            // Set click listener on the entire item view
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Handle item click here
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Notes clickedNote = noteList.get(position);

                // Pass the note ID to other activity
                Intent intent = new Intent(context, user_notesDetails.class);
                intent.putExtra("noteId", clickedNote.getNotes_id());
                context.startActivity(intent);
            }
        }
    }

}


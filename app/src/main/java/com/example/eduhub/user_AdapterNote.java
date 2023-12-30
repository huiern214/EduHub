package com.example.eduhub;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.List;

public class user_AdapterNote extends RecyclerView.Adapter<user_AdapterNote.ViewHolder> {
    private Context context;
    private List<user_modelPdf> noteList;
    private static final String TAG = "PDF_ADAPTER_TAG";
    private FirebaseAuth firebaseAuth;
    boolean isInMyFavourite = false;
    boolean isInMyLike = false;

    public user_AdapterNote(Context context, List<user_modelPdf> noteList) {
        this.context = context;
        this.noteList = noteList;
    }

    public void setNoteList(List<user_modelPdf> noteList) {
        this.noteList = noteList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notes, parent, false);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            // Initialize noteId when creating the view holder
            String noteId = noteList.get(viewType).getId();
            ViewHolder viewHolder = new ViewHolder(view);
            checkIsFavourite(noteId, viewHolder.getFavouriteBtn());
            checkIsLike(noteId, viewHolder.getLikeBtn());
            return viewHolder;
        }
        return new ViewHolder(view);
    }

    private void checkIsLike(String noteId, ToggleButton likeBtn) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("LikeNote").child(noteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyLike = snapshot.exists(); //true if exists, false if not exists
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
                        Toast.makeText(context, "Unable to check like list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemViewType(int position) {
        // Use position as viewType to set noteId in onCreateViewHolder
        return position;
    }

    //Debugging needed for favourite button
    private void checkIsFavourite(String noteId, ToggleButton favouriteBtn) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("FavouriteNote").child(noteId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isInMyFavourite = snapshot.exists(); // true if exists, false if not exists
                        if (isInMyFavourite) {
                            // Exists in favourite
                            // Handle favouriteBtn based on your logic
                            favouriteBtn.setChecked(true);

                        } else {
                            // Not exists in favourite
                            // Handle favouriteBtn based on your logic
                            favouriteBtn.setChecked(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "checkIsFavourite:onCancelled", error.toException());
                        Toast.makeText(context, "Unable to check favourite list", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBindViewHolder(@NonNull user_AdapterNote.ViewHolder holder, int position) {
        user_modelPdf note = noteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.noteDescription.setText(note.getDescription());
        holder.noteCategory.setText(note.getCategory());
        holder.setNodeId(note.getId());

        long timestamp = note.getTimestamp();
        String formattedDate = MyApplication.formatTimestamp(timestamp);
        holder.timestamp.setText(formattedDate);

        // Load further details like author, pdf from URL, pdf size in separate functions
        loadPdfUrl(note, holder);
        loadPdfSize(note, holder);
        loadAuthor(note, holder);

        // Setting the note ID as a tag to the itemView for easy retrieval in OnClick
        holder.itemView.setTag(note.getId());

        //Set the number of likes in the TextView
        setNumberOfLikes(note.getId(), holder.numberOfLikes);
    }

    private void setNumberOfLikes(String id, TextView numberOfLikes) {
        DatabaseReference likeRef = FirebaseDatabase.getInstance().getReference("Notes").child(id).child("Likes");
        likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Get the number of likes
                Integer likes = snapshot.getValue(Integer.class);
                if (likes != null){
                    //Set the number of likes in the TextView
                    numberOfLikes.setText(likes+" Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "setNumberOfLikes: onCancelled ",error.toException());
            }
        });
    }

    private void loadAuthor(user_modelPdf note, ViewHolder holder) {
        // Get author using authorUid
        String author_uid = note.getAuthor_uid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(author_uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get author name
                        String authorName = "" + snapshot.child("name").getValue();
                        // Set to author text view
                        holder.noteAuthor.setText(authorName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "loadAuthor:onCancelled", error.toException());
                    }
                });
    }

    private void loadPdfSize(user_modelPdf note, ViewHolder holder) {
        // Using URL we can get file and its metadata from Firebase Storage
        String pdfUrl = note.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        // Get size in bytes
                        double bytes = storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: " + note.getTitle() + " " + bytes);
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

    private void loadPdfUrl(user_modelPdf note, ViewHolder holder) {
        // Using URL we can get file and its metadata from Firebase Storage
        String pdfUrl = note.getUrl();
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: " + note.getTitle() + " successfully got the file");
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
        private TextView noteTitle, noteDescription, noteCategory, numberOfLikes;
        private TextView timestamp, noteSize, noteAuthor;
        private PDFView pdfView;
        private ToggleButton favouriteBtn, likeBtn;
        private String noteId;

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
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);

            // Set click listener on the entire item view
            itemView.setOnClickListener(this);

          //favourite button
            favouriteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
                    } else{
                        if (isInMyFavourite){
                            //in favourite, remove from favourite
                            MyApplication.removeFromFavouriteNote(context, noteId);
                        } else{
                            //not in favourite, add to favourite
                            MyApplication.addToFavouriteNote(context, noteId);
                        }
                    }
                }
            });

            //Like button -- need to debug as the number of likes only increase not decrease
            likeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (firebaseAuth.getCurrentUser() == null){
                        Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
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
                            MyApplication.removeFromLikeNote(context, noteId);
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
                            MyApplication.addToLikeNote(context, noteId);
                        }
                    }
                }
            });
        }


        public ToggleButton getFavouriteBtn(){
            return favouriteBtn;
        }

        public ToggleButton getLikeBtn() {
            return likeBtn;
        }

        @Override
        public void onClick(View v) {
            // Handle item click here
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                user_modelPdf clickedNote = noteList.get(position);

                // Pass the note ID to other activity
                Intent intent = new Intent(context, user_notesDetails.class);
                intent.putExtra("noteId", clickedNote.getId());
                context.startActivity(intent);
            }
        }

        public void setNodeId(String id) {
            this.noteId = id;
        }
    }
}
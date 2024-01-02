package com.example.eduhub.adapter;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eduhub.MyApplication;
import com.example.eduhub.R;
import com.example.eduhub.model.Comment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class user_AdapterComment extends RecyclerView.Adapter<user_AdapterComment.HolderComment> {
    // context
    private Context context;

    // arraylist to hold comments
    private ArrayList<Comment> commentArrayList;
    private FirebaseAuth firebaseAuth;

    // constructor
    public user_AdapterComment(Context context, ArrayList<Comment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCommentArrayList(ArrayList<Comment> commentArrayList) {
        this.commentArrayList = commentArrayList;
    }

    @NonNull
    @Override
    public HolderComment onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate / bind the view xml
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);
        return new HolderComment(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderComment holder, int position) {
        // Get data from the specific position of the list and set data

        // Get data
        Comment modelComment = commentArrayList.get(holder.getAdapterPosition());
        String comment = modelComment.getComment_details();
        Timestamp timestamp = modelComment.getComment_timestamp();
        String uid = modelComment.getUser_id();

        // Format date using the MyApplication method
        String date = MyApplication.formatTimestamp(timestamp);

        // Set data
        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

        // Load user details including name and profile image
        loadUserDetails(uid, holder);
    }

    private void loadUserDetails(String uid, HolderComment holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get data
                String name = documentSnapshot.getString("user_name");
                String profileImg = documentSnapshot.getString("user_photo");

                // Set data
                holder.nameTv.setText(name);
                Log.d(TAG, "nameee" + profileImg);

                if (profileImg != null && (profileImg.startsWith("http://") || profileImg.startsWith("https://"))) {
                    // HTTP/HTTPS URL: Use Glide to load the image from the web
                    Glide.with(holder.profileIv.getContext())
                            .load(profileImg)
                            .placeholder(R.drawable.baseline_person_2_24)
                            .error(R.drawable.baseline_person_2_24)
                            .into(holder.profileIv);
                } else if (profileImg != null && profileImg.startsWith("gs://")) {
                    // GS URL: Use Firebase Storage to load the image
                    StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImg);

                    // Get the download URL for the file
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImgUrl = uri.toString();

                        Log.d(TAG, "loadUserDetails: " + profileImgUrl);

                        // Load the image using Glide
                        Glide.with(context)
                                .load(profileImgUrl)
                                .placeholder(R.drawable.baseline_person_2_24)
                                .error(R.drawable.baseline_person_2_24)
                                .into(holder.profileIv);
                    }).addOnFailureListener(e -> {
                        // Handle the failure to get the download URL
                        holder.profileIv.setImageResource(R.drawable.baseline_person_2_24);
                    });
                } else {
                    // Handle unsupported URL format or null URL
                    holder.profileIv.setImageResource(R.drawable.baseline_person_2_24);
                }
            }
        }).addOnFailureListener(e -> {
            // Handle the failure to fetch user details
        });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size(); // return the size of comments, the number of records
    }

    // view holder class for row_comment.xml
    class HolderComment extends RecyclerView.ViewHolder {
        // ui views of row_comment.xml
        ShapeableImageView profileIv;
        TextView nameTv, dateTv, commentTv;

        public HolderComment(@NonNull View itemView) {
            super(itemView);
            // initialize UI views
            profileIv = itemView.findViewById(R.id.profileImg);
            nameTv = itemView.findViewById(R.id.nameOfUserComment);
            dateTv = itemView.findViewById(R.id.dateOfUserComment);
            commentTv = itemView.findViewById(R.id.commentOfUserComment);
        }
    }
}
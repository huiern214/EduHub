package com.example.eduhub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eduhub.databinding.RowCommentsBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class user_AdapterComment extends RecyclerView.Adapter<user_AdapterComment.HolderComment> {
    // context
    private Context context;
    // arraylist to hold comments
    private ArrayList<user_modelComment> commentArrayList;
    private FirebaseAuth firebaseAuth;

    // constructor
    public user_AdapterComment(Context context, ArrayList<user_modelComment> commentArrayList) {
        this.context = context;
        this.commentArrayList = commentArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
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
        user_modelComment modelComment = commentArrayList.get(position);
        String comment = modelComment.getComment();
        String timestamp = modelComment.getTimestamp();
        String uid = modelComment.getUid();

        // Format date using the MyApplication method
        String date = MyApplication.formatTimestamp(Long.parseLong(timestamp));

        // Set data
        holder.dateTv.setText(date);
        holder.commentTv.setText(comment);

        // Load user details including name and profile image
        loadUserDetails(uid, holder);
    }

    private void loadUserDetails(String uid, HolderComment holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Get data
                            String name = snapshot.child("name").getValue(String.class);
                            String profileImg = snapshot.child("profileImage").getValue(String.class);

                            // Set data
                            holder.nameTv.setText(name);
                            try {
                                Glide.with(context)
                                        .load(profileImg)
                                        .placeholder(R.drawable.baseline_person_2_24)
                                        .into(holder.profileTv);
                            } catch (Exception e) {
                                holder.profileTv.setImageResource(R.drawable.baseline_person_2_24);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle onCancelled
                    }
                });
    }

    @Override
    public int getItemCount() {
        return commentArrayList.size(); // return the size of comments, the number of records
    }

    // view holder class for row_comment.xml
    class HolderComment extends RecyclerView.ViewHolder {
        // ui views of row_comment.xml
        ShapeableImageView profileTv;
        TextView nameTv, dateTv, commentTv;

        public HolderComment(@NonNull View itemView) {
            super(itemView);
            // initialize UI views
            profileTv = itemView.findViewById(R.id.profileImg);
            nameTv = itemView.findViewById(R.id.nameOfUserComment);
            dateTv = itemView.findViewById(R.id.dateOfUserComment);
            commentTv = itemView.findViewById(R.id.commentOfUserComment);
        }
    }
}
package com.example.eduhub.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eduhub.MyApplication;
import com.example.eduhub.R;
import com.example.eduhub.admin_report_details;
import com.example.eduhub.model.Report;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class admin_AdapterReport extends RecyclerView.Adapter<admin_AdapterReport.HolderReport> {
    // context
    private Context context;

    // arraylist to hold reports
    private ArrayList<Report> reportArrayList;
    private FirebaseAuth firebaseAuth;

    // constructor
    public admin_AdapterReport(Context context, ArrayList<Report> reportArrayList) {
        this.context = context;
        this.reportArrayList = reportArrayList;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setReportArrayList(ArrayList<Report> reportArrayList) {
        this.reportArrayList = reportArrayList;
    }

    @NonNull
    @Override
    public HolderReport onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate / bind the view xml
        View itemView = LayoutInflater.from(context).inflate(R.layout.row_user_reports_details, parent, false);
        return new HolderReport(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderReport holder, int position) {
        // Get data from the specific position of the list and set data

        // Get data
        Report modelReport = reportArrayList.get(holder.getAdapterPosition());
        String reportDetails = modelReport.getReportDetails();
        Timestamp timestamp = modelReport.getReport_timestamp();
        String uid = modelReport.getUser_id();
        if (modelReport.getIs_solved()){
            holder.statusTv.setText("Solved");
            holder.solvedBtn.setVisibility(View.GONE);
            holder.undoBtn.setVisibility(View.VISIBLE);
        };

        // Format date using the MyApplication method
        String date = MyApplication.formatTimestamp(timestamp);

        // Set data
        holder.dateTv.setText(date);
        holder.detailsTv.setText(reportDetails);

        // Load user details including name and profile image
        loadUserDetails(uid, holder);

        holder.solvedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReportStatusForSpecificReport(reportArrayList.get(holder.getAdapterPosition()).getReport_id(), true, holder);
            }
        });

        holder.undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReportStatusForSpecificReport(reportArrayList.get(holder.getAdapterPosition()).getReport_id(), false, holder);
            }
        });
    }

    private void loadUserDetails(String uid, HolderReport holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Get data
                String name = documentSnapshot.getString("user_name");
                String profileImg = documentSnapshot.getString("user_photo");

                // Set data
                holder.nameTv.setText(name);

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

    private void setReportStatusForSpecificReport(String reportId, Boolean set_is_solved, HolderReport holder) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reportRef = db.collection("report").document(reportId);

        // Check if the document exists
        reportRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Update the 'is_solved' field to true
                        reportRef.update("is_solved", set_is_solved)
                                .addOnSuccessListener(aVoid -> {
                                    // Report status updated successfully
                                    // You can add your code here to handle the success case
                                    if (set_is_solved){
                                        Toast.makeText(this.context, "Solved", Toast.LENGTH_SHORT);
                                        holder.statusTv.setText("Solved");
                                        holder.solvedBtn.setVisibility(View.GONE);
                                        holder.undoBtn.setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(this.context, "Undo", Toast.LENGTH_SHORT);
                                        holder.statusTv.setText("Pending");
                                        holder.undoBtn.setVisibility(View.GONE);
                                        holder.solvedBtn.setVisibility(View.VISIBLE);
                                    }

                                })
                                .addOnFailureListener(e -> {
                                    // Error handling in case of failure
                                    Log.w("Firestore", "Error updating report status: ", e);
                                    // Consider notifying the user or taking appropriate actions
                                });
                    } else {
                        // The report document with the specified ID does not exist
                        // Handle this case accordingly (e.g., display an error message)
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle errors gracefully
                    Log.w("Firestore", "Error retrieving report: ", e);
                    // Consider notifying the user or taking appropriate actions
                });
    }

    @Override
    public int getItemCount() {
        return reportArrayList.size(); // return the size of reports, the number of records
    }

    // view holder class for row_user_reports_details.xml
    public static class HolderReport extends RecyclerView.ViewHolder {
        ShapeableImageView profileIv;
        TextView nameTv, dateTv, statusTv, detailsTv;
        Button solvedBtn, undoBtn;

        public HolderReport(@NonNull View itemView) {
            super(itemView);
            // initialize UI views
            profileIv = itemView.findViewById(R.id.profileImg);
            nameTv = itemView.findViewById(R.id.reporterTv);
            dateTv = itemView.findViewById(R.id.reportedDate);
            statusTv = itemView.findViewById(R.id.reportedStatus);
            detailsTv = itemView.findViewById(R.id.reportDescription);
            solvedBtn = itemView.findViewById(R.id.solveBtn);
            undoBtn = itemView.findViewById(R.id.undoBtn);
        }
    }
}
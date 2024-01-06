package com.example.eduhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.eduhub.adapter.admin_AdapterReport;
import com.example.eduhub.databinding.ActivityAdminReportsBinding;
import com.example.eduhub.model.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class admin_reports extends AppCompatActivity {
    private ActivityAdminReportsBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<Report> reportArrayList;
    private admin_AdapterReport adapterReport;
    private final String TAG = "ADMIN_REPORTS_TAG";

    RecyclerView reportRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //Load reported notes
        retrieveReportsFromFirebase();

        // Initialize RecyclerView and Adapter
        reportRv = binding.reportsRv;
        reportRv.setLayoutManager(new LinearLayoutManager(this));
        reportArrayList = new ArrayList<>();
        adapterReport = new admin_AdapterReport(this, reportArrayList);
        reportRv.setAdapter(adapterReport);
    }

    private void retrieveReportsFromFirebase() {
        //Initialize ArrayList
        reportArrayList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reportRef = db.collection("report");

        reportRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                reportArrayList.clear();
                for (QueryDocumentSnapshot document : task.getResult()){
                    String report_id = document.getId();
                    String report_timestamp = document.getString("report_timestamp");
                    String report_details = document.getString("report_details");
                    String resource_id = document.getString("resource_id");
                    String user_id = document.getString("user_id");

                    //Create your report object with the report ID
                    Report report = new Report(report_id, report_details, report_timestamp, resource_id, user_id);

                    reportArrayList.add(report);
                }
                adapterReport.notifyDataSetChanged();
            } else{
                // Handle errors here
                Log.w(TAG, "Error getting reports", task.getException());
            }
        });
    }
}

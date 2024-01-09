package com.example.eduhub;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.adapter.admin_AdapterReportedNotes;
import com.example.eduhub.databinding.FragmentAdminReportBinding;
import com.example.eduhub.model.ReportedNotes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class admin_reportFragment extends Fragment {
    private FragmentAdminReportBinding binding;
    private FirebaseAuth firebaseAuth;
    private ArrayList<ReportedNotes> reportedNotesArrayList;
    private admin_AdapterReportedNotes adapterReport;
    private final String TAG = "ADMIN_REPORTS_TAG";

    RecyclerView reportRv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = FragmentAdminReportBinding.inflate(getLayoutInflater(), container, false);
        View view = binding.getRoot();
        //Init Firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //Load reported notes
        retrieveReportedNotesFromFirebase();

        // Initialize RecyclerView and Adapter
        reportRv = binding.reportsRv;
        reportRv.setLayoutManager(new LinearLayoutManager(getContext()));
        reportedNotesArrayList = new ArrayList<>();
        adapterReport = new admin_AdapterReportedNotes(getContext(), reportedNotesArrayList);
        reportRv.setAdapter(adapterReport);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void retrieveReportedNotesFromFirebase() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference reportRef = db.collection("report");
        reportRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            // Process reports data
            Map<String, Integer> totalReportCounts = new HashMap<>();
            Map<String, Integer> pendingReportCounts = new HashMap<>();
            for (DocumentSnapshot reportSnapshot : queryDocumentSnapshots) {
                String resourceId = Objects.requireNonNull(reportSnapshot.getDocumentReference("resource_id")).getId();
                //noinspection DataFlowIssue
                int count = totalReportCounts.getOrDefault(resourceId, 0) + 1;
                totalReportCounts.put(resourceId, count);

                if (Boolean.FALSE.equals(reportSnapshot.getBoolean("is_solved"))){
                    //noinspection DataFlowIssue
                    int p_count = pendingReportCounts.getOrDefault(resourceId, 0) + 1;
                    pendingReportCounts.put(resourceId, p_count);
                }
            }

            for (String key : pendingReportCounts.keySet()) {
                int totalCount = totalReportCounts.get(key) != null ? totalReportCounts.get(key) : 0;
                int pendingCount = pendingReportCounts.get(key) != null ? pendingReportCounts.get(key) : 0;

                ReportedNotes notes = new ReportedNotes(key, totalCount, pendingCount);
                reportedNotesArrayList.add(notes);
            }
            adapterReport.setReportNotesArrayList(reportedNotesArrayList);
            adapterReport.notifyDataSetChanged();
        });
    }
}
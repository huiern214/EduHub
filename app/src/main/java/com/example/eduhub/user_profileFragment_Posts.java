package com.example.eduhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.databinding.FragmentUserProfileFavouriteBinding;
import com.example.eduhub.databinding.FragmentUserProfilePostsBinding;
import com.example.eduhub.model.Notes;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class user_profileFragment_Posts extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FragmentUserProfilePostsBinding binding;
    private RecyclerView recyclerViewNote;
    private user_AdapterNote noteAdapter;
    private ArrayList<Notes> noteList;

    public user_profileFragment_Posts() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserProfilePostsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerViewNote = binding.postsNotes;
        recyclerViewNote.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));
        noteList = new ArrayList<>();
        noteAdapter = new user_AdapterNote(getContext(), noteList);
        recyclerViewNote.setAdapter(noteAdapter);

        // Fetch and display favorite notes
        fetchPostsNotes();

        return view;
    }

    private void fetchPostsNotes() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();
        DocumentReference userRef = firestore.collection("user").document(userId);

        if (user != null) {
            // Create a reference to the "resource" collection
            CollectionReference resourceRef = firestore.collection("resource");

            // Query the "resource" collection where "user_id" matches the user's ID
            resourceRef.whereEqualTo("user_id", userRef).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                String notesId = documentSnapshot.getId();
                                String categoryId = Objects.requireNonNull(documentSnapshot.getDocumentReference("category_id")).getId();
                                String description = documentSnapshot.getString("resource_description");
                                String fileUrl = documentSnapshot.getString("resource_file");
                                int likes = documentSnapshot.getLong("resource_likes").intValue();
                                String resourceName = documentSnapshot.getString("resource_name");
                                Timestamp timestamp = documentSnapshot.getTimestamp("resource_upload_datetime");

                                // Create a Note object
                                Notes note = new Notes(notesId, categoryId, description, fileUrl, likes, resourceName, timestamp, userId);

                                // Add the Note to the list
                                noteList.add(note);
                            }
                        }

                        // Notify the adapter of the data change
                        noteAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        // Handle the failure to fetch the list of resource documents
                    });
        }
    }

}

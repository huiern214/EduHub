package com.example.eduhub;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.databinding.FragmentUserProfileFavouriteBinding;
import com.example.eduhub.model.Notes;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class user_profileFragment_Favourite extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FragmentUserProfileFavouriteBinding binding;
    private RecyclerView recyclerViewNote;
    private user_AdapterNote noteAdapter;
    private ArrayList<Notes> noteList;

    public user_profileFragment_Favourite() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentUserProfileFavouriteBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        recyclerViewNote = binding.favouriteNotes;
        recyclerViewNote.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));
        noteList = new ArrayList<>();
        noteAdapter = new user_AdapterNote(getContext(), noteList);
        recyclerViewNote.setAdapter(noteAdapter);

        // Fetch and display favorite notes
        fetchFavoriteNotes();

        return view;
    }

    private void fetchFavoriteNotes() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        String userId = user.getUid();

        if (user != null) {
            // Fetch the list of favorite note references
            firestore.collection("user").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<DocumentReference> favoriteNotesRefs = (List<DocumentReference>) documentSnapshot.get("favourite_notes");

                            // Iterate through the favorite note references
                            if (favoriteNotesRefs != null){
                                for (DocumentReference favoriteNoteRef : favoriteNotesRefs) {
                                    favoriteNoteRef.get().addOnSuccessListener(documentSnapshot1 -> {
                                        if (documentSnapshot1.exists()) {
                                            // Get the note data and create a Note object
                                            String notesId = documentSnapshot1.getId();
                                            String categoryId = Objects.requireNonNull(documentSnapshot1.getDocumentReference("category_id")).getId();
                                            String description = documentSnapshot1.getString("resource_description");
                                            String fileUrl = documentSnapshot1.getString("resource_file");
                                            int likes = documentSnapshot1.getLong("resource_likes").intValue();
                                            String resourceName = documentSnapshot1.getString("resource_name");
                                            Timestamp timestamp = documentSnapshot1.getTimestamp("resource_upload_datetime");

                                            // Create a Note object
                                            Notes note = new Notes(notesId, categoryId, description, fileUrl, likes, resourceName, timestamp, userId);

                                            // Add the Note to the list
                                            noteList.add(note);
                                        }

                                        // Notify the adapter of the data change
                                        noteAdapter.notifyDataSetChanged();

                                    }).addOnFailureListener(e -> {
                                        // Handle the failure to fetch the list of favorite note references
                                    });
                                }
                            }
                        }
                    }).addOnFailureListener(e -> {
                        // Handle the failure to fetch the list of favorite note references
                    });
        }
    }
}

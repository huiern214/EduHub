package com.example.eduhub;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.databinding.FragmentUserSearchNoteBinding;
import com.example.eduhub.model.Notes;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class user_search_note extends Fragment {
    private FragmentUserSearchNoteBinding binding;
    private RecyclerView searchNotesRv;
    private ArrayList<Notes> noteList;
    private user_AdapterNote noteAdapter;
    EditText searchEt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentUserSearchNoteBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize RecyclerView and noteAdapter
        searchNotesRv = binding.searchNotesRv;
        searchNotesRv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        noteList = new ArrayList<>();
        noteAdapter = new user_AdapterNote(getContext(), noteList);
        searchNotesRv.setAdapter(noteAdapter);
        searchEt = binding.searchEt;

        // Load Notes
        loadNotes();

        // Edit text change listener, search
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    //Assuming you have an instance of user_AdapterNote named adapterNote
                    noteAdapter.getFilter().filter(s);
                } catch (Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void loadNotes() {
        // Initialize ArrayList
        noteList = new ArrayList<>();

        //Get reference to the "Resource" collection in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notesRef = db.collection("resource");

        notesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                //Clear ArrayList before adding data into it
                noteList.clear();

                for (QueryDocumentSnapshot document : task.getResult()){
                    //Get data
                    long resource_likes_long = document.getLong("resource_likes");
                    int resource_likes = (int) resource_likes_long;
                    Notes note = new Notes(document.getId(),
                            Objects.requireNonNull(document.getDocumentReference("category_id")).getId(),
                            document.getString("resource_description"),
                            document.getString("resource_file"), resource_likes,
                            document.getString("resource_name"),
                            document.getTimestamp("resource_upload_datetime"),
                            Objects.requireNonNull(document.getDocumentReference("user_id")).getId());
//                            Notes note = document.toObject(Notes.class);
                    noteList.add(note);
                }
                //Setup adapter
                noteAdapter = new user_AdapterNote(getContext(),noteList);
                //Set adapter to recycler view
                binding.searchNotesRv.setAdapter(noteAdapter);
            }else{
                //handle errors here
                Log.w("Error loading notes",task.getException());
            }
        });
    }
}

package com.example.eduhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.databinding.ActivityLoginBinding;
import com.example.eduhub.databinding.ActivityUserSearchBinding;
import com.example.eduhub.model.Notes;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class user_search extends AppCompatActivity {
    ImageButton backBtn;
    private ActivityUserSearchBinding binding;
    private RecyclerView searchNotesRv;
    private ArrayList<Notes> noteList;
    private user_AdapterNote noteAdapter;
    EditText searchEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initialize buttons and editText
        backBtn = binding.backBtn;

        //Back function
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(user_search.this, user_DashboardActivity.class));
            }
        });

        // Initialize recycler view and note adapter
        searchNotesRv = binding.searchNotesRv;
        searchNotesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        noteList = new ArrayList<>();
        noteAdapter = new user_AdapterNote(this, noteList);
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
                            Objects.requireNonNull(document.getDocumentReference("user_id")).getId(),
                            document.getBoolean("is_deleted")
                    );

                    if (!note.getIs_deleted()){
                        noteList.add(note);
                    }
                }
                //Setup adapter
                noteAdapter = new user_AdapterNote(this, noteList);
                //Set adapter to recycler view
                binding.searchNotesRv.setAdapter(noteAdapter);
            }else{
                //handle errors here
                Log.w("Error loading notes",task.getException());
            }
        });
    }
}
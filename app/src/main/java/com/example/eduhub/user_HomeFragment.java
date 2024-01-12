package com.example.eduhub;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.adapter.user_AdapterHomeFragmentCategory;
import com.example.eduhub.databinding.FragmentHomeBinding;
import com.example.eduhub.model.Category;
import com.example.eduhub.model.Notes;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class user_HomeFragment extends Fragment implements CategoryClickListener{
    String[] item = {"All", "Most downloaded", "Most views", "Most likes"};
    AutoCompleteTextView autoCompleteTextView;
    TextInputLayout selectionFilterTv;

    private FragmentHomeBinding binding;
    private RecyclerView recyclerViewCategory, recyclerViewNote;
    private user_AdapterHomeFragmentCategory categoryAdapter;
    private List<Category> categoryList;

    private ArrayList<Notes> noteList;
    private ArrayList<Notes> FilteredNoteList;
    private user_AdapterNote noteAdapter;
    private ImageButton selectAllCategoryBtn;
    private TextView eventTitle, eventTime;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //Initialize the Task
        eventTitle = view.findViewById(R.id.event);
        eventTime = view.findViewById(R.id.eventTime);


        //Initialize RecyclerView and CategoryAdapter
        recyclerViewCategory = binding.CategoryRecyclerView;
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        categoryList = new ArrayList<>();
        categoryAdapter = new user_AdapterHomeFragmentCategory(getContext(),categoryList);
        categoryAdapter.setCategoryClickListener(this);  //Set the click listener for the adapter
        recyclerViewCategory.setAdapter(categoryAdapter);

        //Initialize RecyclerView and NoteAdapter
        recyclerViewNote = binding.notesRv;
        recyclerViewNote.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));
        noteList = new ArrayList<>();
        noteAdapter = new user_AdapterNote(getContext(), noteList);
        recyclerViewNote.setAdapter(noteAdapter);

        //Retrieve category data from Firebase
        retrieveCategoriesFromFirestore();
        retrieveNotesFromFirestore();
        
        return view;
    }

    @SuppressLint({"NotifyDataSetChanged", "RestrictedApi"})
    private void retrieveNotesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notesRef = db.collection("resource");

        notesRef.whereEqualTo("resource_type", "notes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        noteList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
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
                        noteAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors here
                        Log.w(TAG, "Error getting notes", task.getException());
                    }
                });
    }

    @SuppressLint({"RestrictedApi", "NotifyDataSetChanged"})
    private void retrieveCategoriesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference categoriesRef = db.collection("category");

        categoriesRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoryList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String category_id = document.getId(); // Get the document ID
                            String category_name = document.getString("category_name");

                            // Create your Category object with the document ID
                            Category category = new Category(category_id, category_name);

                            categoryList.add(category);
                        }
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors here
                        Log.w(TAG, "Error getting categories", task.getException());
                    }
                });
    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // Drop down menu
        selectionFilterTv = view.findViewById(R.id.selectionTv);
        autoCompleteTextView = view.findViewById(R.id.filter);
        ArrayAdapter<String> adapterItem = new ArrayAdapter<>(requireContext(), R.layout.filter_list, item);
        autoCompleteTextView.setAdapter(adapterItem);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                //display the selected item in the box
                Objects.requireNonNull(selectionFilterTv.getEditText()).setText(selectedItem);
                Toast.makeText(requireContext(), "Item: " + selectedItem, Toast.LENGTH_SHORT).show();
                // Handle the item selection as needed
            }
        });

        //Add an OnTouchListener to the AutoCompleteTextView
        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Check if the drop down arrow is clicked
                if (event.getAction()== MotionEvent.ACTION_UP){
                    if (event.getRawX() >= (autoCompleteTextView.getRight() - autoCompleteTextView.getCompoundDrawables()[2].getBounds().width())){
                        //Clear the selection
                        selectionFilterTv.getEditText().setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @SuppressLint({"RestrictedApi", "NotifyDataSetChanged"})
    @Override
    public void onCategoryClick(String category_id) {
        // Initialize the list before adding data
        FilteredNoteList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference notesRef = db.collection("resource");

        // Create a DocumentReference to the category document
        DocumentReference categoryRef = db.collection("category").document(category_id);

        notesRef.whereEqualTo("category_id", categoryRef)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
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
                                FilteredNoteList.add(note);
                            }
                        }
                        // Set up the adapter and notify data changes
                        noteAdapter.setNoteList(FilteredNoteList);
                        noteAdapter.notifyDataSetChanged();
                    } else {
                        // Handle errors here
                        Log.w(TAG, "Error getting notes", task.getException());
                    }
                });
    }

    // Define an interface to handle the result of the asynchronous check
    public interface CheckResultListener {
        void onCheckResult(boolean result);
    }
}
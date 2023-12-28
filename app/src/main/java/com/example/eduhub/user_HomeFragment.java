package com.example.eduhub;

import static androidx.fragment.app.FragmentManager.TAG;

import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Toast;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.databinding.FragmentHomeBinding;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class user_HomeFragment extends Fragment implements CategoryClickListener{
    String[] item = {"All", "Most downloaded", "Most views", "Most likes"};
    AutoCompleteTextView autoCompleteTextView;
    TextInputLayout selectionFilterTv;

    private FragmentHomeBinding binding;
    private RecyclerView recyclerViewCategory, recyclerViewNote;
    private user_homeFragmentCategoryAdapter categoryAdapter;
    private List<user_ModelCategory> categoryList;

    private ArrayList<user_modelPdf> noteList, FilteredNoteList, AllList;
    private user_AdapterNote noteAdapter;
    private ImageButton selectAllCategoryBtn;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        //Initialize RecyclerView and CategoryAdapter
        recyclerViewCategory = binding.CategoryRecyclerView;
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        categoryList = new ArrayList<>();
        categoryAdapter = new user_homeFragmentCategoryAdapter(getContext(),categoryList);
        categoryAdapter.setCategoryClickListener(this);  //Set the click listener for the adapter
        recyclerViewCategory.setAdapter(categoryAdapter);

        //Initialize RecyclerView and NoteAdapter
        recyclerViewNote = binding.notesRv;
        recyclerViewNote.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL, false));
        noteList = new ArrayList<>();
        noteAdapter = new user_AdapterNote(getContext(), noteList);
        recyclerViewNote.setAdapter(noteAdapter);

        //Retrieve category data from Firebase 
        retrieveCategoriesFromFirebase();
        retrieveNotesFromFirebase();
        
        return view;
    }

    private void retrieveNotesFromFirebase() {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("Notes");
        notesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noteList.clear();
                for (DataSnapshot notesSnapshot : snapshot.getChildren()){
                    user_modelPdf notes = notesSnapshot.getValue(user_modelPdf.class);
                    if (notes != null){
                        noteList.add(notes);
                        //Log.d(TAG, "Category: " + category.getCategory());
                    }
                }
                noteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
            }
        });
    }

    private void retrieveCategoriesFromFirebase() {
        DatabaseReference categoriesRef = FirebaseDatabase.getInstance().getReference("Categories");
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()){
                    user_ModelCategory category = categorySnapshot.getValue(user_ModelCategory.class);
                    if (category!=null){
                        categoryList.add(category);
                        //Log.d(TAG, "Category: " + category.getCategory());
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle onCancelled if needed
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
                selectionFilterTv.getEditText().setText(selectedItem);
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

    @Override
    public void onCategoryClick(String category) {
        //Toast.makeText(requireContext(), "Clicked category: "+category, Toast.LENGTH_SHORT).show();
        //init list before adding data
        FilteredNoteList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Notes");
        ref.orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        FilteredNoteList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            //get data
                            user_modelPdf note = ds.getValue(user_modelPdf.class);
                            //add to list
                            FilteredNoteList.add(note);
                        }
                        //setup adapter
                        noteAdapter.setNoteList(FilteredNoteList);
                        noteAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}
package com.example.eduhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.eduhub.adapter.user_AdapterNote;
import com.example.eduhub.databinding.ActivityLoginBinding;
import com.example.eduhub.databinding.ActivityUserSearchBinding;

public class user_search extends AppCompatActivity {
    Button allBtn, noteBtn, shortsBtn;
    ImageButton backBtn;
    private ActivityUserSearchBinding binding;
    private user_AdapterNote adapterNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Initialize buttons and editText
        allBtn = binding.allBtn;
        noteBtn = binding.notesBtn;
        shortsBtn = binding.eduShortsBtn;
        backBtn = binding.backBtn;

        //Back function
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(user_search.this, user_DashboardActivity.class));
            }
        });

        loadFragment(new user_search_all());

        //All resources
        allBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new user_search_all());
            }
        });

        //Note
        noteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new user_search_note());
            }
        });

        //Shorts
        shortsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new user_search_edushorts());
            }
        });

    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_search_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
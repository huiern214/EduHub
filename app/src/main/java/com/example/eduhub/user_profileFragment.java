package com.example.eduhub;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class user_profileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button btnFragmentNotes = view.findViewById(R.id.btnFragmentPosts);
        Button btnFragmentVideos = view.findViewById(R.id.btnFragmentFavourite);

        btnFragmentNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 1
                loadFragment(new user_profileFragment_Posts());
            }
        });

        btnFragmentVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 2
                loadFragment(new user_profileFragment_Favourite());
            }
        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // Add transaction to the back stack
        transaction.commit();
    }
}

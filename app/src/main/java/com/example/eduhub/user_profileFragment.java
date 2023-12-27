package com.example.eduhub;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link user_profileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class user_profileFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Button btnFragmentNotes = view.findViewById(R.id.btnFragmentNotes);
        Button btnFragmentVideos = view.findViewById(R.id.btnFragmentVideos);
        Button btnFragmentHistory = view.findViewById(R.id.btnFragmentHistory);

        btnFragmentNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 1
                loadFragment(new user_profileFragment_Notes());
            }
        });

        btnFragmentVideos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 2
                loadFragment(new user_profileFragment_Videos());
            }
        });

        btnFragmentHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 3
                loadFragment(new user_profileFragment_History());
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

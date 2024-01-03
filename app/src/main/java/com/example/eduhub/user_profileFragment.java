package com.example.eduhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class user_profileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private TextView usernameTextView, emailTextView, accountTextView, joinedTextView, postsTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize TextViews
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        accountTextView = view.findViewById(R.id.accountTextView);
        joinedTextView = view.findViewById(R.id.joinedTextView);
        postsTextView = view.findViewById(R.id.postsTextView);

        // Load user profile data
        loadUserProfile();

        // Fragment
        Button btnFragmentPosts = view.findViewById(R.id.btnFragmentPosts);
        Button btnFragmentFavourite = view.findViewById(R.id.btnFragmentFavourite);
        Button btnFragmentLikes = view.findViewById(R.id.btnFragmentLike);

        btnFragmentPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 1
                loadFragment(new user_profileFragment_Posts());
            }
        });

        btnFragmentFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 2
                loadFragment(new user_profileFragment_Favourite());
            }
        });

        btnFragmentLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Load Fragment 3
                loadFragment(new user_profileFragment_Like());
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

    private void loadUserProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Get user data from Firestore
            DocumentReference userRef = firestore.collection("user").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String username = documentSnapshot.getString("user_name");
                    String email = documentSnapshot.getString("user_email");
                    String userType = documentSnapshot.getString("user_type");
                    long creationTime = user.getMetadata().getCreationTimestamp();

                    // Set username and email
                    usernameTextView.setText(username);
                    emailTextView.setText(email);

                    // Set account type based on userType
                    if ("user".equals(userType)) {
                        accountTextView.setText("User");
                    } else if ("admin".equals(userType)) {
                        accountTextView.setText("Admin");
                    }

                    // Set joined date
                    joinedTextView.setText(MyApplication.formatTimestamp(creationTime));

                    // Count user's posts
                    countUserPosts(user.getUid());
                }
            });
        }
    }

    private void countUserPosts(String userId) {
        CollectionReference resourceRef = firestore.collection("resource");
        DocumentReference userRef = firestore.collection("user").document(userId);
        resourceRef.whereEqualTo("user_id", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int postCount = queryDocumentSnapshots.size();
                    postsTextView.setText(String.valueOf(postCount));
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }
}

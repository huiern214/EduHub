package com.example.eduhub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class user_profileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private TextView usernameTextView, emailTextView, accountTextView, joinedTextView, postsTextView;
    private Button btnFragmentPosts, btnFragmentFavourite, btnFragmentLikes;
    private LinearLayout btnLinearLayout;
    private ShapeableImageView profilePicIv;
    private String role = "user";

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
        profilePicIv = view.findViewById(R.id.profile_pic);
        btnLinearLayout = view.findViewById(R.id.buttonLinearLayout);

        // Fragment
        btnFragmentPosts = view.findViewById(R.id.btnFragmentPosts);
        btnFragmentFavourite = view.findViewById(R.id.btnFragmentFavourite);
        btnFragmentLikes = view.findViewById(R.id.btnFragmentLike);

        // Load user profile data
        loadUserProfile();

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
                    String profileImageUrl = documentSnapshot.getString("user_photo");

                    // Set username and email
                    usernameTextView.setText(username);
                    emailTextView.setText(email);
                    role = userType;
                    // Set account type based on userType
                    if ("user".equals(userType)) {
                        accountTextView.setText("User");
                        loadFragment(new user_profileFragment_Posts());
                    } else if ("admin".equals(userType)) {
                        accountTextView.setText("Admin");
                        btnFragmentPosts.setVisibility(View.GONE);
                        loadFragment(new user_profileFragment_Favourite());
                    }

                    // Set joined date
                    joinedTextView.setText(MyApplication.formatTimestamp(creationTime));

                    // Count user's posts
                    countUserPosts(user.getUid());

                    if (profileImageUrl != null && (profileImageUrl.startsWith("http://") || profileImageUrl.startsWith("https://"))) {
                        // HTTP/HTTPS URL: Use Glide to load the image from the web
                        Glide.with(profilePicIv.getContext())
                                .load(profileImageUrl)
                                .placeholder(R.drawable.baseline_person_2_24)
                                .error(R.drawable.baseline_person_2_24)
                                .into(profilePicIv);
                    } else if (profileImageUrl != null && profileImageUrl.startsWith("gs://")) {
                        // GS URL: Use Firebase Storage to load the image
                        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profileImageUrl);

                        // Get the download URL for the file
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImgUrl = uri.toString();

                            // Load the image using Glide
                            Glide.with(this)
                                    .load(profileImgUrl)
                                    .placeholder(R.drawable.baseline_person_2_24)
                                    .error(R.drawable.baseline_person_2_24)
                                    .into(profilePicIv);
                        }).addOnFailureListener(e -> {
                            // Handle the failure to get the download URL
                            profilePicIv.setImageResource(R.drawable.baseline_person_2_24);
                        });
                    } else {
                        // Handle unsupported URL format or null URL
                        profilePicIv.setImageResource(R.drawable.baseline_person_2_24);
                    }
                }
            });
        }
    }

    private void countUserPosts(String userId) {
        CollectionReference resourceRef = firestore.collection("resource");
        DocumentReference userRef = firestore.collection("user").document(userId);
        resourceRef.whereEqualTo("user_id", userRef).whereEqualTo("is_deleted", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int postCount = queryDocumentSnapshots.size();
                    postsTextView.setText(String.valueOf(postCount));
                })
                .addOnFailureListener(e -> {
                    // Handle error
                });
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (role.equals("admin")){
            btnLinearLayout.setWeightSum(2);
        }
    }
}
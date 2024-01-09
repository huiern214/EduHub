package com.example.eduhub;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eduhub.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class EditProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    ActivityEditProfileBinding binding;
    Dialog deleteAccountDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase instances
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        // Set initial profile data
        setInitialProfileData();

        // Handle click on profile picture button
        binding.profilePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Handle click on update button
        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the new name from the EditText
                String newName = binding.nameEt.getText().toString().trim();

                // Update user's name in Firestore
                updateUserName(newName);

                // Upload the profile picture to Firebase Storage and update the user's profile photo URL in Firestore
                // You can use the storageReference to upload the image and then update the Firestore document with the image URL
                // Example:
                if (selectedImageUri != null){
                    uploadProfilePicture(selectedImageUri);
                }
                progressDialog.dismiss();
            }
        });

        //Set an OnClickListener to handle the back button click
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, ForgotPassword.class));
            }
        });

        //pop up dialog handle
        deleteAccountDialog = new Dialog(this);
        binding.deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountDialog();
            }
        });
    }

    private void setInitialProfileData() {
        // Check if the user is authenticated
        if (currentUser != null) {
            // Fetch the user's name from Firestore and set it in the EditText
            firestore.collection("user").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("user_name");
                            binding.nameEt.setText(userName);
                            String profileImageUrl = documentSnapshot.getString("user_photo");
                            // Glide.with(this).load(profileImageUrl).into(binding.profilePicBtn);
                            if (profileImageUrl != null && (profileImageUrl.startsWith("http://") || profileImageUrl.startsWith("https://"))) {
                                // HTTP/HTTPS URL: Use Glide to load the image from the web
                                Glide.with(binding.profilePicBtn.getContext())
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.baseline_person_2_24)
                                        .error(R.drawable.baseline_person_2_24)
                                        .into(binding.profilePicBtn);
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
                                            .into(binding.profilePicBtn);
                                }).addOnFailureListener(e -> {
                                    // Handle the failure to get the download URL
                                    binding.profilePicBtn.setImageResource(R.drawable.baseline_person_2_24);
                                });
                            } else {
                                // Handle unsupported URL format or null URL
                                binding.profilePicBtn.setImageResource(R.drawable.baseline_person_2_24);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to fetch user's name
                    });
        }
    }

    private void updateUserName(String newName) {
        if (currentUser != null) {
            // Update the user's name in Firestore
            firestore.collection("user").document(currentUser.getUid())
                    .update("user_name", newName)
                    .addOnSuccessListener(aVoid -> {
                        // Name updated successfully
                        Toast.makeText(EditProfile.this, "Name updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle failure to update name
                    });
        }
    }

    private void openImagePicker(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        launchSomeActivity.launch(i);
    }

    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),

            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (data != null
                            && data.getData() != null) {
                        selectedImageUri = data.getData();
                        Glide.with(this)
                                .load(selectedImageUri)
                                .placeholder(R.drawable.baseline_person_2_24)
                                .into(binding.profilePicBtn);
                    }
                }
            });

    // Upload the selected image as the user's profile picture
    private void uploadProfilePicture(Uri selectedImageUri) {
        if (selectedImageUri != null) {
            // Generate a unique file name for the image
            String fileName = UUID.randomUUID().toString();
            StorageReference imageRef = storageReference.child("profile_images/" + fileName);

            // Upload the image to Firebase Storage
            UploadTask uploadTask = imageRef.putFile(selectedImageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully, get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Update the user's profile picture URL in Firestore
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // Replace "user_photo" with the field where you store the profile picture URL in Firestore
                        firestore.collection("user").document(user.getUid()).update("user_photo", uri.toString());
                        Toast.makeText(EditProfile.this, "Profile picture uploaded successfully.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                // Handle image upload failure
                Toast.makeText(EditProfile.this, "Profile picture upload failed.", Toast.LENGTH_SHORT).show();
            });
        } else {
            // No image selected
            Toast.makeText(EditProfile.this, "Please select an image first.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAccountDialog(){
        deleteAccountDialog.setContentView(R.layout.dialog_delete_account);
        deleteAccountDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Find views from the addCommentDialog
        ImageButton closeDeleteProfileBtn = deleteAccountDialog.findViewById(R.id.closeDeleteProfileBtn);
        Button submitDeleteProfileBtn = deleteAccountDialog.findViewById(R.id.submitDeleteProfileBtn);

        closeDeleteProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountDialog.dismiss();
            }
        });

        submitDeleteProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApplication.deleteProfile(getApplicationContext(), currentUser.getUid());
                deleteAccountDialog.dismiss();
                finish();
                startActivity(new Intent(EditProfile.this, MainActivity.class));
            }
        });


        // Show the dialog
        deleteAccountDialog.show();
    }


}

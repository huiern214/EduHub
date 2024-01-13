package com.example.eduhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eduhub.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    //view binding
    private ActivityRegisterBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = firebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go to Login page
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

        //handle click, begin register
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        //Find the back button by its ID
        ImageButton backButton = findViewById(R.id.backBtn);
        //Set an OnClickListener to handle the back button click
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackButtonClicked();
            }
        });
    }

    private void onBackButtonClicked() {
        //Implement the desired action when the back button is clicked
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
        finish();
    }

    private String name = "", email = "", password = "", cpassword = "";

    private void validateData() {
        /*Before creating an account, let's do some data validation*/

        //get data
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt1.getText().toString().trim();
        cpassword = binding.passwordEt2.getText().toString().trim();

        //validate data
        if (TextUtils.isEmpty(name)) {
            //name edit text is empty, must enter name
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //email is either not entered or email pattern is invalid, don't allow to continue in that case
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            //password edit text is empty, must enter password
            Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cpassword)) {
            //confirm password edit text is empty, must enter confirm password
            Toast.makeText(this, "Confirm your password", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cpassword)) {
            //password and confirmed password don't match, don't allow to continue in that case, both passwords must match
            Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
        } else {
            //all data is validated, begin creating an account
            createUserAccount();
        }
    }

    private void createUserAccount() {
        //show progress 
        progressDialog.setMessage("Creating account...");
        progressDialog.show();

        //create user in firebase auth 
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //account creation success, now add in firebase realtime database 
                        updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //account creating failed 
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserInfo() {
        progressDialog.setMessage("Saving user info...");

        // Get the current user UID
        String uid = firebaseAuth.getUid();

        if (uid != null) {

            // Setup data to add in Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("user").document(uid);

            Map<String, Object> userData = new HashMap<>();
            userData.put("user_email", email);
            userData.put("user_name", name);
            userData.put("user_photo", ""); // Add empty, will do later
            userData.put("user_type", "user"); // Possible values are user, admin, will make admin manually in Firestore

            // Set data in Firestore
            userRef.set(userData)
                    .addOnSuccessListener(aVoid -> {
                        // Data added to Firestore
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "Account created...", Toast.LENGTH_SHORT).show();
                        // Since the user account is created, start the user dashboard
                        startActivity(new Intent(RegisterActivity.this, user_DashboardActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        // Data failed to be added to Firestore
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, String.format("%s", e.getMessage()), Toast.LENGTH_SHORT).show();
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, "UID is null", Toast.LENGTH_SHORT).show();
        }
    }
}

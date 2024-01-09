package com.example.eduhub;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eduhub.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    //view binding
    private ActivityLoginBinding binding;

    //firebase auth
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    private String email="", password="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //handle click, go to register screen
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //handle click, begin login
        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
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

        TextView forgotPasswordTv = findViewById(R.id.forgotTv);
        //Set an OnClickListener to handle the forgot password click
        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, ForgotPassword.class);
                startActivity(i);
            }
        });
    }

    private void onBackButtonClicked() {
        //Implement the desired action when the back button is clicked
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void validateData() {
        /*Before log in, lets do some data validation*/

        //get data
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //email is either not entered or email pattern is invalid, don't allow to continue in that case
            Toast.makeText(this, "Invalid email pattern", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            //password edit text is empty, must enter password
            Toast.makeText(this,"Enter password",Toast.LENGTH_SHORT).show();
        } else{
            //data is validated, begin login
            loginUser();
        }
    }

    private void loginUser() {
        //show progress
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        //login user
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        //login success, check if user is user or admin
                        checkUser();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        //login failed
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "Incorrect login credential.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUser() {
        progressDialog.setMessage("Checking User");
        // Check if the user is a user or admin in Firestore
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("user").document(firebaseUser.getUid());
    
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                progressDialog.dismiss();
                if (documentSnapshot.exists()) {
                    String userType = documentSnapshot.getString("user_type");
                    if ("user".equals(userType)) {
                        // This is a simple user, open the user dashboard
                        startActivity(new Intent(LoginActivity.this, user_DashboardActivity.class));
                        finish();
                    } else if ("admin".equals(userType)) {
                        // This is an admin, open the admin dashboard
                        startActivity(new Intent(LoginActivity.this, admin_DashboardAdminActivity.class));
                        finish();
                    } else if ("deleted_user".equals(userType)){
                        Toast.makeText(this, "The user account has been deleted.", Toast.LENGTH_SHORT);
                        finish();
                    }
                } else {
                    // User document does not exist
                    // Handle the case accordingly
                    Toast.makeText(LoginActivity.this, "User document does not exist", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                // Handle the error here
                Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }    
}
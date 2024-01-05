package com.example.eduhub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    //firebase auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //init firebase auth
        firebaseAuth = firebaseAuth.getInstance();
        //start main screen after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run(){
                //stuck checkUser()
                checkUser();
                //startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        },3000); //3000 means 3 secs
    }

    private void checkUser() {
        // Get the current user, if logged in
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        // firebaseUser = null; // For testing
    
        if (firebaseUser == null) {
            // User not logged in, start the main screen
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish(); // Finish this activity
        } else {
            // User logged in, check user type in Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userRef = db.collection("user").document(firebaseUser.getUid());
    
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Get user type
                        String userType = document.getString("user_type");
    
                        if ("user".equals(userType)) {
                            // This is a simple user, open the user dashboard
                            startActivity(new Intent(SplashActivity.this, user_DashboardActivity.class));
                        } else if ("admin".equals(userType)) {
                            // This is an admin, open the admin dashboard
                            startActivity(new Intent(SplashActivity.this, admin_DashboardAdminActivity.class));
                        }
    
                        finish();
                    } else {
                        // User document does not exist in Firestore, handle this case
                    }
                } else {
                    // Handle errors here
                }
            });
        }
    }
    
}
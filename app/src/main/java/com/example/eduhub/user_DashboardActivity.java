package com.example.eduhub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.eduhub.MainActivity;
import com.example.eduhub.databinding.ActivityDashboardBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class user_DashboardActivity extends AppCompatActivity {
    //view binding
    private ActivityDashboardBinding binding;

    //Firebase auth
    private FirebaseAuth firebaseAuth;

    //Bottom Navigation
    BottomNavigationView bottomNavigationView;
    user_HomeFragment homeFragment = new user_HomeFragment();
    user_shortsFragment shortsFragment = new user_shortsFragment();
    user_calendarFragment calendarFragment = new user_calendarFragment();
    user_profileFragment profileFragment = new user_profileFragment();
    Toolbar toolbar;

    //Upload Materials
    Dialog uploadDialog, settingDialog;
    Button uploadNote,uploadVideo;
    Button settingBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        //pop up dialog handle
        uploadDialog = new Dialog(this);
        //Inflate the dialog layout
        uploadDialog.setContentView(R.layout.dialog_upload_materials);
        //Find views from uploadDialog
        uploadNote = uploadDialog.findViewById(R.id.uploadNotesBtn);
        uploadVideo = uploadDialog.findViewById(R.id.uploadVideoBtn);
        uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Setting dialog
//        settingDialog = new Dialog(this);
//        settingDialog.setContentView(R.layout.dialog_settings);
//        settingBtn = binding.toolbar.findViewById(R.id.setting);
//        settingBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                settingDialog.setContentView(R.layout.dialog_settings);
//                settingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//                Button privacy = settingDialog.findViewById(R.id.privacyBtn);
//                Button editProfile = settingDialog.findViewById(R.id.editProfileBtn);
//                Button logOut = settingDialog.findViewById(R.id.logoutBtn);
//
//                logOut.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        firebaseAuth.signOut();
//                        checkUser();
//                    }
//                });
//            }
//        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.shorts) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, shortsFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.upload) {
                    uploadDialog.setContentView(R.layout.dialog_upload_materials);
                    uploadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                    // Find views from uploadDialog
                    Button uploadNote = uploadDialog.findViewById(R.id.uploadNotesBtn);
                    Button uploadVideo = uploadDialog.findViewById(R.id.uploadVideoBtn);

                    uploadNote.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(user_DashboardActivity.this, user_uploadNotes.class));
                            uploadDialog.dismiss(); // Dismiss the dialog after starting the new activity
                        }
                    });

                    uploadVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Handle the click for uploading video
                            // You can open a new dialog, perform an action, or navigate to another screen
                            // For example, you can show a Toast message for demonstration purposes
                            Toast.makeText(user_DashboardActivity.this, "Upload Video Clicked", Toast.LENGTH_SHORT).show();
                            uploadDialog.dismiss(); // Dismiss the dialog after handling the click
                        }
                    });

                    uploadDialog.show();
                    return true;
                } else if (item.getItemId() == R.id.calender) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, calendarFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.user) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, profileFragment).commit();
                    return true;
                }
                return true;
            }
        });
    }
    private void checkUser() {
        //get current user
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            //not logged in, go to main screen
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else{
            //logged in, get user info
            String email = firebaseUser.getEmail();
            //binding.subTitleTv.setText(email);
        }
    }
}

        //handle click, logout
//        binding.logoutBtn.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                firebaseAuth.signOut();
//                checkUser();
//            }
//        });


//    private void showPopupMenu(View view) {
//        PopupMenu popupMenu = new PopupMenu(this,view);
//        popupMenu.inflate(R.menu.setting_user);
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId()==R.id.privacy){
//                    return true;
//                } else if (item.getItemId()==R.id.edit_profile) {
//                    return true;
//                } else if (item.getItemId()==R.id.logout) {
////                    firebaseAuth.signOut();
////                    checkUser();
//                    return true;
//                }
//                return true;
//            }
//        });
//    }


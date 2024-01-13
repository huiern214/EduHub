package com.example.eduhub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.eduhub.databinding.ActivityDashboardBinding;

public class user_DashboardActivity extends AppCompatActivity {
    //view binding
    private ActivityDashboardBinding binding;

    //Firebase auth
    private FirebaseAuth firebaseAuth;

    //Bottom Navigation
    BottomNavigationView bottomNavigationView;
    user_HomeFragment homeFragment = new user_HomeFragment();
    user_calendarFragment calendarFragment = new user_calendarFragment();
    user_profileFragment profileFragment = new user_profileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        // Find the "Setting" menu item from the toolbar
        MenuItem settingMenuItem = binding.toolbar.getMenu().findItem(R.id.setting);
        MenuItem searchMenuItem = binding.toolbar.getMenu().findItem(R.id.search);

        // Set an onClickListener to show the popup menu when the item is clicked
        settingMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showSettingPopupMenu(binding.toolbar); // Pass the toolbar as the anchor view
                return true;
            }
        });

        //Set an onClickListener to pass to search Activity
        searchMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                startActivity(new Intent(user_DashboardActivity.this, user_search.class));
                return true;
            }
        });

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, homeFragment).commit();
                    return true;
                } else if (item.getItemId() == R.id.upload) {
                    startActivity(new Intent(user_DashboardActivity.this, user_uploadNotes.class));
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
        }
    }

    private void showSettingPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.setting_user, popupMenu.getMenu());

        // Set an item click listener for the popup menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle item clicks here
                if (item.getItemId() == R.id.privacy) {
                    // Handle Privacy item click
                } else if (item.getItemId() == R.id.edit_profile) {
                    // Handle Edit Profile item click
                    startActivity(new Intent(user_DashboardActivity.this, EditProfile.class));
                } else if (item.getItemId() == R.id.logout) {
                    // Handle Log Out item click
                    firebaseAuth.signOut();
                    checkUser();
                }
                return true;
            }
        });

        popupMenu.show();
    }
}

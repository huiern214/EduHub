package com.example.eduhub;

import static com.example.eduhub.Constants.MAX_BYTES_PDF;

import android.app.Application;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

//application runs before launcher activity
public class MyApplication extends Application {
    private static final String TAG_DOWNLOAD = "DOWNLOAD_TAG";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    //created a static method to convert timestamp to proper date format
    public static final String formatTimestamp (long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        //format timestamp to dd/MM/yyyy
        String date = DateFormat.format("dd/MM/yyyy",cal).toString();

        return date;
    }

    public static void addToFavourite (Context context, String noteId){
        //we can add only if user is logged in
        //1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't add to favourite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else{
            long timestamp = System.currentTimeMillis();
            
            //setup data to add in firebase db of current user for favourite note
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("noteId",""+noteId);
            hashMap.put("timestamp",""+timestamp);

            //save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(noteId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Added to your favourite list",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"Failed to add to favourite due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromFavourite(Context context, String noteId){
        //we can remove only if user is logged in
        //1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't remove to favourite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else{
            //remove from db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Favourites").child(noteId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Removed from your favourite list",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"Failed to removed from favourite due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}

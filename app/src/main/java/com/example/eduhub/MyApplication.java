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

    public static void addToFavouriteNote (Context context, String noteId){
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
            ref.child(firebaseAuth.getUid()).child("FavouriteNote").child(noteId)
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

    public static void removeFromFavouriteNote(Context context, String noteId){
        //we can remove only if user is logged in
        //1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't remove to favourite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else{
            //remove from db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("FavouriteNote").child(noteId)
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

    //Like function
    public static void addToLikeNote (Context context, String noteId){
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
            ref.child(firebaseAuth.getUid()).child("LikeNote").child(noteId)
                    .setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Added to your like list",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"Failed to add to like due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void removeFromLikeNote(Context context, String noteId){
        //we can remove only if user is logged in
        //1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't remove to favourite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else{
            //remove from db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("LikeNote").child(noteId)
                    .removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(context,"Removed from your like list",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"Failed to removed from like due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public static void addToReadNote(Context context, String noteId) {
        // Check if the user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            // Not logged in, can't add to read history list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
        } else {
            long timestamp = System.currentTimeMillis();

            // Setup data to add in the Firebase db of the current user for read note
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("noteId", "" + noteId);
            hashMap.put("timestamp", "" + timestamp);

            // Save to db
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            DatabaseReference userReadNotesRef = ref.child(firebaseAuth.getUid()).child("ReadNote").child(noteId);

            userReadNotesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // NoteId already exists in ReadNote, update the timestamp
                        userReadNotesRef.child("timestamp").setValue("" + timestamp)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Updated timestamp in your read history", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to update timestamp due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // NoteId not present, add a new entry
                        userReadNotesRef.setValue(hashMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Added to your read history", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "Failed to add to read history due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Failed to check ReadNote due to " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}

package com.example.eduhub;

import android.app.Application;
import android.content.Context;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
    public static final String formatTimestamp (Timestamp timestamp){
        if (timestamp != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH); // Define your desired date format
            String formattedDate = sdf.format(timestamp.toDate());
            return formattedDate;
        }
        return null;
    }
    public static void addToFavouriteNote (Context context, String noteId){
        //we can add only if user is logged in
        //1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't add to favourite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
            return;
        } 

        // Get the current user's ID
        String userId = firebaseAuth.getUid();

        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("user").document(userId);

        // Create a reference to the note in Firestore
        DocumentReference noteRef = db.collection("resource").document(noteId);

        // Add the noteId to the "like_notes" array field
        userRef.update("favourite_notes", FieldValue.arrayUnion(noteRef))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Added to your favourite list", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to add to favourite due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    public static void removeFromFavouriteNote(Context context, String noteId){
        //we can remove only if user is logged in
        //1) Check if user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null){
            //not logged in, can't remove to favourite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
            return;
        } 
        // Get the current user's ID
        String userId = firebaseAuth.getUid();

        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("user").document(userId);

        // Create a reference to the note in Firestore
        DocumentReference noteRef = db.collection("resource").document(noteId);

        // Remove the noteId from the "like_notes" array field
        userRef.update("favourite_notes", FieldValue.arrayRemove(noteRef))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Removed from your favourite list", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to remove from favourite due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    //Like function
    // Add note to liked notes
    public static void addToLikeNote(Context context, String noteId) {
        // Check if the user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            // Not logged in, can't add to favorite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String userId = firebaseAuth.getUid();

        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("user").document(userId);

        // Create a reference to the note in Firestore
        DocumentReference noteRef = db.collection("resource").document(noteId);

        // Add the noteId to the "like_notes" array field
        userRef.update("like_notes", FieldValue.arrayUnion(noteRef))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Added to your like list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to add to like due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // Count for resource_likes
        noteRef.update("resource_likes", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(context, "Added to your like list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Failed to add to like due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
    // Unlike function
    // Remove note from liked notes
    public static void removeFromLikeNote(Context context, String noteId) {
        // Check if the user is logged in
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {
            // Not logged in, can't remove from favorite list
            Toast.makeText(context, "You're not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user's ID
        String userId = firebaseAuth.getUid();

        // Get Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the user's document in Firestore
        DocumentReference userRef = db.collection("user").document(userId);

        // Create a reference to the note in Firestore
        DocumentReference noteRef = db.collection("resource").document(noteId);

        // Remove the noteId from the "like_notes" array field
        userRef.update("like_notes", FieldValue.arrayRemove(noteRef))
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(context, "Removed from your like list", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Failed to remove from like due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        // Count for resource_likes
        noteRef.update("resource_likes", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(context, "Added to your like list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Failed to add to like due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static void deleteNote(Context context, String noteId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Reference to the note document in the "resource" collection
        DocumentReference noteRef = firestore.collection("resource").document(noteId);

        // Delete the document
        noteRef.update("is_deleted", true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Successfully deleted the note
                        // You can perform any additional actions here if needed
                        Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occurred while deleting the note
                        Toast.makeText(context, "Failed to delete note: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void removeReferenceFromUserLikes(String noteId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query the "user" collection to find documents with references to the note
        firestore.collection("user")
                .whereArrayContains("like_notes", firestore.collection("resource").document(noteId))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Get the user document
                            DocumentReference userRef = firestore.collection("user").document(document.getId());

                            // Remove the reference from the "like_notes" field
                            userRef.update("like_notes", FieldValue.arrayRemove(firestore.collection("resource").document(noteId)))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Successfully removed the reference from the user's "like_notes"
                                            // You can perform any additional actions here if needed
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle any errors that occurred while updating the "like_notes" field
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occurred while querying the "user" collection
                    }
                });
    }

    public static void removeReferenceFromUserFavourite(String noteId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Query the "user" collection to find documents with references to the note
        firestore.collection("user")
                .whereArrayContains("favourite_notes", firestore.collection("resource").document(noteId))
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Get the user document
                            DocumentReference userRef = firestore.collection("user").document(document.getId());

                            // Remove the reference from the "like_notes" field
                            userRef.update("favourite_notes", FieldValue.arrayRemove(firestore.collection("resource").document(noteId)))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // Successfully removed the reference from the user's "like_notes"
                                            // You can perform any additional actions here if needed
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Handle any errors that occurred while updating the "like_notes" field
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors that occurred while querying the "user" collection
                    }
                });
    }

}
package com.example.eduhub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.eduhub.databinding.FragmentCalendarBinding;
import com.example.eduhub.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class user_calendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private static final String TAG = "ADD_TASK";
    private Dialog createTaskDialog;
    private Button uploadTaskBtn, addTaskBtn;
    private ImageButton addTaskCloseBtn, calenderPickerBtn, timePickerBtn;
    private EditText addTaskTitleEt, addTaskDescriptionEt, addTaskDateEt, addTaskTimeEt, addTaskEventEt;
    private String taskDate = "", taskTime ="";
    private String taskTitle, taskDesc, taskEvent;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        firebaseAuth = FirebaseAuth.getInstance();

        createTaskDialog = new Dialog(requireContext());
        createTaskDialog.setContentView(R.layout.dialog_create_task);
        createTaskDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        uploadTaskBtn = createTaskDialog.findViewById(R.id.uploadTaskBtn);
        addTaskCloseBtn = createTaskDialog.findViewById(R.id.closeBtn);
        addTaskTitleEt = createTaskDialog.findViewById(R.id.addTaskTitleEt);
        addTaskDescriptionEt = createTaskDialog.findViewById(R.id.addTaskDescriptionEt);
        addTaskDateEt = createTaskDialog.findViewById(R.id.addTaskDateEt);
        addTaskTimeEt = createTaskDialog.findViewById(R.id.addTaskTimeEt);
        addTaskEventEt = createTaskDialog.findViewById(R.id.addTaskEventEt);
        timePickerBtn = createTaskDialog.findViewById(R.id.timePickerBtn);
        calenderPickerBtn = createTaskDialog.findViewById(R.id.calenderPickerBtn);

        addTaskBtn = view.findViewById(R.id.addTaskBtn);
        addTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Make sure to show the dialog when the button is clicked
                createTaskDialog.show();
                addTaskCloseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createTaskDialog.dismiss();
                    }
                });

                //Set the date
                calenderPickerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                taskDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year);
                                addTaskDateEt.setText(taskDate);
                            }
                        }, 2024, 0, 15);
                        datePickerDialog.show();
                    }
                });

                //Set the time
                timePickerBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Initialize current time to 15:00
                        Calendar currentTime = Calendar.getInstance();
                        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = currentTime.get(Calendar.MINUTE);

                        // Create TimePickerDialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                requireContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        // Handle the selected time
                                        taskTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                                        addTaskTimeEt.setText(taskTime);
                                    }
                                },
                                currentHour, // Initial hour
                                currentMinute, // Initial minute
                                true // 24-hour format
                        );
                        // Show the TimePickerDialog
                        timePickerDialog.show();
                    }
                });

                // Set click listener for the button inside the dialog
                uploadTaskBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        createTaskDialog.dismiss();
                        validateData();
                    }
                });
            }
        });
        return view;
    }

    private void validateData() {
        //Step 1: Validate data
        Log.d(TAG, "validateData: validating data...");

        taskTitle = addTaskTitleEt.getText().toString();
        taskDesc = addTaskDescriptionEt.getText().toString();
        taskEvent = addTaskEventEt.getText().toString();

        if (TextUtils.isEmpty(taskTitle)) {
            Log.d(TAG, "Task title is empty");
            Toast.makeText(requireContext(), "Enter task title", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(taskDesc)) {
            Log.d(TAG, "Task description is empty");
            Toast.makeText(requireContext(), "Enter task description", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(taskEvent)) {
            Log.d(TAG, "Task event is empty");
            Toast.makeText(requireContext(), "Enter task event", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(taskDate)) {
            Log.d(TAG, "Task date is empty");
            Toast.makeText(requireContext(), "Enter task date", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(taskTime)) {
            // Print log for debugging
            Log.d(TAG, "Task time is empty");
            Toast.makeText(requireContext(), "Enter task time", Toast.LENGTH_SHORT).show();
        } else {
            // Print log for debugging
            Log.d(TAG, "All data is valid. Uploading task info to the database.");
            uploadTaskInfoToDb();
        }
    }

    private void uploadTaskInfoToDb() {
        // Step 2: Upload task info to Firestore
        Log.d(TAG, "uploadTaskInfoToDb: Uploading task info to Firestore...");

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading task info");
        progressDialog.show();

        String uid = firebaseAuth.getUid();

        //Create document reference for user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(uid);

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("task_title", taskTitle);
        taskData.put("task_description", taskDesc);
        taskData.put("task_event", taskEvent);
        taskData.put("task_date", taskDate);
        taskData.put("task_time", taskTime);
        taskData.put("task_user", userRef);

        CollectionReference taskRef = db.collection("task");

        taskRef.add(taskData)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    // Document was successfully added, and documentReference now contains the document ID
                    String taskId = documentReference.getId();
                    Log.d(TAG, "uploadTaskInfoToDb: Successfully uploaded. Task ID: " + taskId);
                    Toast.makeText(requireContext(), "Task successfully uploaded", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "uploadTaskInfoToDb: Failed to upload to Firestore due to " + e.getMessage());
                    Toast.makeText(requireContext(), "Failed to upload task to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

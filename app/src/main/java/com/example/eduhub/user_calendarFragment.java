package com.example.eduhub;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import com.example.eduhub.adapter.admin_AdapterReportedNotes;
import com.example.eduhub.adapter.user_AdapterTask;
import com.example.eduhub.databinding.FragmentCalendarBinding;
import com.example.eduhub.model.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class user_calendarFragment extends Fragment {
    private FragmentCalendarBinding binding;
    private FirebaseFirestore firestore;
    private static final String TAG = "ADD_TASK";
    private Dialog createTaskDialog;
    private Button uploadTaskBtn, addTaskBtn;
    private ImageButton addTaskCloseBtn, calenderPickerBtn, timePickerBtn;
    private EditText addTaskTitleEt, addTaskDescriptionEt, addTaskDateEt, addTaskTimeEt, addTaskEventEt;
    private String taskDate = "", taskTime ="";
    private String taskTitle, taskDesc, taskEvent;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private ArrayList<Task> taskList;
    private user_AdapterTask adapterTask;
    private String user_id;
    private RecyclerView taskRv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        firebaseAuth = FirebaseAuth.getInstance();

        user_id = firebaseAuth.getCurrentUser().getUid();
        taskRv = view.findViewById(R.id.tasksRv);
        taskRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskList = new ArrayList<>();
        adapterTask = new user_AdapterTask(getContext(), taskList);
        taskRv.setAdapter(adapterTask);
        retrieveTaskFromFirestore();

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
                        //Get current date
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                taskDate = String.valueOf(dayOfMonth) + "/" + String.valueOf(month + 1) + "/" + String.valueOf(year);
                                addTaskDateEt.setText(taskDate);
                            }
                        }, year, month, dayOfMonth);
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

    private void retrieveTaskFromFirestore() {
        // Initialize the ArrayList before adding data into it
        taskList = new ArrayList<>();

        // Get a reference to the Firestore collection "user"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDoc = db.collection("user").document(user_id);
        CollectionReference tasksRef = userDoc.collection("tasks");

        tasksRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Error loading tasks: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Clear the ArrayList before adding data into it
            taskList.clear();

            if (value != null) {
                for (DocumentSnapshot document : value.getDocuments()) {
                    // Log the data to check if it's correct
                    Log.d("TaskData", document.getData().toString());

                    // Create a model class to represent the task data
                    Task taskSet = new Task(document.getId(),
                            document.getString("task_date"),
                            document.getString("task_description"),
                            document.getString("task_event"),
                            document.getString("task_time"),
                            document.getString("task_title"),
                            user_id,
                            document.getString("task_status"));
                    taskList.add(taskSet);
                }

                // Sort the taskList based on date and time
                Collections.sort(taskList, new Comparator<Task>() {
                    @Override
                    public int compare(Task task1, Task task2) {
                        // Compare dates first
                        int dateComparison = task1.getTask_date().compareTo(task2.getTask_date());
                        if (dateComparison != 0) {
                            return dateComparison;
                        }

                        // If dates are equal, compare times
                        return task1.getTask_time().compareTo(task2.getTask_time());
                    }
                });

                // Notify the adapter that the data has changed
                adapterTask.setTaskArrayList(taskList);
                adapterTask.notifyDataSetChanged();
            }
        });
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
        Timestamp timestamp = Timestamp.now();

        // Get a reference to the Firestore collection "user"
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("user").document(uid);

        // Get a reference to the "tasks" subcollection within the document
        CollectionReference taskRef = userRef.collection("tasks");

        // Create new task document with an auto-generated ID
        DocumentReference newTaskRef = taskRef.document();

        // Setup data to add in task document
        Map<String, Object> taskData = new HashMap<>();
        taskData.put("task_title", taskTitle);
        taskData.put("task_description", taskDesc);
        taskData.put("task_event", taskEvent);
        taskData.put("task_date", taskDate);
        taskData.put("task_time", taskTime);
        taskData.put("task_status", "ongoing");

        // Set the data for the new task document
        newTaskRef.set(taskData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Task added successfully
                        Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                        createTaskDialog.dismiss();
                        progressDialog.dismiss();
                    } else {
                        // Failed to add task
                        progressDialog.dismiss();
                        Toast.makeText(requireContext(), "Failed to add task due to " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
package com.example.eduhub.adapter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.R;
import com.example.eduhub.model.Task;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class user_AdapterTask extends RecyclerView.Adapter<user_AdapterTask.ViewHolder> {
    private Context context;
    public ArrayList<Task> taskList;
    private static final String TAG = "TASK_ADAPTER_TAG";
    private FirebaseAuth firebaseAuth;
    String taskId;
    private Dialog updateTaskDialog;
    private EditText updateTaskTitleEt, updateTaskDescriptionEt, updateTaskDateEt, updateTaskTimeEt, updateTaskEventEt;

    public user_AdapterTask(Context context, ArrayList<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_task, parent, false);
        firebaseAuth = firebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            //Initialize taskId when creating the view holder
            taskId = taskList.get(viewType).getTask_id();
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.taskTitle.setText(task.getTask_title());
        holder.taskDescription.setText(task.getTask_description());
        holder.taskTime.setText(task.getTask_time());
        taskId = task.getTask_id();

        loadDay(task, holder);
        loadDate(task, holder);
        loadMonth(task, holder);
        loadStatus(task, holder);
    }

    private void loadStatus(Task task, ViewHolder holder) {
        if (task.getTask_status().equals("ongoing")) {
            holder.taskStatus.setText("ONGOING");
        } else {
            holder.taskStatus.setText("COMPLETED");
        }
    }

    private void loadMonth(Task task, ViewHolder holder) {
        //Assuming taskDate is in "dd/MM/yyyy" format
        String dateString = task.getTask_date();

        //Parse the date string to a Date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return; //Handle parsing error as needed
        }

        //Format the month to get only the month part
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        String monthShort = monthFormat.format(date);

        //Set the month in ViewHolder
        holder.taskMonth.setText(monthShort);
    }

    private void loadDate(Task task, ViewHolder holder) {
        //Assuming taskDate is in "dd/MM/yyyy" format
        String dateString = task.getTask_date();

        //Parse the date string to a Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return; //Handle parsing error as needed
        }

        //Format the date to only the day part
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        String dayOfMonth = dayFormat.format(date);

        //Set the day of the month in your ViewHolder
        holder.taskDate.setText(dayOfMonth);
    }

    private void loadDay(Task task, ViewHolder holder) {
        //Assuming taskDate is in "dd/MM/yyyy" format
        String dateString = task.getTask_date();

        //Parse the date string to a Date object
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return; //Handle missing error as needed
        }
        //Format the date to get the day of the week
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        assert date != null;
        String datOfWeek = dayFormat.format(date);

        //Set the day of the week in ViewHolder
        holder.taskDay.setText(datOfWeek);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void setTaskArrayList(ArrayList<Task> taskList) {
        this.taskList = taskList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView taskTitle, taskDescription, taskDate, taskDay, taskMonth, taskTime, taskStatus;
        private ImageView optionsBtn;
        private Button uploadTaskBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskMonth = itemView.findViewById(R.id.taskMonth);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskStatus = itemView.findViewById(R.id.taskStatus);
            taskDay = itemView.findViewById(R.id.taskDay);
            taskTime = itemView.findViewById(R.id.taskTime);
            optionsBtn = itemView.findViewById(R.id.optionsBtn);
            taskStatus = itemView.findViewById(R.id.taskStatus);

            // Initialize updateTaskDialog only once
            updateTaskDialog = new Dialog(context);
            updateTaskDialog.setContentView(R.layout.dialog_update_task);
            updateTaskDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            updateTaskTitleEt = updateTaskDialog.findViewById(R.id.updateTaskTitleEt);
            updateTaskDescriptionEt = updateTaskDialog.findViewById(R.id.updateTaskDescriptionEt);
            updateTaskDateEt = updateTaskDialog.findViewById(R.id.updateTaskDateEt);
            updateTaskTimeEt = updateTaskDialog.findViewById(R.id.updateTaskTimeEt);
            updateTaskEventEt = updateTaskDialog.findViewById(R.id.updateTaskEventEt);
            uploadTaskBtn = updateTaskDialog.findViewById(R.id.updateTaskBtn);

            optionsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettingPopupMenu(v, getAdapterPosition());
                }
            });
        }
    }

    private void showSettingPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.task_user, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle item clicks here
                if (item.getItemId() == R.id.completeTask) {
                    // Toast.makeText(context, "Completed Task", Toast.LENGTH_SHORT).show();
                    // Update the status of the task to "completed" in FirebaseFirestore
                    updateTaskStatus(position, "completed");
                } else if (item.getItemId() == R.id.updateTask) {
                    // Retrieve the task at the specified position
                    Task task = taskList.get(position);

                    // Set the data in the updateTaskDialog
                    updateTaskTitleEt.setText(task.getTask_title());
                    updateTaskDescriptionEt.setText(task.getTask_description());
                    updateTaskDateEt.setText(task.getTask_date());
                    updateTaskTimeEt.setText(task.getTask_time());
                    updateTaskEventEt.setText(task.getTask_event());

                    // Set the date and time pickers (same as before)

                    // Show the updateTaskDialog for updating task details
                    updateTaskDialog.show();

                    // Close the updateTaskDialog
                    updateTaskDialog.findViewById(R.id.closeBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updateTaskDialog.dismiss();
                        }
                    });

                    // Update task in FirebaseFirestore
                    updateTaskDialog.findViewById(R.id.updateTaskBtn).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Fetch the updated values from the EditText fields
                            String updatedTitle = updateTaskTitleEt.getText().toString();
                            String updatedDescription = updateTaskDescriptionEt.getText().toString();
                            String updatedDate = updateTaskDateEt.getText().toString();
                            String updatedTime = updateTaskTimeEt.getText().toString();
                            String updatedEvent = updateTaskEventEt.getText().toString();

                            // Call the update function
                            updateTaskDetails(position, updatedTitle, updatedDescription, updatedDate, updatedTime, updatedEvent);

                            // Close the updateTaskDialog
                            updateTaskDialog.dismiss();
                        }
                    });
                } else if (item.getItemId() == R.id.deleteTask) {
                    //Toast.makeText(context, "Delete Task", Toast.LENGTH_SHORT).show();
                    // Delete the task from FirebaseFirestore
                    deleteTask(position);
                }
                return true;
            }
        });
        popupMenu.show();  // You need to show the PopupMenu
    }

    // Function to delete task from Firestore
    private void deleteTask(int position) {
        // Ensure that the position is valid
        if (position >= 0 && position < taskList.size()) {
            // Get the task at the specified position
            Task task = taskList.get(position);

            // Delete the task from FirebaseFirestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference taskRef = db.collection("user")
                    .document(task.getTask_userId())
                    .collection("tasks")
                    .document(task.getTask_id());

            taskRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Successfully deleted the task from FirebaseFirestore
                            Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();

                            // Remove the task from the local list
                            taskList.remove(position);

                            // Notify the adapter that the data has changed
                            //notifyItemRemoved(position);
                            //notifyItemRangeChanged(position, taskList.size()); // Optional: To update the remaining items
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete the task
                            Toast.makeText(context, "Failed to delete task", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateTaskDetails(int position, String updatedTitle, String updatedDescription, String updatedDate, String updatedTime, String updatedEvent) {
        if (position>=0 && position<taskList.size()){
            Task task = taskList.get(position);

            //Update the task details in the local list
            task.setTask_title(updatedTitle);
            task.setTask_description(updatedDescription);
            task.setTask_date(updatedDate);
            task.setTask_time(updatedTime);
            task.setTask_event(updatedEvent);

            //Update the task details in FirestoreFirebase
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference taskRef = db.collection("user")
                    .document(task.getTask_userId())
                    .collection("tasks")
                    .document(task.getTask_id());

            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("task_title", updatedTitle);
            updatedData.put("task_description", updatedDescription);
            updatedData.put("task_date", updatedDate);
            updatedData.put("task_time", updatedTime);
            updatedData.put("task_event", updatedEvent);

            taskRef.update(updatedData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Successfully updated the task details in FirebaseFirestore
                            Toast.makeText(context, "Task details updated", Toast.LENGTH_SHORT).show();
                            // Notify the adapter that the data has changed
                            notifyItemChanged(position);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to update the task details
                            Toast.makeText(context, "Failed to update task details", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateTaskStatus(int position, String completedStatus) {
        // Ensure that the position is valid
        if (position >= 0 && position < taskList.size()) {
            // Get the task at the specified position
            Task task = taskList.get(position);

            // Update the task status in the local list
            task.setTask_status(completedStatus);

            // Update the task status in FirebaseFirestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference taskRef = db.collection("user")
                    .document(task.getTask_userId())
                    .collection("tasks")
                    .document(task.getTask_id());

            taskRef.update("task_status", completedStatus)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Successfully updated the task status in FirebaseFirestore
                            Toast.makeText(context, "Task status updated to " + completedStatus, Toast.LENGTH_SHORT).show();
                            // Notify the adapter that the data has changed
                            notifyItemChanged(position);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to update the task status
                            Toast.makeText(context, "Failed to update task status", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
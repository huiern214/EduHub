package com.example.eduhub.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eduhub.R;
import com.example.eduhub.model.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class user_AdapterTask extends RecyclerView.Adapter<user_AdapterTask.ViewHolder> {
    private Context context;
    public ArrayList<Task> taskList;
    private static final String TAG = "TASK_ADAPTER_TAG";
    private FirebaseAuth firebaseAuth;
    String taskId;

    public user_AdapterTask(Context context, ArrayList<Task> taskList){
        this.context = context;
        this.taskList = taskList;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_task, parent, false);
        firebaseAuth = firebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser()!= null){
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

        holder.taskEvent.setText(task.getTask_title());
        holder.taskDescription.setText(task.getTask_description());
        holder.taskTime.setText(task.getTask_time());
        holder.taskStatus.setText(task.getTask_status());

        taskId = task.getTask_id();

        loadDay(task, holder);
        loadDate(task, holder);
        loadMonth(task, holder);
        loadStatus(task, holder);
    }

    private void loadStatus(Task task, ViewHolder holder) {
        // Implement your logic to load and display the status
    }

    private void loadMonth(Task task, ViewHolder holder) {
        //Assuming taskDate is in "dd/MM/yyyy" format
        String dateString = task.getTask_date();

        //Parse the date string to a Date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date;
        try{
            date = dateFormat.parse(dateString);
        } catch (ParseException e){
            e.printStackTrace();
            return; //Handle parsing error as needed
        }

        //Format the month to get only the month part
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM",Locale.getDefault());
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
        try{
            date = dateFormat.parse(dateString);
        } catch (ParseException e){
            e.printStackTrace();
            return; //Handle parsing error as needed
        }

        //Format the date to only the day part
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd",Locale.getDefault());
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
        try{
            date = dateFormat.parse(dateString);
        } catch(ParseException e){
            e.printStackTrace();
            return; //Handle missing error as needed
        }
        //Format the date to get the day of the week
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
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
        private TextView taskTitle, taskDescription, taskEvent, taskDate, taskDay, taskMonth,  taskTime, taskStatus;
        private ImageButton optionsBtn;

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

            optionsBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSettingPopupMenu(v);
                }
            });
        }
    }

    private void showSettingPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(context, view, Gravity.END);
        popupMenu.getMenuInflater().inflate(R.menu.task_user, popupMenu.getMenu());
        popupMenu.show();  // You need to show the PopupMenu

        // Set an item click listener for the popup menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()== R.id.deleteTask){
                    // Implement delete task logic
                } else if(item.getItemId()==R.id.updateTask){
                    // Implement update task logic
                } else if (item.getItemId()==R.id.completeTask) {
                    updateTaskStatus();
                }
                return true;
            }
        });
    }

    private void updateTaskStatus() {
        // Implement your logic to update the status of the task at the given position
        // You can use taskList.get(position) to get the Task object
        // Update the status in Firestore or wherever your data is stored
    }
}

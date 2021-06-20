package com.example.mylist.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylist.AddNewTask;
import com.example.mylist.MainActivity;
import com.example.mylist.Models.TaskModel;
import com.example.mylist.R;
import com.example.mylist.databinding.AddTaskLayoutBinding;
import com.example.mylist.databinding.TaskLayoutBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private List<TaskModel> taskList;
    private MainActivity activity;
    private FirebaseFirestore firestore;

    public TaskAdapter(MainActivity mainActivity , List<TaskModel> taskList){
        this.taskList = taskList;
        activity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.task_layout , parent , false);
        firestore = FirebaseFirestore.getInstance();

        return new MyViewHolder(view);

    }

    public void deleteTask(int position){
        TaskModel taskModel = taskList.get(position);
        firestore.collection("task").document(taskModel.TaskId).delete();
        taskList.remove(position);
        notifyItemRemoved(position);
    }

    public Context getContext(){
        return activity;
    }

    public void editTask(int position){
        TaskModel taskModel = taskList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task" , taskModel.getTask());
        bundle.putString("due" , taskModel.getDue());
        bundle.putString("id" , taskModel.TaskId);

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager() , addNewTask.getTag());
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        TaskModel taskModel = taskList.get(position);
        holder.mCheckBox.setText(taskModel.getTask());

        holder.tvDueDate.setText("Due On " + taskModel.getDue());

        holder.mCheckBox.setChecked(toBoolean(taskModel.getStatus()));

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    firestore.collection("task").document(taskModel.TaskId).update("status" , 1);
                }else{
                    firestore.collection("task").document(taskModel.TaskId).update("status" , 0);
                }
            }
        });

    }

    private boolean toBoolean(int status){
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tvDueDate;
        CheckBox mCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            mCheckBox = itemView.findViewById(R.id.cbTaskDone);

        }
    }
}
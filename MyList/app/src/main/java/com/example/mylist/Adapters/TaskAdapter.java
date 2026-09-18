package com.example.mylist.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylist.AddNewTask;
import com.example.mylist.MainActivity;
import com.example.mylist.Models.TaskModel;
import com.example.mylist.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private final List<TaskModel> taskList;
    private final MainActivity activity;
    private final FirebaseFirestore firestore;

    public TaskAdapter(MainActivity mainActivity, List<TaskModel> taskList) {
        this.taskList = taskList;
        this.activity = mainActivity;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(view);
    }

    public void deleteTask(int position) {
        if (position == RecyclerView.NO_POSITION || position >= taskList.size()) {
            return;
        }

        TaskModel taskModel = taskList.get(position);
        firestore.collection("task")
                .document(taskModel.TaskId)
                .delete()
                .addOnSuccessListener(unused -> {
                    int currentPosition = taskList.indexOf(taskModel);
                    if (currentPosition != -1) {
                        taskList.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                    }
                })
                .addOnFailureListener(exception -> {
                    notifyItemChanged(position);
                    Toast.makeText(activity, "Could not delete task", Toast.LENGTH_SHORT).show();
                });
    }

    public Context getContext() {
        return activity;
    }

    public void editTask(int position) {
        if (position == RecyclerView.NO_POSITION || position >= taskList.size()) {
            return;
        }

        TaskModel taskModel = taskList.get(position);

        Bundle bundle = new Bundle();
        bundle.putString("task", taskModel.getTask());
        bundle.putString("due", taskModel.getDue());
        bundle.putString("id", taskModel.TaskId);

        AddNewTask addNewTask = new AddNewTask();
        addNewTask.setArguments(bundle);
        addNewTask.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
        notifyItemChanged(position);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TaskModel taskModel = taskList.get(position);
        holder.mCheckBox.setText(taskModel.getTask());
        holder.tvDueDate.setText("Due On " + taskModel.getDue());

        holder.mCheckBox.setOnCheckedChangeListener(null);
        holder.mCheckBox.setChecked(toBoolean(taskModel.getStatus()));

        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                firestore.collection("task")
                        .document(taskModel.TaskId)
                        .update("status", isChecked ? 1 : 0)
                        .addOnFailureListener(exception -> {
                            Toast.makeText(activity, "Could not update task", Toast.LENGTH_SHORT).show();
                            int currentPosition = holder.getBindingAdapterPosition();
                            if (currentPosition != RecyclerView.NO_POSITION) {
                                notifyItemChanged(currentPosition);
                            }
                        })
        );
    }

    private boolean toBoolean(int status) {
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        final TextView tvDueDate;
        final CheckBox mCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            mCheckBox = itemView.findViewById(R.id.cbTaskDone);
        }
    }
}

package com.example.mylist.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylist.Models.TaskModel;
import com.example.mylist.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    private final List<TaskModel> taskList;
    private final Context context;
    private final TaskActionListener actionListener;

    public TaskAdapter(@NonNull Context context,
                       @NonNull List<TaskModel> taskList,
                       @NonNull TaskActionListener actionListener) {
        this.context = context;
        this.taskList = taskList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(view);
    }

    public void requestDelete(int position) {
        TaskModel task = getTask(position);
        if (task != null) {
            actionListener.onDeleteTask(task, position);
        }
    }

    public void requestEdit(int position) {
        TaskModel task = getTask(position);
        if (task != null) {
            actionListener.onEditTask(task);
            notifyItemChanged(position);
        }
    }

    public void restoreItem(int position) {
        if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
            notifyItemChanged(position);
        } else {
            notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TaskModel taskModel = taskList.get(position);

        holder.taskCheckBox.setText(taskModel.getTask());

        String dueDate = taskModel.getDue();
        holder.dueDateText.setText(
                dueDate == null || dueDate.isEmpty()
                        ? context.getString(R.string.set_due_date)
                        : context.getString(R.string.due_on_format, dueDate)
        );

        holder.taskCheckBox.setOnCheckedChangeListener(null);
        holder.taskCheckBox.setChecked(taskModel.getStatus() != 0);

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                actionListener.onTaskStatusChanged(taskModel, isChecked)
        );
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    @Nullable
    private TaskModel getTask(int position) {
        if (position == RecyclerView.NO_POSITION || position < 0 || position >= taskList.size()) {
            return null;
        }
        return taskList.get(position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        final TextView dueDateText;
        final CheckBox taskCheckBox;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            dueDateText = itemView.findViewById(R.id.tvDueDate);
            taskCheckBox = itemView.findViewById(R.id.cbTaskDone);
        }
    }

    public interface TaskActionListener {
        void onEditTask(@NonNull TaskModel task);

        void onDeleteTask(@NonNull TaskModel task, int adapterPosition);

        void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete);
    }
}

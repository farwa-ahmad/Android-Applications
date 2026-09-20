package com.farwaahmad.mylist.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.R;
import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScheduleTaskAdapter extends RecyclerView.Adapter<ScheduleTaskAdapter.TaskViewHolder> {

    private final List<TaskModel> tasks = new ArrayList<>();
    private final OnTaskStatusChangedListener listener;

    public ScheduleTaskAdapter(@NonNull OnTaskStatusChangedListener listener) {
        this.listener = listener;
    }

    public void submitTasks(@NonNull List<TaskModel> allTasks, @NonNull String selectedDate) {
        tasks.clear();
        String normalizedSelected = TaskDateUtils.normalizeForStorage(selectedDate);

        for (TaskModel task : allTasks) {
            String due = TaskDateUtils.normalizeForStorage(task.getDue());
            if (!normalizedSelected.isEmpty() && normalizedSelected.equals(due)) {
                tasks.add(task);
            }
        }

        tasks.sort(
                Comparator.comparingInt(TaskModel::getStatus)
                        .thenComparingLong(task ->
                                TaskDateUtils.sortTimestamp(task.getDue(), task.getDueTime()))
        );

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.schedule_task_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = tasks.get(position);
        boolean completed = task.getStatus() != 0;
        boolean pastDate = TaskDateUtils.isPastDate(task.getDue());

        holder.title.setText(task.getTask() == null ? "" : task.getTask());
        holder.time.setText(
                TaskDateUtils.formatTimeForDisplay(holder.itemView.getContext(), task.getDueTime())
        );

        holder.title.setTextColor(
                ContextCompat.getColor(
                        holder.itemView.getContext(),
                        !completed && pastDate ? R.color.delete_color : R.color.secondary
                )
        );
        holder.time.setTextColor(
                ContextCompat.getColor(
                        holder.itemView.getContext(),
                        !completed && pastDate ? R.color.delete_color : R.color.dark_gray
                )
        );

        holder.title.setPaintFlags(
                completed
                        ? holder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                        : holder.title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG
        );

        float alpha = completed ? 0.62f : 1f;
        holder.title.setAlpha(alpha);
        holder.time.setAlpha(alpha);
        holder.checkBox.setAlpha(1f);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(completed);
        holder.checkBox.setOnCheckedChangeListener((button, checked) -> {
            if (checked != (task.getStatus() != 0)) {
                listener.onTaskStatusChanged(task, checked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public int getTaskCount() {
        return tasks.size();
    }

    public int getOpenTaskCount() {
        int count = 0;
        for (TaskModel task : tasks) {
            if (task.getStatus() == 0) {
                count++;
            }
        }
        return count;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBox;
        final TextView title;
        final TextView time;

        TaskViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cbScheduleTaskDone);
            title = itemView.findViewById(R.id.tvScheduleTaskTitle);
            time = itemView.findViewById(R.id.tvScheduleTaskTime);
        }
    }

    public interface OnTaskStatusChangedListener {
        void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete);
    }
}

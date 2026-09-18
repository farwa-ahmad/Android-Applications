package com.farwaahmad.mylist.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.R;
import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;
    private static final int MENU_EDIT = 1;
    private static final int MENU_DELETE = 2;

    private final Context context;
    private final TaskActionListener actionListener;
    private final List<Row> rows = new ArrayList<>();

    public TaskAdapter(@NonNull Context context,
                       @NonNull TaskActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
    }

    public void submitTasks(@NonNull List<TaskModel> tasks) {
        rows.clear();

        List<TaskModel> overdue = new ArrayList<>();
        List<TaskModel> today = new ArrayList<>();
        List<TaskModel> upcoming = new ArrayList<>();
        List<TaskModel> noDueDate = new ArrayList<>();
        List<TaskModel> completed = new ArrayList<>();

        for (TaskModel task : tasks) {
            if (task.getStatus() != 0) {
                completed.add(task);
                continue;
            }

            switch (TaskDateUtils.bucketFor(task.getDue())) {
                case TaskDateUtils.BUCKET_OVERDUE:
                    overdue.add(task);
                    break;
                case TaskDateUtils.BUCKET_TODAY:
                    today.add(task);
                    break;
                case TaskDateUtils.BUCKET_UPCOMING:
                    upcoming.add(task);
                    break;
                default:
                    noDueDate.add(task);
            }
        }

        Comparator<TaskModel> byDueDate =
                Comparator.comparingLong(task -> TaskDateUtils.sortTimestamp(task.getDue()));
        overdue.sort(byDueDate);
        today.sort(byDueDate);
        upcoming.sort(byDueDate);

        addSection(R.string.section_overdue, overdue);
        addSection(R.string.section_today, today);
        addSection(R.string.section_upcoming, upcoming);
        addSection(R.string.section_no_due_date, noDueDate);
        addSection(R.string.section_completed, completed);

        notifyDataSetChanged();
    }

    private void addSection(int titleRes, @NonNull List<TaskModel> tasks) {
        if (tasks.isEmpty()) {
            return;
        }

        rows.add(Row.header(context.getString(titleRes)));
        for (TaskModel task : tasks) {
            rows.add(Row.task(task));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).task == null ? TYPE_HEADER : TYPE_TASK;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.task_section_header, parent, false);
            return new HeaderViewHolder(view);
        }

        View view = inflater.inflate(R.layout.task_layout, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText(row.header);
            return;
        }

        TaskViewHolder taskHolder = (TaskViewHolder) holder;
        TaskModel task = row.task;
        if (task == null) {
            return;
        }

        boolean completed = task.getStatus() != 0;
        taskHolder.taskCheckBox.setText(task.getTask());
        taskHolder.itemView.setAlpha(completed ? 0.62f : 1f);

        int flags = taskHolder.taskCheckBox.getPaintFlags();
        if (completed) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;
        } else {
            flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
        }
        taskHolder.taskCheckBox.setPaintFlags(flags);

        String dueDate = task.getDue();
        boolean hasDueDate = dueDate != null && !dueDate.trim().isEmpty();
        taskHolder.dueDateText.setVisibility(hasDueDate ? View.VISIBLE : View.GONE);

        if (hasDueDate) {
            String displayDate = TaskDateUtils.formatForDisplay(context, dueDate);
            if (!completed && TaskDateUtils.isOverdue(dueDate)) {
                taskHolder.dueDateText.setText(
                        context.getString(R.string.overdue_due_format, displayDate)
                );
                taskHolder.dueDateText.setTextColor(
                        ContextCompat.getColor(context, R.color.delete_color)
                );
            } else {
                taskHolder.dueDateText.setText(
                        context.getString(R.string.due_label_format, displayDate)
                );
                taskHolder.dueDateText.setTextColor(
                        ContextCompat.getColor(context, R.color.due_text)
                );
            }
        }

        taskHolder.taskCheckBox.setOnCheckedChangeListener(null);
        taskHolder.taskCheckBox.setChecked(completed);
        taskHolder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                actionListener.onTaskStatusChanged(task, isChecked)
        );

        taskHolder.itemView.setOnClickListener(v -> actionListener.onEditTask(task));
        taskHolder.moreButton.setOnClickListener(v -> showOptions(taskHolder, task));
    }

    private void showOptions(@NonNull TaskViewHolder holder, @NonNull TaskModel task) {
        PopupMenu popupMenu = new PopupMenu(context, holder.moreButton);
        popupMenu.getMenu().add(0, MENU_EDIT, 0, R.string.edit_task);
        popupMenu.getMenu().add(0, MENU_DELETE, 1, R.string.delete_task_title);
        popupMenu.setOnMenuItemClickListener(item -> {
            int position = holder.getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return false;
            }

            if (item.getItemId() == MENU_EDIT) {
                actionListener.onEditTask(task);
                return true;
            }

            if (item.getItemId() == MENU_DELETE) {
                actionListener.onDeleteTaskRequested(task, position);
                return true;
            }

            return false;
        });
        popupMenu.show();
    }

    public boolean isTaskPosition(int position) {
        return position >= 0
                && position < rows.size()
                && rows.get(position).task != null;
    }

    @Nullable
    public TaskModel getTaskAt(int position) {
        if (!isTaskPosition(position)) {
            return null;
        }
        return rows.get(position).task;
    }

    public void requestDelete(int position) {
        TaskModel task = getTaskAt(position);
        if (task != null) {
            actionListener.onDeleteTask(task, position);
        }
    }

    public void requestEdit(int position) {
        TaskModel task = getTaskAt(position);
        if (task != null) {
            actionListener.onEditTask(task);
            notifyItemChanged(position);
        }
    }

    public void restoreItem(int position) {
        if (position >= 0 && position < rows.size()) {
            notifyItemChanged(position);
        } else {
            notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static class Row {
        final String header;
        final TaskModel task;

        private Row(@Nullable String header, @Nullable TaskModel task) {
            this.header = header;
            this.task = task;
        }

        static Row header(@NonNull String header) {
            return new Row(header, null);
        }

        static Row task(@NonNull TaskModel task) {
            return new Row(null, task);
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView title;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView;
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        final TextView dueDateText;
        final CheckBox taskCheckBox;
        final ImageButton moreButton;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            dueDateText = itemView.findViewById(R.id.tvDueDate);
            taskCheckBox = itemView.findViewById(R.id.cbTaskDone);
            moreButton = itemView.findViewById(R.id.btnMore);
        }
    }

    public interface TaskActionListener {
        void onEditTask(@NonNull TaskModel task);

        void onDeleteTaskRequested(@NonNull TaskModel task, int adapterPosition);

        void onDeleteTask(@NonNull TaskModel task, int adapterPosition);

        void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete);
    }
}

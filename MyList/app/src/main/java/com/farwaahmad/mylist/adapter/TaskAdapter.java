package com.farwaahmad.mylist.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.R;
import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    private static final int SECTION_OVERDUE = 0;
    private static final int SECTION_TODAY = 1;
    private static final int SECTION_UPCOMING = 2;
    private static final int SECTION_NO_DUE_DATE = 3;
    private static final int SECTION_COMPLETED = 4;

    private static final long SWIPE_HINT_DURATION_MS = 6500L;

    private final Context context;
    private final TaskActionListener actionListener;
    private final List<Row> rows = new ArrayList<>();
    private final List<TaskModel> latestTasks = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideSwipeHintRunnable = this::hideSwipeHint;

    private String swipeHintTaskId;
    private boolean completedCollapsed = true;

    public TaskAdapter(@NonNull Context context,
                       @NonNull TaskActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
    }

    public void submitTasks(@NonNull List<TaskModel> tasks) {
        latestTasks.clear();
        latestTasks.addAll(tasks);
        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();

        List<TaskModel> overdue = new ArrayList<>();
        List<TaskModel> today = new ArrayList<>();
        List<TaskModel> upcoming = new ArrayList<>();
        List<TaskModel> noDueDate = new ArrayList<>();
        List<TaskModel> completed = new ArrayList<>();

        for (TaskModel task : latestTasks) {
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

        addSection(SECTION_OVERDUE, R.string.section_overdue, overdue, false);
        addSection(SECTION_TODAY, R.string.section_today, today, false);
        addSection(SECTION_UPCOMING, R.string.section_upcoming, upcoming, false);
        addSection(SECTION_NO_DUE_DATE, R.string.section_no_due_date, noDueDate, false);
        addSection(
                SECTION_COMPLETED,
                R.string.section_completed,
                completed,
                completedCollapsed
        );

        notifyDataSetChanged();
    }

    private void addSection(int section,
                            int titleRes,
                            @NonNull List<TaskModel> tasks,
                            boolean collapsed) {
        if (tasks.isEmpty()) {
            return;
        }

        rows.add(Row.header(context.getString(titleRes), section, tasks.size()));

        if (collapsed) {
            return;
        }

        for (int index = 0; index < tasks.size(); index++) {
            rows.add(Row.task(
                    tasks.get(index),
                    section,
                    index == 0,
                    index == tasks.size() - 1
            ));
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
            bindHeader((HeaderViewHolder) holder, row);
            return;
        }

        TaskViewHolder taskHolder = (TaskViewHolder) holder;
        TaskModel task = row.task;
        if (task == null) {
            return;
        }

        bindGroupShape(taskHolder, row);

        boolean completed = task.getStatus() != 0;
        taskHolder.taskTitle.setText(task.getTask());
        taskHolder.itemView.animate().cancel();
        taskHolder.itemView.setTranslationX(0f);
        taskHolder.itemView.setAlpha(completed ? 0.62f : 1f);

        boolean showSwipeHint = task.getId() != null
                && task.getId().equals(swipeHintTaskId);
        taskHolder.swipeHint.setVisibility(showSwipeHint ? View.VISIBLE : View.GONE);
        taskHolder.dismissSwipeHint.setOnClickListener(v -> hideSwipeHint());

        int flags = taskHolder.taskTitle.getPaintFlags();
        if (completed) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;
        } else {
            flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
        }
        taskHolder.taskTitle.setPaintFlags(flags);

        bindDueDate(taskHolder, task, row.section, completed);

        taskHolder.taskCheckBox.setOnCheckedChangeListener(null);
        taskHolder.taskCheckBox.setChecked(completed);
        taskHolder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                actionListener.onTaskStatusChanged(task, isChecked)
        );

        taskHolder.itemView.setOnClickListener(v -> actionListener.onEditTask(task));
    }

    private void bindDueDate(@NonNull TaskViewHolder holder,
                             @NonNull TaskModel task,
                             int section,
                             boolean completed) {
        String dueDate = task.getDue();
        int bucket = TaskDateUtils.bucketFor(dueDate);

        boolean hideDate = dueDate == null
                || dueDate.trim().isEmpty()
                || bucket == TaskDateUtils.BUCKET_NONE
                || bucket == TaskDateUtils.BUCKET_TODAY
                || section == SECTION_TODAY
                || section == SECTION_NO_DUE_DATE;

        if (hideDate) {
            holder.dueDateText.setVisibility(View.GONE);
            return;
        }

        String displayDate = compactDate(dueDate);
        if (displayDate.isEmpty()) {
            holder.dueDateText.setVisibility(View.GONE);
            return;
        }

        holder.dueDateText.setVisibility(View.VISIBLE);
        holder.dueDateText.setText(displayDate);
        holder.dueDateText.setTextColor(
                ContextCompat.getColor(
                        context,
                        !completed && section == SECTION_OVERDUE
                                ? R.color.delete_color
                                : R.color.dark_gray
                )
        );
    }

    @NonNull
    private String compactDate(@Nullable String dueDate) {
        if (dueDate == null || dueDate.trim().isEmpty()) {
            return "";
        }

        if (TaskDateUtils.bucketFor(dueDate) == TaskDateUtils.BUCKET_TODAY) {
            return "";
        }

        Calendar due = TaskDateUtils.calendarForDue(dueDate);
        if (due == null) {
            return dueDate;
        }

        Calendar today = Calendar.getInstance();
        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (sameDay(due, tomorrow)) {
            return context.getString(R.string.tomorrow);
        }

        return new SimpleDateFormat("d MMM", Locale.getDefault())
                .format(due.getTime());
    }

    private boolean sameDay(@NonNull Calendar first, @NonNull Calendar second) {
        return first.get(Calendar.ERA) == second.get(Calendar.ERA)
                && first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    private void bindHeader(@NonNull HeaderViewHolder holder, @NonNull Row row) {
        holder.title.setText(row.header);
        holder.count.setText(String.valueOf(row.count));

        boolean isOverdue = row.section == SECTION_OVERDUE;
        int headerColor = ContextCompat.getColor(
                context,
                isOverdue ? R.color.delete_color : R.color.secondary
        );
        holder.title.setTextColor(headerColor);
        holder.count.setTextColor(
                ContextCompat.getColor(
                        context,
                        isOverdue ? R.color.delete_color : R.color.dark_gray
                )
        );

        boolean isCompleted = row.section == SECTION_COMPLETED;
        holder.toggle.setVisibility(isCompleted ? View.VISIBLE : View.GONE);
        holder.itemView.setClickable(isCompleted);
        holder.itemView.setFocusable(isCompleted);

        if (!isCompleted) {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setContentDescription(null);
            return;
        }

        holder.toggle.setRotation(completedCollapsed ? -90f : 0f);
        holder.toggle.setContentDescription(
                context.getString(
                        completedCollapsed
                                ? R.string.expand_completed
                                : R.string.collapse_completed
                )
        );
        holder.itemView.setContentDescription(
                context.getString(
                        R.string.completed_section_accessibility,
                        row.count,
                        context.getString(
                                completedCollapsed
                                        ? R.string.expand_completed
                                        : R.string.collapse_completed
                        )
                )
        );
        holder.itemView.setOnClickListener(v -> {
            completedCollapsed = !completedCollapsed;
            rebuildRows();
        });
    }

    private void bindGroupShape(@NonNull TaskViewHolder holder, @NonNull Row row) {
        float radius = dpToPx(14);

        holder.card.setShapeAppearanceModel(
                holder.card.getShapeAppearanceModel()
                        .toBuilder()
                        .setTopLeftCornerSize(row.firstInSection ? radius : 0f)
                        .setTopRightCornerSize(row.firstInSection ? radius : 0f)
                        .setBottomLeftCornerSize(row.lastInSection ? radius : 0f)
                        .setBottomRightCornerSize(row.lastInSection ? radius : 0f)
                        .build()
        );

        holder.divider.setVisibility(row.lastInSection ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        holder.itemView.animate().cancel();
        holder.itemView.setTranslationX(0f);
        super.onViewRecycled(holder);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        handler.removeCallbacksAndMessages(null);
        swipeHintTaskId = null;

        for (int index = 0; index < recyclerView.getChildCount(); index++) {
            View child = recyclerView.getChildAt(index);
            child.animate().cancel();
            child.setTranslationX(0f);
        }

        super.onDetachedFromRecyclerView(recyclerView);
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

    public int showSwipeHint(@NonNull String taskId) {
        int position = getPositionForTaskId(taskId);
        if (position == RecyclerView.NO_POSITION) {
            return position;
        }

        handler.removeCallbacks(hideSwipeHintRunnable);
        swipeHintTaskId = taskId;
        notifyItemChanged(position);
        handler.postDelayed(hideSwipeHintRunnable, SWIPE_HINT_DURATION_MS);
        return position;
    }

    public void hideSwipeHint() {
        if (swipeHintTaskId == null) {
            return;
        }

        int position = getPositionForTaskId(swipeHintTaskId);
        swipeHintTaskId = null;
        handler.removeCallbacks(hideSwipeHintRunnable);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    public void animateSwipeHint(@NonNull RecyclerView recyclerView,
                                 @NonNull String taskId) {
        int position = getPositionForTaskId(taskId);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        RecyclerView.ViewHolder holder =
                recyclerView.findViewHolderForAdapterPosition(position);
        if (holder == null) {
            recyclerView.scrollToPosition(position);
            handler.postDelayed(() -> {
                if (recyclerView.isAttachedToWindow()) {
                    animateSwipeHint(recyclerView, taskId);
                }
            }, 180L);
            return;
        }

        float distance = dpToPx(18);
        View itemView = holder.itemView;
        itemView.animate().cancel();
        itemView.animate()
                .translationX(distance)
                .setDuration(150L)
                .withEndAction(() -> itemView.animate()
                        .translationX(-distance)
                        .setDuration(260L)
                        .withEndAction(() -> itemView.animate()
                                .translationX(0f)
                                .setDuration(150L)
                                .start())
                        .start())
                .start();
    }

    private int getPositionForTaskId(@NonNull String taskId) {
        for (int position = 0; position < rows.size(); position++) {
            TaskModel task = rows.get(position).task;
            if (task != null && taskId.equals(task.getId())) {
                return position;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    private float dpToPx(int dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private static class Row {
        @Nullable
        final String header;
        @Nullable
        final TaskModel task;
        final int section;
        final int count;
        final boolean firstInSection;
        final boolean lastInSection;

        private Row(@Nullable String header,
                    @Nullable TaskModel task,
                    int section,
                    int count,
                    boolean firstInSection,
                    boolean lastInSection) {
            this.header = header;
            this.task = task;
            this.section = section;
            this.count = count;
            this.firstInSection = firstInSection;
            this.lastInSection = lastInSection;
        }

        static Row header(@NonNull String header, int section, int count) {
            return new Row(header, null, section, count, false, false);
        }

        static Row task(@NonNull TaskModel task,
                        int section,
                        boolean firstInSection,
                        boolean lastInSection) {
            return new Row(
                    null,
                    task,
                    section,
                    0,
                    firstInSection,
                    lastInSection
            );
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView count;
        final ImageView toggle;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tvSectionTitle);
            count = itemView.findViewById(R.id.tvSectionCount);
            toggle = itemView.findViewById(R.id.ivSectionToggle);
        }
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView card;
        final TextView dueDateText;
        final TextView taskTitle;
        final CheckBox taskCheckBox;
        final View swipeHint;
        final ImageButton dismissSwipeHint;
        final View divider;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            dueDateText = itemView.findViewById(R.id.tvDueDate);
            taskTitle = itemView.findViewById(R.id.tvTaskTitle);
            taskCheckBox = itemView.findViewById(R.id.cbTaskDone);
            swipeHint = itemView.findViewById(R.id.swipeHint);
            dismissSwipeHint = itemView.findViewById(R.id.btnDismissSwipeHint);
            divider = itemView.findViewById(R.id.taskDivider);
        }
    }

    public interface TaskActionListener {
        void onEditTask(@NonNull TaskModel task);

        void onDeleteTaskRequested(@NonNull TaskModel task, int adapterPosition);

        void onDeleteTask(@NonNull TaskModel task, int adapterPosition);

        void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete);
    }
}

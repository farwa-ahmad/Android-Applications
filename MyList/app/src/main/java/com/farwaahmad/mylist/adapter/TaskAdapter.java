package com.farwaahmad.mylist.adapter;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_TASK = 1;

    private static final long SWIPE_HINT_DURATION_MS = 6500L;

    private final Context context;
    private final TaskActionListener actionListener;
    private final List<Row> rows = new ArrayList<>();
    private final List<TaskModel> latestTasks = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable hideSwipeHintRunnable = this::hideSwipeHint;

    private String swipeHintTaskId;
    private final TaskEditState editState = new TaskEditState();
    private boolean completedCollapsed = true;

    public TaskAdapter(@NonNull Context context,
                       @NonNull TaskActionListener actionListener) {
        this.context = context;
        this.actionListener = actionListener;
    }

    public void submitTasks(@NonNull List<TaskModel> tasks) {
        latestTasks.clear();
        latestTasks.addAll(tasks);

        String editingTaskId = editState.getEditingTaskId();
        if (editingTaskId != null && !containsTask(editingTaskId)) {
            editState.clear();
        }

        rebuildRows();
    }

    private boolean containsTask(@NonNull String taskId) {
        for (TaskModel task : latestTasks) {
            if (taskId.equals(task.getId())) {
                return true;
            }
        }
        return false;
    }

    private void rebuildRows() {
        rows.clear();

        for (TaskListOrganizer.Section section : TaskListOrganizer.organize(latestTasks)) {
            boolean collapsed = section.getType() == TaskListOrganizer.SectionType.COMPLETED
                    && completedCollapsed;
            addSection(section.getType(), sectionTitleRes(section.getType()), section.getTasks(), collapsed);
        }

        notifyDataSetChanged();
    }

    private int sectionTitleRes(@NonNull TaskListOrganizer.SectionType section) {
        switch (section) {
            case OVERDUE:
                return R.string.section_overdue;
            case TODAY:
                return R.string.section_today;
            case TOMORROW:
                return R.string.section_tomorrow;
            case THIS_WEEK:
                return R.string.section_this_week;
            case LATER:
                return R.string.section_later;
            case NO_DUE_DATE:
                return R.string.section_no_due_date;
            case COMPLETED:
            default:
                return R.string.section_completed;
        }
    }

    private void addSection(@NonNull TaskListOrganizer.SectionType section,
                            int titleRes,
                            @NonNull List<TaskModel> tasks,
                            boolean collapsed) {
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
        boolean editing = editState.isEditing(task.getId());

        taskHolder.itemView.animate().cancel();
        taskHolder.itemView.setTranslationX(0f);
        // Keep the card itself opaque so swipe action colors never bleed
        // through completed rows. De-emphasize only the row's content.
        taskHolder.itemView.setAlpha(1f);
        float contentAlpha = completed && !editing ? 0.62f : 1f;
        taskHolder.taskTitle.setAlpha(contentAlpha);
        taskHolder.dueDateText.setAlpha(contentAlpha);
        // Keep both circle outlines equally visible; fade only completed text.
        taskHolder.taskCheckBox.setAlpha(1f);

        bindTaskTitle(taskHolder, task, completed, editing);

        boolean showSwipeHint = !editing
                && task.getId() != null
                && task.getId().equals(swipeHintTaskId);
        taskHolder.swipeHint.setVisibility(showSwipeHint ? View.VISIBLE : View.GONE);
        taskHolder.dismissSwipeHint.setOnClickListener(v -> hideSwipeHint());

        if (editing) {
            taskHolder.dueDateText.setVisibility(View.GONE);
        } else {
            bindDueDate(taskHolder, task, row.section, completed);
        }

        taskHolder.taskCheckBox.setOnCheckedChangeListener(null);
        taskHolder.taskCheckBox.setChecked(completed);
        // Only the row being edited is locked. A save in progress must not
        // disable unrelated task checkboxes, otherwise RecyclerView can leave
        // completed tasks rendered in Android's disabled gray state.
        taskHolder.taskCheckBox.setEnabled(!editing);
        taskHolder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                actionListener.onTaskStatusChanged(task, isChecked)
        );

        bindInlineEditor(taskHolder, task, editing);

        taskHolder.itemView.setOnClickListener(v -> {
            if (!editing) {
                startInlineEdit(taskHolder.getBindingAdapterPosition());
            }
        });
    }

    private void bindTaskTitle(@NonNull TaskViewHolder holder,
                               @NonNull TaskModel task,
                               boolean completed,
                               boolean editing) {
        if (holder.titleWatcher != null) {
            holder.taskTitle.removeTextChangedListener(holder.titleWatcher);
            holder.titleWatcher = null;
        }

        holder.taskTitle.setError(null);
        holder.taskTitle.setText(editing
                ? editState.getDraftTaskText()
                : (task.getTask() == null ? "" : task.getTask()));

        int flags = holder.taskTitle.getPaintFlags();
        if (completed && !editing) {
            flags |= Paint.STRIKE_THRU_TEXT_FLAG;
        } else {
            flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
        }
        holder.taskTitle.setPaintFlags(flags);

        holder.taskTitle.setCursorVisible(editing);
        holder.taskTitle.setFocusable(editing);
        holder.taskTitle.setFocusableInTouchMode(editing);
        holder.taskTitle.setClickable(editing);

        if (!editing) {
            holder.taskTitle.clearFocus();
            return;
        }

        holder.titleWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editState.isEditing(task.getId())) {
                    editState.setDraftTaskText(s);
                    updateInlineSaveButton(holder);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        holder.taskTitle.addTextChangedListener(holder.titleWatcher);

        holder.taskTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveInlineEdit(holder, task);
                return true;
            }
            return false;
        });

        if (editState.consumePendingFocus(task.getId())) {
            String taskId = task.getId();
            holder.taskTitle.post(() -> {
                if (!editState.isEditing(taskId)
                        || holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) {
                    return;
                }
                holder.taskTitle.requestFocus();
                holder.taskTitle.setSelection(holder.taskTitle.length());
                InputMethodManager inputMethodManager =
                        (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(
                        holder.taskTitle,
                        InputMethodManager.SHOW_IMPLICIT
                );

                holder.itemView.postDelayed(() -> {
                    if (!editState.isEditing(taskId)
                            || holder.getBindingAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }

                    // Reveal the whole expanded editor, not just the EditText.
                    // Otherwise Android stops scrolling once the text is visible
                    // and leaves the Cancel / Save actions behind the keyboard.
                    Rect editorBounds = new Rect(
                            0,
                            0,
                            holder.itemView.getWidth(),
                            holder.itemView.getHeight()
                    );
                    holder.itemView.requestRectangleOnScreen(editorBounds, false);
                }, 250L);
            });
        }
    }

    private void bindInlineEditor(@NonNull TaskViewHolder holder,
                                  @NonNull TaskModel task,
                                  boolean editing) {
        holder.inlineEditArea.setVisibility(editing ? View.VISIBLE : View.GONE);
        if (!editing) {
            return;
        }

        holder.editToday.setCheckable(true);
        holder.editTomorrow.setCheckable(true);
        holder.editPickDate.setCheckable(true);
        holder.editPickTime.setCheckable(true);
        bindDraftDateControls(holder);
        setInlineControlsEnabled(holder, !editState.isSaveInProgress());
        updateInlineSaveButton(holder);

        holder.editToday.setOnClickListener(v -> {
            editState.toggleDueDate(storageDateForOffset(0));
            bindDraftDateControls(holder);
        });

        holder.editTomorrow.setOnClickListener(v -> {
            editState.toggleDueDate(storageDateForOffset(1));
            bindDraftDateControls(holder);
        });

        holder.editPickDate.setOnClickListener(v -> {
            if (!editState.isEditing(task.getId())) {
                return;
            }

            hideKeyboard(holder.taskTitle);
            boolean shown = actionListener.onTaskDatePickerRequested(
                    editState.getDraftDueDate()
            );
            editState.setDatePickerOpen(shown);
            bindDraftDateControls(holder);
        });

        holder.editPickTime.setOnClickListener(v -> {
            if (editState.getDraftDueDate().isEmpty()) {
                return;
            }

            if (!editState.getDraftDueTime().isEmpty()) {
                editState.toggleTimeOff();
                bindDraftTimeControls(holder);
            } else {
                hideKeyboard(holder.taskTitle);
                actionListener.onTaskTimePickerRequested(editState.getDraftDueTime());
            }
        });

        holder.editCancel.setOnClickListener(v -> cancelInlineEdit(holder, task));
        holder.editSave.setOnClickListener(v -> saveInlineEdit(holder, task));
    }

    private void bindDraftDateControls(@NonNull TaskViewHolder holder) {
        String draftDueDate = editState.getDraftDueDate();
        boolean hasDueDate = !draftDueDate.isEmpty();
        String today = storageDateForOffset(0);
        String tomorrow = storageDateForOffset(1);

        boolean isToday = today.equals(draftDueDate);
        boolean isTomorrow = tomorrow.equals(draftDueDate);
        boolean isCustomDate = hasDueDate && !isToday && !isTomorrow;

        holder.editToday.setChecked(isToday);
        holder.editTomorrow.setChecked(isTomorrow);
        holder.editPickDate.setChecked(editState.isDatePickerOpen() || isCustomDate);
        holder.editPickDate.setText(
                isCustomDate
                        ? TaskDateUtils.formatDateForRow(context, draftDueDate)
                        : context.getString(R.string.pick_date)
        );

        bindDraftTimeControls(holder);
    }

    private void bindDraftTimeControls(@NonNull TaskViewHolder holder) {
        boolean hasDueDate = !editState.getDraftDueDate().isEmpty();
        holder.editPickTime.setVisibility(hasDueDate ? View.VISIBLE : View.GONE);

        String draftDueTime = editState.getDraftDueTime();
        boolean hasTime = !draftDueTime.isEmpty();
        holder.editPickTime.setChecked(hasTime);
        holder.editPickTime.setText(
                hasTime
                        ? TaskDateUtils.formatTimeForDisplay(context, draftDueTime)
                        : context.getString(R.string.pick_time)
        );
    }

    public void onDatePickerResult(boolean confirmed,
                                   @Nullable String selectedDate) {
        String taskId = editState.getEditingTaskId();
        if (taskId == null) {
            return;
        }

        editState.setDatePickerOpen(false);
        if (confirmed) {
            editState.setDraftDueDate(selectedDate);
        }

        int position = getPositionForTaskId(taskId);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    public void onTimePickerResult(@Nullable String selectedTime) {
        String taskId = editState.getEditingTaskId();
        if (taskId == null) {
            return;
        }

        editState.setDraftDueTime(selectedTime);
        int position = getPositionForTaskId(taskId);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    private void startInlineEdit(int position) {
        if (!isTaskPosition(position) || editState.isSaveInProgress()) {
            return;
        }

        TaskModel task = rows.get(position).task;
        if (task == null || task.getId() == null) {
            return;
        }

        String previousTaskId = editState.getEditingTaskId();
        int previousPosition = previousTaskId == null
                ? RecyclerView.NO_POSITION
                : getPositionForTaskId(previousTaskId);

        editState.start(task);
        hideSwipeHint();

        if (previousPosition != RecyclerView.NO_POSITION && previousPosition != position) {
            notifyItemChanged(previousPosition);
        }
        notifyItemChanged(position);
    }

    private void cancelInlineEdit(@NonNull TaskViewHolder holder,
                                  @NonNull TaskModel task) {
        if (editState.isSaveInProgress() || !editState.isEditing(task.getId())) {
            return;
        }

        String taskId = task.getId();
        hideKeyboard(holder.taskTitle);
        editState.clear();

        int position = getPositionForTaskId(taskId);
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    private void saveInlineEdit(@NonNull TaskViewHolder holder,
                                @NonNull TaskModel task) {
        if (editState.isSaveInProgress() || !editState.isEditing(task.getId())) {
            return;
        }

        String taskText = editState.trimmedTaskText();
        if (taskText.isEmpty()) {
            holder.taskTitle.setError(context.getString(R.string.no_task_entered));
            holder.taskTitle.requestFocus();
            return;
        }

        if (!editState.hasChanges(task)) {
            cancelInlineEdit(holder, task);
            return;
        }

        editState.setSaveInProgress(true);
        setInlineControlsEnabled(holder, false);
        updateInlineSaveButton(holder);

        String taskId = task.getId();
        actionListener.onTaskEditSaveRequested(
                task,
                taskText,
                editState.getDraftDueDate(),
                editState.getDraftDueTime(),
                new EditSaveCallback() {
                    @Override
                    public void onSuccess() {
                        if (!editState.isEditing(taskId)) {
                            return;
                        }

                        hideKeyboard(holder.taskTitle);
                        editState.clear();
                        int position = getPositionForTaskId(taskId);
                        if (position != RecyclerView.NO_POSITION) {
                            notifyItemChanged(position);
                        } else {
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        if (!editState.isEditing(taskId)) {
                            return;
                        }

                        editState.setSaveInProgress(false);
                        int position = getPositionForTaskId(taskId);
                        if (position != RecyclerView.NO_POSITION) {
                            notifyItemChanged(position);
                        }
                    }
                }
        );
    }

    private void setInlineControlsEnabled(@NonNull TaskViewHolder holder, boolean enabled) {
        holder.taskTitle.setEnabled(enabled);
        holder.editToday.setEnabled(enabled);
        holder.editTomorrow.setEnabled(enabled);
        holder.editPickDate.setEnabled(enabled);
        holder.editPickTime.setEnabled(enabled);
        holder.editCancel.setEnabled(enabled);
        holder.editSave.setEnabled(enabled && editState.hasValidText());
        holder.editSave.setText(
                editState.isSaveInProgress()
                        ? context.getString(R.string.saving)
                        : context.getString(R.string.save)
        );
    }

    private void updateInlineSaveButton(@NonNull TaskViewHolder holder) {
        holder.editSave.setEnabled(
                !editState.isSaveInProgress() && editState.hasValidText()
        );
        holder.editSave.setText(
                editState.isSaveInProgress()
                        ? context.getString(R.string.saving)
                        : context.getString(R.string.save)
        );
        if (editState.hasValidText()) {
            holder.taskTitle.setError(null);
        }
    }

    private void hideKeyboard(@NonNull View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    @NonNull
    private String storageDateForOffset(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return TaskDateUtils.toStorageDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void bindDueDate(@NonNull TaskViewHolder holder,
                             @NonNull TaskModel task,
                             @NonNull TaskListOrganizer.SectionType section,
                             boolean completed) {
        String displayDue = TaskDateUtils.formatDueForRow(
                context,
                task.getDue(),
                task.getDueTime()
        );

        if (displayDue.isEmpty()) {
            holder.dueDateText.setVisibility(View.GONE);
            return;
        }

        holder.dueDateText.setVisibility(View.VISIBLE);
        holder.dueDateText.setText(displayDue);
        holder.dueDateText.setTextColor(
                ContextCompat.getColor(
                        context,
                        !completed && section == TaskListOrganizer.SectionType.OVERDUE
                                ? R.color.delete_color
                                : R.color.dark_gray
                )
        );
    }

    private void bindHeader(@NonNull HeaderViewHolder holder, @NonNull Row row) {
        holder.title.setText(row.header);
        holder.count.setText(String.valueOf(row.count));

        boolean isOverdue = row.section == TaskListOrganizer.SectionType.OVERDUE;
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

        boolean isCompleted = row.section == TaskListOrganizer.SectionType.COMPLETED;
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

        if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            if (taskHolder.titleWatcher != null) {
                taskHolder.taskTitle.removeTextChangedListener(taskHolder.titleWatcher);
                taskHolder.titleWatcher = null;
            }
        }

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

    public void requestDelete(@NonNull TaskModel task) {
        actionListener.onDeleteTask(task);
    }

    public void requestEdit(int position) {
        startInlineEdit(position);
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
        @NonNull
        final TaskListOrganizer.SectionType section;
        final int count;
        final boolean firstInSection;
        final boolean lastInSection;

        private Row(@Nullable String header,
                    @Nullable TaskModel task,
                    @NonNull TaskListOrganizer.SectionType section,
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

        static Row header(@NonNull String header,
                          @NonNull TaskListOrganizer.SectionType section,
                          int count) {
            return new Row(header, null, section, count, false, false);
        }

        static Row task(@NonNull TaskModel task,
                        @NonNull TaskListOrganizer.SectionType section,
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
        final EditText taskTitle;
        final CheckBox taskCheckBox;
        final View swipeHint;
        final ImageButton dismissSwipeHint;
        final View inlineEditArea;
        final MaterialButton editToday;
        final MaterialButton editTomorrow;
        final MaterialButton editPickDate;
        final MaterialButton editPickTime;
        final MaterialButton editCancel;
        final MaterialButton editSave;
        final View divider;
        @Nullable
        TextWatcher titleWatcher;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            dueDateText = itemView.findViewById(R.id.tvDueDate);
            taskTitle = itemView.findViewById(R.id.etTaskTitle);
            taskCheckBox = itemView.findViewById(R.id.cbTaskDone);
            swipeHint = itemView.findViewById(R.id.swipeHint);
            dismissSwipeHint = itemView.findViewById(R.id.btnDismissSwipeHint);
            inlineEditArea = itemView.findViewById(R.id.inlineEditArea);
            editToday = itemView.findViewById(R.id.btnEditToday);
            editTomorrow = itemView.findViewById(R.id.btnEditTomorrow);
            editPickDate = itemView.findViewById(R.id.btnEditPickDate);
            editPickTime = itemView.findViewById(R.id.btnEditPickTime);
            editCancel = itemView.findViewById(R.id.btnEditCancel);
            editSave = itemView.findViewById(R.id.btnEditSave);
            divider = itemView.findViewById(R.id.taskDivider);
        }
    }

    public interface TaskActionListener {
        void onTaskEditSaveRequested(@NonNull TaskModel task,
                                     @NonNull String taskText,
                                     @NonNull String dueDate,
                                     @NonNull String dueTime,
                                     @NonNull EditSaveCallback callback);

        void onDeleteTask(@NonNull TaskModel task);

        void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete);

        boolean onTaskDatePickerRequested(@NonNull String initialDate);

        void onTaskTimePickerRequested(@NonNull String initialTime);
    }

    public interface EditSaveCallback {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }
}

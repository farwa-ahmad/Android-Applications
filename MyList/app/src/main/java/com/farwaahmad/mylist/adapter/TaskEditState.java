package com.farwaahmad.mylist.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;

/** Holds inline-edit state independently from RecyclerView view holders. */
public final class TaskEditState {

    @Nullable
    private String editingTaskId;
    @Nullable
    private String pendingFocusTaskId;
    @NonNull
    private String draftTaskText = "";
    @NonNull
    private String draftDueDate = "";
    @NonNull
    private String draftDueTime = "";
    private boolean datePickerOpen;
    private boolean saveInProgress;

    public void start(@NonNull TaskModel task) {
        if (task.getId() == null) {
            throw new IllegalArgumentException("Task identity is missing.");
        }

        editingTaskId = task.getId();
        pendingFocusTaskId = task.getId();
        draftTaskText = task.getTask() == null ? "" : task.getTask();
        draftDueDate = TaskDateUtils.normalizeForStorage(
                task.getDue() == null ? "" : task.getDue()
        );
        draftDueTime = draftDueDate.isEmpty()
                ? ""
                : TaskDateUtils.normalizeTimeForStorage(task.getDueTime());
        datePickerOpen = false;
        saveInProgress = false;
    }

    public void clear() {
        editingTaskId = null;
        pendingFocusTaskId = null;
        draftTaskText = "";
        draftDueDate = "";
        draftDueTime = "";
        datePickerOpen = false;
        saveInProgress = false;
    }

    public boolean isEditing(@Nullable String taskId) {
        return taskId != null && taskId.equals(editingTaskId);
    }

    @Nullable
    public String getEditingTaskId() {
        return editingTaskId;
    }

    public boolean consumePendingFocus(@Nullable String taskId) {
        if (taskId == null || !taskId.equals(pendingFocusTaskId)) {
            return false;
        }

        pendingFocusTaskId = null;
        return true;
    }

    @NonNull
    public String getDraftTaskText() {
        return draftTaskText;
    }

    public void setDraftTaskText(@Nullable CharSequence text) {
        draftTaskText = text == null ? "" : text.toString();
    }

    @NonNull
    public String getDraftDueDate() {
        return draftDueDate;
    }

    public void toggleDueDate(@NonNull String dueDate) {
        if (dueDate.equals(draftDueDate)) {
            draftDueDate = "";
            draftDueTime = "";
        } else {
            draftDueDate = dueDate;
        }
    }

    public void setDraftDueDate(@Nullable String dueDate) {
        draftDueDate = TaskDateUtils.normalizeForStorage(
                dueDate == null ? "" : dueDate
        );
        if (draftDueDate.isEmpty()) {
            draftDueTime = "";
        }
    }

    @NonNull
    public String getDraftDueTime() {
        return draftDueTime;
    }

    public void toggleTimeOff() {
        draftDueTime = "";
    }

    public void setDraftDueTime(@Nullable String dueTime) {
        draftDueTime = draftDueDate.isEmpty()
                ? ""
                : TaskDateUtils.normalizeTimeForStorage(dueTime);
    }

    public boolean isDatePickerOpen() {
        return datePickerOpen;
    }

    public void setDatePickerOpen(boolean open) {
        datePickerOpen = open;
    }

    public boolean isSaveInProgress() {
        return saveInProgress;
    }

    public void setSaveInProgress(boolean inProgress) {
        saveInProgress = inProgress;
    }

    public boolean hasValidText() {
        return !draftTaskText.trim().isEmpty();
    }

    @NonNull
    public String trimmedTaskText() {
        return draftTaskText.trim();
    }

    public boolean hasChanges(@NonNull TaskModel task) {
        String originalText = task.getTask() == null ? "" : task.getTask().trim();
        String originalDue = TaskDateUtils.normalizeForStorage(
                task.getDue() == null ? "" : task.getDue()
        );
        String originalDueTime = originalDue.isEmpty()
                ? ""
                : TaskDateUtils.normalizeTimeForStorage(task.getDueTime());

        return !trimmedTaskText().equals(originalText)
                || !draftDueDate.equals(originalDue)
                || !draftDueTime.equals(originalDueTime);
    }
}

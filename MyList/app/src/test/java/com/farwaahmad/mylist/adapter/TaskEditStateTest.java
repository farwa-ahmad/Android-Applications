package com.farwaahmad.mylist.adapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import com.farwaahmad.mylist.model.TaskModel;

import org.junit.Test;

public class TaskEditStateTest {

    @Test
    public void toggleDueDate_clearsTimeWhenDateIsRemoved() {
        TaskEditState state = new TaskEditState();
        TaskModel task = task("task-1", "Title", "2026-09-22", "09:30");
        state.start(task);

        state.toggleDueDate("2026-09-22");

        assertEquals("", state.getDraftDueDate());
        assertEquals("", state.getDraftDueTime());
    }

    @Test
    public void setTime_withoutDate_keepsTimeEmpty() {
        TaskEditState state = new TaskEditState();
        TaskModel task = task("task-1", "Title", "", "");
        state.start(task);

        state.setDraftDueTime("09:30");

        assertEquals("", state.getDraftDueTime());
    }

    @Test
    public void hasChanges_comparesNormalizedDraftAgainstOriginal() {
        TaskEditState state = new TaskEditState();
        TaskModel task = task("task-1", "Title", "2026-09-22", "09:30");
        state.start(task);

        assertFalse(state.hasChanges(task));

        state.setDraftTaskText("Updated");
        assertTrue(state.hasChanges(task));
    }

    @Test
    public void pendingFocus_isConsumedOnlyOnce() {
        TaskEditState state = new TaskEditState();
        state.start(task("task-1", "Title", "", ""));

        assertTrue(state.consumePendingFocus("task-1"));
        assertFalse(state.consumePendingFocus("task-1"));
    }

    private static TaskModel task(String id,
                                  String title,
                                  String due,
                                  String dueTime) {
        TaskModel task = new TaskModel();
        task.setId(id);
        task.setTask(title);
        task.setDue(due);
        task.setDueTime(dueTime);
        task.setStatus(0);
        return task;
    }
}

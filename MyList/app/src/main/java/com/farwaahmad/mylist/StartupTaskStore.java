package com.farwaahmad.mylist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farwaahmad.mylist.model.TaskModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StartupTaskStore {

    private static final Object LOCK = new Object();

    @Nullable
    private static State pendingState;

    private StartupTaskStore() {
    }

    public static void publishTasks(@NonNull List<TaskModel> tasks) {
        synchronized (LOCK) {
            pendingState = new State(new ArrayList<>(tasks), false);
        }
    }

    public static void publishError() {
        synchronized (LOCK) {
            pendingState = new State(Collections.emptyList(), true);
        }
    }

    @Nullable
    public static State consume() {
        synchronized (LOCK) {
            State state = pendingState;
            pendingState = null;
            return state;
        }
    }

    public static final class State {

        @NonNull
        private final List<TaskModel> tasks;
        private final boolean loadFailed;

        private State(@NonNull List<TaskModel> tasks, boolean loadFailed) {
            this.tasks = tasks;
            this.loadFailed = loadFailed;
        }

        @NonNull
        public List<TaskModel> getTasks() {
            return tasks;
        }

        public boolean isLoadFailed() {
            return loadFailed;
        }
    }
}

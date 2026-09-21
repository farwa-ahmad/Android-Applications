package com.farwaahmad.mylist.adapter;

import androidx.annotation.NonNull;

import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Pure task grouping/sorting logic used by the main task list. */
public final class TaskListOrganizer {

    public enum SectionType {
        OVERDUE,
        TODAY,
        UPCOMING,
        NO_DUE_DATE,
        COMPLETED
    }

    public static final class Section {

        @NonNull
        private final SectionType type;
        @NonNull
        private final List<TaskModel> tasks;

        private Section(@NonNull SectionType type,
                        @NonNull List<TaskModel> tasks) {
            this.type = type;
            this.tasks = Collections.unmodifiableList(tasks);
        }

        @NonNull
        public SectionType getType() {
            return type;
        }

        @NonNull
        public List<TaskModel> getTasks() {
            return tasks;
        }
    }

    private TaskListOrganizer() {
    }

    @NonNull
    public static List<Section> organize(@NonNull List<TaskModel> source) {
        List<TaskModel> overdue = new ArrayList<>();
        List<TaskModel> today = new ArrayList<>();
        List<TaskModel> upcoming = new ArrayList<>();
        List<TaskModel> noDueDate = new ArrayList<>();
        List<TaskModel> completed = new ArrayList<>();

        for (TaskModel task : source) {
            if (task.getStatus() != 0) {
                completed.add(task);
                continue;
            }

            switch (TaskDateUtils.bucketFor(task.getDue(), task.getDueTime())) {
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
                Comparator.comparingLong(task ->
                        TaskDateUtils.sortTimestamp(task.getDue(), task.getDueTime()));
        overdue.sort(byDueDate);
        today.sort(byDueDate);
        upcoming.sort(byDueDate);

        List<Section> sections = new ArrayList<>();
        addIfNotEmpty(sections, SectionType.OVERDUE, overdue);
        addIfNotEmpty(sections, SectionType.TODAY, today);
        addIfNotEmpty(sections, SectionType.UPCOMING, upcoming);
        addIfNotEmpty(sections, SectionType.NO_DUE_DATE, noDueDate);
        addIfNotEmpty(sections, SectionType.COMPLETED, completed);
        return Collections.unmodifiableList(sections);
    }

    private static void addIfNotEmpty(@NonNull List<Section> sections,
                                      @NonNull SectionType type,
                                      @NonNull List<TaskModel> tasks) {
        if (!tasks.isEmpty()) {
            sections.add(new Section(type, new ArrayList<>(tasks)));
        }
    }
}

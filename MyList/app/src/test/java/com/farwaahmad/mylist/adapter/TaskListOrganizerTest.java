package com.farwaahmad.mylist.adapter;

import static org.junit.Assert.assertEquals;

import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TaskListOrganizerTest {

    @Test
    public void organize_placesTasksInExpectedSections() {
        Calendar now = Calendar.getInstance();
        String today = TaskDateUtils.toStorageDate(
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        Calendar yesterday = (Calendar) now.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        String past = TaskDateUtils.toStorageDate(
                yesterday.get(Calendar.YEAR),
                yesterday.get(Calendar.MONTH),
                yesterday.get(Calendar.DAY_OF_MONTH)
        );

        Calendar tomorrow = (Calendar) now.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        String future = TaskDateUtils.toStorageDate(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH),
                tomorrow.get(Calendar.DAY_OF_MONTH)
        );

        List<TaskListOrganizer.Section> sections = TaskListOrganizer.organize(Arrays.asList(
                task("none", "", "", 0),
                task("done", future, "", 1),
                task("future", future, "", 0),
                task("past", past, "", 0),
                task("today", today, "", 0)
        ));

        assertEquals(5, sections.size());
        assertEquals(TaskListOrganizer.SectionType.OVERDUE, sections.get(0).getType());
        assertEquals("past", sections.get(0).getTasks().get(0).getId());
        assertEquals(TaskListOrganizer.SectionType.TODAY, sections.get(1).getType());
        assertEquals(TaskListOrganizer.SectionType.UPCOMING, sections.get(2).getType());
        assertEquals(TaskListOrganizer.SectionType.NO_DUE_DATE, sections.get(3).getType());
        assertEquals(TaskListOrganizer.SectionType.COMPLETED, sections.get(4).getType());
    }

    @Test
    public void organize_sortsTimedTasksBeforeUntimedTasksOnSameDate() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        String date = TaskDateUtils.toStorageDate(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH),
                tomorrow.get(Calendar.DAY_OF_MONTH)
        );

        List<TaskListOrganizer.Section> sections = TaskListOrganizer.organize(Arrays.asList(
                task("untimed", date, "", 0),
                task("late", date, "18:00", 0),
                task("early", date, "08:30", 0)
        ));

        assertEquals(1, sections.size());
        List<TaskModel> tasks = sections.get(0).getTasks();
        assertEquals("early", tasks.get(0).getId());
        assertEquals("late", tasks.get(1).getId());
        assertEquals("untimed", tasks.get(2).getId());
    }

    private static TaskModel task(String id,
                                  String due,
                                  String dueTime,
                                  int status) {
        TaskModel task = new TaskModel();
        task.setId(id);
        task.setTask(id);
        task.setDue(due);
        task.setDueTime(dueTime);
        task.setStatus(status);
        return task;
    }
}

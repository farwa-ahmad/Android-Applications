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
        String past = relativeStorageDate(-1);
        String today = relativeStorageDate(0);
        String tomorrow = relativeStorageDate(1);
        String thisWeek = relativeStorageDate(4);
        String later = relativeStorageDate(8);

        List<TaskListOrganizer.Section> sections = TaskListOrganizer.organize(Arrays.asList(
                task("none", "", "", 0),
                task("done", tomorrow, "", 1),
                task("later", later, "", 0),
                task("this-week", thisWeek, "", 0),
                task("tomorrow", tomorrow, "", 0),
                task("past", past, "", 0),
                task("today", today, "", 0)
        ));

        assertEquals(7, sections.size());
        assertEquals(TaskListOrganizer.SectionType.OVERDUE, sections.get(0).getType());
        assertEquals("past", sections.get(0).getTasks().get(0).getId());

        assertEquals(TaskListOrganizer.SectionType.TODAY, sections.get(1).getType());
        assertEquals("today", sections.get(1).getTasks().get(0).getId());

        assertEquals(TaskListOrganizer.SectionType.TOMORROW, sections.get(2).getType());
        assertEquals("tomorrow", sections.get(2).getTasks().get(0).getId());

        assertEquals(TaskListOrganizer.SectionType.THIS_WEEK, sections.get(3).getType());
        assertEquals("this-week", sections.get(3).getTasks().get(0).getId());

        assertEquals(TaskListOrganizer.SectionType.LATER, sections.get(4).getType());
        assertEquals("later", sections.get(4).getTasks().get(0).getId());

        assertEquals(TaskListOrganizer.SectionType.NO_DUE_DATE, sections.get(5).getType());
        assertEquals("none", sections.get(5).getTasks().get(0).getId());

        assertEquals(TaskListOrganizer.SectionType.COMPLETED, sections.get(6).getType());
        assertEquals("done", sections.get(6).getTasks().get(0).getId());
    }

    @Test
    public void organize_sortsTimedTasksBeforeUntimedTasksOnSameDate() {
        String date = relativeStorageDate(1);

        List<TaskListOrganizer.Section> sections = TaskListOrganizer.organize(Arrays.asList(
                task("untimed", date, "", 0),
                task("late", date, "18:00", 0),
                task("early", date, "08:30", 0)
        ));

        assertEquals(1, sections.size());
        assertEquals(TaskListOrganizer.SectionType.TOMORROW, sections.get(0).getType());

        List<TaskModel> tasks = sections.get(0).getTasks();
        assertEquals("early", tasks.get(0).getId());
        assertEquals("late", tasks.get(1).getId());
        assertEquals("untimed", tasks.get(2).getId());
    }

    private static String relativeStorageDate(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return TaskDateUtils.toStorageDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
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

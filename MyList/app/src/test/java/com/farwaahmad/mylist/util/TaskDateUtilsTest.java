package com.farwaahmad.mylist.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;

public class TaskDateUtilsTest {

    @Test
    public void toStorageDate_usesIsoDateFormat() {
        assertEquals("2026-09-20", TaskDateUtils.toStorageDate(2026, 8, 20));
    }

    @Test
    public void toStorageTime_zeroPadsHourAndMinute() {
        assertEquals("07:05", TaskDateUtils.toStorageTime(7, 5));
    }

    @Test
    public void normalizeForStorage_convertsLegacyDate() {
        assertEquals("2026-09-20", TaskDateUtils.normalizeForStorage("20/9/2026"));
    }

    @Test
    public void normalizeForStorage_keepsInvalidInputForBackwardCompatibility() {
        assertEquals("not-a-date", TaskDateUtils.normalizeForStorage("not-a-date"));
    }

    @Test
    public void normalizeTimeForStorage_rejectsInvalidTime() {
        assertEquals("", TaskDateUtils.normalizeTimeForStorage("25:10"));
    }

    @Test
    public void calendarForDue_rejectsImpossibleDate() {
        assertNull(TaskDateUtils.calendarForDue("2026-02-30"));
    }

    @Test
    public void calendarForDue_acceptsValidStorageDate() {
        assertNotNull(TaskDateUtils.calendarForDue("2026-09-20"));
    }

    @Test
    public void bucketFor_emptyDate_hasNoDueBucket() {
        assertEquals(TaskDateUtils.BUCKET_NONE, TaskDateUtils.bucketFor(""));
    }

    @Test
    public void bucketFor_yesterday_isOverdue() {
        assertEquals(
                TaskDateUtils.BUCKET_OVERDUE,
                TaskDateUtils.bucketFor(relativeStorageDate(-1))
        );
    }

    @Test
    public void bucketFor_todayWithoutTime_isToday() {
        assertEquals(
                TaskDateUtils.BUCKET_TODAY,
                TaskDateUtils.bucketFor(relativeStorageDate(0))
        );
    }

    @Test
    public void bucketFor_tomorrow_isTomorrow() {
        assertEquals(
                TaskDateUtils.BUCKET_TOMORROW,
                TaskDateUtils.bucketFor(relativeStorageDate(1))
        );
    }

    @Test
    public void bucketFor_daySeven_isThisWeek() {
        assertEquals(
                TaskDateUtils.BUCKET_THIS_WEEK,
                TaskDateUtils.bucketFor(relativeStorageDate(7))
        );
    }

    @Test
    public void bucketFor_dayEight_isLater() {
        assertEquals(
                TaskDateUtils.BUCKET_LATER,
                TaskDateUtils.bucketFor(relativeStorageDate(8))
        );
    }

    @Test
    public void sortTimestamp_sameDate_placesTimedTaskBeforeUntimedTask() {
        String date = relativeStorageDate(1);

        long timed = TaskDateUtils.sortTimestamp(date, "09:00");
        long untimed = TaskDateUtils.sortTimestamp(date, "");

        assertTrue(timed < untimed);
    }

    @Test
    public void sortTimestamp_invalidDate_sortsLast() {
        assertEquals(Long.MAX_VALUE, TaskDateUtils.sortTimestamp("invalid"));
    }

    private static String relativeStorageDate(int dayOffset) {
        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return TaskDateUtils.toStorageDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }
}

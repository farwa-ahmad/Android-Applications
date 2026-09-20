package com.farwaahmad.mylist.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farwaahmad.mylist.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class TaskDateUtils {

    public static final int BUCKET_OVERDUE = 0;
    public static final int BUCKET_TODAY = 1;
    public static final int BUCKET_UPCOMING = 2;
    public static final int BUCKET_NONE = 3;

    private static final String STORAGE_PATTERN = "yyyy-MM-dd";
    private static final String LEGACY_PATTERN = "d/M/yyyy";
    private static final String TIME_PATTERN = "HH:mm";

    private TaskDateUtils() {
    }

    @NonNull
    public static String toStorageDate(int year, int zeroBasedMonth, int dayOfMonth) {
        return String.format(
                Locale.US,
                "%04d-%02d-%02d",
                year,
                zeroBasedMonth + 1,
                dayOfMonth
        );
    }

    @NonNull
    public static String toStorageTime(int hourOfDay, int minute) {
        return String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
    }

    @NonNull
    public static String normalizeForStorage(@Nullable String dueDate) {
        Calendar calendar = calendarForDue(dueDate);
        if (calendar == null) {
            return dueDate == null ? "" : dueDate;
        }

        return toStorageDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    @NonNull
    public static String normalizeTimeForStorage(@Nullable String dueTime) {
        Calendar calendar = calendarForDueTime(dueTime);
        if (calendar == null) {
            return "";
        }

        return toStorageTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        );
    }

    @Nullable
    public static Calendar calendarForDue(@Nullable String dueDate) {
        if (dueDate == null || dueDate.trim().isEmpty()) {
            return null;
        }

        Date parsed = parse(dueDate.trim(), STORAGE_PATTERN);
        if (parsed == null) {
            parsed = parse(dueDate.trim(), LEGACY_PATTERN);
        }

        if (parsed == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsed);
        normalize(calendar);
        return calendar;
    }

    @Nullable
    public static Calendar calendarForDueTime(@Nullable String dueTime) {
        if (dueTime == null || dueTime.trim().isEmpty()) {
            return null;
        }

        Date parsed = parse(dueTime.trim(), TIME_PATTERN);
        if (parsed == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsed);
        return calendar;
    }

    public static int bucketFor(@Nullable String dueDate) {
        return bucketFor(dueDate, null);
    }

    public static int bucketFor(@Nullable String dueDate, @Nullable String dueTime) {
        Calendar due = calendarForDue(dueDate);
        if (due == null) {
            return BUCKET_NONE;
        }

        Calendar today = Calendar.getInstance();
        normalize(today);

        if (due.before(today)) {
            return BUCKET_OVERDUE;
        }

        if (!sameDay(due, today)) {
            return BUCKET_UPCOMING;
        }

        Calendar time = calendarForDueTime(dueTime);
        if (time == null) {
            return BUCKET_TODAY;
        }

        Calendar dueMoment = Calendar.getInstance();
        dueMoment.set(
                due.get(Calendar.YEAR),
                due.get(Calendar.MONTH),
                due.get(Calendar.DAY_OF_MONTH),
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                0
        );
        dueMoment.set(Calendar.MILLISECOND, 0);

        return dueMoment.before(Calendar.getInstance())
                ? BUCKET_OVERDUE
                : BUCKET_TODAY;
    }

    public static long sortTimestamp(@Nullable String dueDate) {
        return sortTimestamp(dueDate, null);
    }

    public static long sortTimestamp(@Nullable String dueDate, @Nullable String dueTime) {
        Calendar due = calendarForDue(dueDate);
        if (due == null) {
            return Long.MAX_VALUE;
        }

        Calendar time = calendarForDueTime(dueTime);
        if (time == null) {
            due.set(Calendar.HOUR_OF_DAY, 23);
            due.set(Calendar.MINUTE, 59);
            due.set(Calendar.SECOND, 59);
        } else {
            due.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
            due.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
            due.set(Calendar.SECOND, 0);
        }
        due.set(Calendar.MILLISECOND, 0);
        return due.getTimeInMillis();
    }

    public static boolean isOverdue(@Nullable String dueDate) {
        return bucketFor(dueDate) == BUCKET_OVERDUE;
    }

    public static boolean isOverdue(@Nullable String dueDate, @Nullable String dueTime) {
        return bucketFor(dueDate, dueTime) == BUCKET_OVERDUE;
    }

    @NonNull
    public static String formatForDisplay(@NonNull Context context, @Nullable String dueDate) {
        return formatDateForRow(context, dueDate);
    }

    @NonNull
    public static String formatDateForRow(@NonNull Context context, @Nullable String dueDate) {
        Calendar due = calendarForDue(dueDate);
        if (due == null) {
            return "";
        }

        Calendar today = Calendar.getInstance();
        normalize(today);

        if (sameDay(due, today)) {
            return context.getString(R.string.today);
        }

        Calendar tomorrow = (Calendar) today.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        if (sameDay(due, tomorrow)) {
            return context.getString(R.string.tomorrow);
        }

        for (int offset = 2; offset <= 7; offset++) {
            Calendar upcoming = (Calendar) today.clone();
            upcoming.add(Calendar.DAY_OF_MONTH, offset);
            if (sameDay(due, upcoming)) {
                return new SimpleDateFormat("EEE", Locale.getDefault())
                        .format(due.getTime());
            }
        }

        return new SimpleDateFormat("d MMM", Locale.getDefault())
                .format(due.getTime());
    }

    @NonNull
    public static String formatTimeForDisplay(@NonNull Context context,
                                              @Nullable String dueTime) {
        Calendar time = calendarForDueTime(dueTime);
        if (time == null) {
            return "";
        }

        return android.text.format.DateFormat
                .getTimeFormat(context)
                .format(time.getTime());
    }

    @NonNull
    public static String formatDueForRow(@NonNull Context context,
                                         @Nullable String dueDate,
                                         @Nullable String dueTime) {
        String date = formatDateForRow(context, dueDate);
        if (date.isEmpty()) {
            return "";
        }

        String time = formatTimeForDisplay(context, dueTime);
        return time.isEmpty() ? date : date + ", " + time;
    }

    private static boolean sameDay(@NonNull Calendar first, @NonNull Calendar second) {
        return first.get(Calendar.ERA) == second.get(Calendar.ERA)
                && first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    @Nullable
    private static Date parse(@NonNull String value, @NonNull String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setLenient(false);
        try {
            return formatter.parse(value);
        } catch (ParseException ignored) {
            return null;
        }
    }

    private static void normalize(@NonNull Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}

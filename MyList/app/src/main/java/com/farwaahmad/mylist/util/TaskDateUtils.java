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

    public static int bucketFor(@Nullable String dueDate) {
        Calendar due = calendarForDue(dueDate);
        if (due == null) {
            return BUCKET_NONE;
        }

        Calendar today = Calendar.getInstance();
        normalize(today);

        if (due.before(today)) {
            return BUCKET_OVERDUE;
        }

        if (sameDay(due, today)) {
            return BUCKET_TODAY;
        }

        return BUCKET_UPCOMING;
    }

    public static long sortTimestamp(@Nullable String dueDate) {
        Calendar due = calendarForDue(dueDate);
        return due == null ? Long.MAX_VALUE : due.getTimeInMillis();
    }

    public static boolean isOverdue(@Nullable String dueDate) {
        return bucketFor(dueDate) == BUCKET_OVERDUE;
    }

    @NonNull
    public static String formatForDisplay(@NonNull Context context, @Nullable String dueDate) {
        Calendar due = calendarForDue(dueDate);
        if (due == null) {
            return dueDate == null ? "" : dueDate;
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

        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE, d MMM", Locale.getDefault());
        return formatter.format(due.getTime());
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

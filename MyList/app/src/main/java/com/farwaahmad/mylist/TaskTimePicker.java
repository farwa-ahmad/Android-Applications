package com.farwaahmad.mylist;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.farwaahmad.mylist.util.TaskDateUtils;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

/** Shared Material time picker used by both task creation and task editing. */
public final class TaskTimePicker {

    public static final String RESULT = "time";
    private static final String TAG = "TaskTimePicker";

    private TaskTimePicker() {
    }

    public static void show(@NonNull Context context,
                            @NonNull FragmentManager manager,
                            @NonNull String requestKey,
                            @NonNull String initialTime) {
        if (manager.isStateSaved() || manager.findFragmentByTag(TAG) != null) {
            return;
        }

        Calendar time = TaskDateUtils.calendarForDueTime(initialTime);
        if (time == null) {
            time = Calendar.getInstance();
        }

        int timeFormat = android.text.format.DateFormat.is24HourFormat(context)
                ? TimeFormat.CLOCK_24H
                : TimeFormat.CLOCK_12H;

        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTitleText(R.string.pick_time)
                .setTimeFormat(timeFormat)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setHour(time.get(Calendar.HOUR_OF_DAY))
                .setMinute(time.get(Calendar.MINUTE))
                .build();

        picker.addOnPositiveButtonClickListener(view -> {
            Bundle result = new Bundle();
            result.putString(
                    RESULT,
                    TaskDateUtils.toStorageTime(
                            picker.getHour(),
                            picker.getMinute()
                    )
            );
            manager.setFragmentResult(requestKey, result);
        });

        picker.show(manager, TAG);
    }
}

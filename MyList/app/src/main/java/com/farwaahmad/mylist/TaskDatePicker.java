package com.farwaahmad.mylist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.content.Context;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.farwaahmad.mylist.adapter.CalendarMonthAdapter;
import com.farwaahmad.mylist.util.TaskDateUtils;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

/** A shared calendar surface for task creation and inline editing. */
public class TaskDatePicker extends DialogFragment {
    public static final String RESULT = "date";
    public static final String RESULT_CONFIRMED = "confirmed";
    private static final String TAG = "TaskDatePicker";
    private Calendar month;
    private String selected;
    private CalendarMonthAdapter adapter;
    private TextView title;
    private boolean resultDelivered;

    public static boolean show(FragmentManager manager, String requestKey, String initialDate) {
        if (manager.isStateSaved() || manager.findFragmentByTag(TAG) != null) return false;
        TaskDatePicker picker = new TaskDatePicker();
        Bundle args = new Bundle();
        args.putString("key", requestKey);
        args.putString(RESULT, initialDate);
        picker.setArguments(args);
        picker.show(manager, TAG);
        return true;
    }

    @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle state) {
        resultDelivered = state != null && state.getBoolean("resultDelivered", false);
        selected = state == null ? requireArguments().getString(RESULT, "") : state.getString(RESULT, "");
        month = TaskDateUtils.calendarForDue(selected);
        if (month == null) month = Calendar.getInstance();
        if (selected.isEmpty()) selected = TaskDateUtils.toStorageDate(month.get(Calendar.YEAR), month.get(Calendar.MONTH), month.get(Calendar.DAY_OF_MONTH));
        month.set(Calendar.DAY_OF_MONTH, 1);
        if (state != null) month.setTimeInMillis(state.getLong("month"));
        View view = getLayoutInflater().inflate(R.layout.date_picker_dialog, null);
        title = view.findViewById(R.id.tvMonthTitle);
        title.setContentDescription(getString(R.string.choose_month_year));
        title.setOnClickListener(v -> chooseMonthYear());
        title.setFocusable(true);
        RecyclerView grid = view.findViewById(R.id.rvCalendar);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        adapter = new CalendarMonthAdapter(date -> { selected = date; render(); }, false);
        grid.setAdapter(adapter);
        view.findViewById(R.id.btnPreviousMonth).setOnClickListener(v -> moveMonth(-1));
        view.findViewById(R.id.btnNextMonth).setOnClickListener(v -> moveMonth(1));
        render();
        return new MaterialAlertDialogBuilder(requireContext())
                .setCustomTitle(getLayoutInflater().inflate(R.layout.picker_dialog_title, null))
                .setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.set_date, (dialog, which) -> {
                    Bundle result = new Bundle();
                    result.putBoolean(RESULT_CONFIRMED, true);
                    result.putString(RESULT, selected);
                    resultDelivered = true;
                    getParentFragmentManager().setFragmentResult(
                            requireArguments().getString("key"),
                            result
                    );
                }).create();
    }

    private void moveMonth(int offset) { month.add(Calendar.MONTH, offset); render(); }
    private void render() {
        title.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.getTime()));
        adapter.submitMonth(month, Collections.emptyList(), selected);
    }

    private void chooseMonthYear() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setGravity(android.view.Gravity.CENTER);
        NumberPicker months = new NumberPicker(requireContext());
        months.setMinValue(0); months.setMaxValue(11);
        months.setDisplayedValues(java.util.Arrays.copyOf(new DateFormatSymbols().getMonths(), 12));
        months.setValue(month.get(Calendar.MONTH));
        months.setContentDescription(getString(R.string.calendar_month));
        NumberPicker years = new NumberPicker(requireContext());
        years.setMinValue(1900); years.setMaxValue(9999);
        years.setValue(Math.max(1900, Math.min(9999, month.get(Calendar.YEAR))));
        years.setWrapSelectorWheel(false);
        years.setContentDescription(getString(R.string.calendar_year));
        row.addView(months, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(years, new LinearLayout.LayoutParams(0, -2, 1));
        new MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.choose_month_year)
                .setView(row).setNegativeButton(R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    month.set(Calendar.DAY_OF_MONTH, 1);
                    month.set(Calendar.YEAR, years.getValue());
                    month.set(Calendar.MONTH, months.getValue()); render();
                }).show();
    }

    @Override public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            InputMethodManager keyboard = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(getDialog().getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override public void onDismiss(@NonNull DialogInterface dialog) {
        if (!resultDelivered && getArguments() != null) {
            Bundle result = new Bundle();
            result.putBoolean(RESULT_CONFIRMED, false);
            resultDelivered = true;
            getParentFragmentManager().setFragmentResult(
                    requireArguments().getString("key"),
                    result
            );
        }
        super.onDismiss(dialog);
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(RESULT, selected);
        state.putLong("month", month.getTimeInMillis());
        state.putBoolean("resultDelivered", resultDelivered);
    }
}

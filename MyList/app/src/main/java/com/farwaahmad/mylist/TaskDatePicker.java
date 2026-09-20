package com.farwaahmad.mylist;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
    private static final String TAG = "TaskDatePicker";
    private Calendar month;
    private String selected;
    private CalendarMonthAdapter adapter;
    private TextView title;

    public static void show(FragmentManager manager, String requestKey, String initialDate) {
        if (manager.isStateSaved() || manager.findFragmentByTag(TAG) != null) return;
        TaskDatePicker picker = new TaskDatePicker();
        Bundle args = new Bundle();
        args.putString("key", requestKey);
        args.putString(RESULT, initialDate);
        picker.setArguments(args);
        picker.show(manager, TAG);
    }

    @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle state) {
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
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.choose_date).setView(view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.set_date, (dialog, which) -> {
                    Bundle result = new Bundle(); result.putString(RESULT, selected);
                    getParentFragmentManager().setFragmentResult(requireArguments().getString("key"), result);
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
        new AlertDialog.Builder(requireContext()).setTitle(R.string.choose_month_year)
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
            GradientDrawable surface = new GradientDrawable();
            surface.setColor(ContextCompat.getColor(requireContext(), R.color.white));
            surface.setCornerRadius(24 * getResources().getDisplayMetrics().density);
            getDialog().getWindow().setBackgroundDrawable(surface);
        }
    }

    @Override public void onSaveInstanceState(@NonNull Bundle state) {
        super.onSaveInstanceState(state);
        state.putString(RESULT, selected); state.putLong("month", month.getTimeInMillis());
    }
}

package com.farwaahmad.mylist.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.R;
import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarMonthAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_WEEKDAY = 0;
    private static final int TYPE_DAY = 1;

    private final List<Cell> cells = new ArrayList<>();
    private final OnDateSelectedListener listener;
    private String selectedDate = "";

    public CalendarMonthAdapter(@NonNull OnDateSelectedListener listener) {
        this.listener = listener;
    }

    public void submitMonth(@NonNull Calendar month,
                            @NonNull List<TaskModel> tasks,
                            @NonNull String selectedDate) {
        this.selectedDate = TaskDateUtils.normalizeForStorage(selectedDate);
        cells.clear();

        Calendar first = (Calendar) month.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        normalize(first);

        addWeekdayHeaders(first.getFirstDayOfWeek());

        Map<String, Integer> taskCounts = new HashMap<>();
        for (TaskModel task : tasks) {
            Calendar due = TaskDateUtils.calendarForDue(task.getDue());
            if (due == null
                    || due.get(Calendar.YEAR) != first.get(Calendar.YEAR)
                    || due.get(Calendar.MONTH) != first.get(Calendar.MONTH)) {
                continue;
            }

            String key = TaskDateUtils.toStorageDate(
                    due.get(Calendar.YEAR),
                    due.get(Calendar.MONTH),
                    due.get(Calendar.DAY_OF_MONTH)
            );
            taskCounts.put(key, taskCounts.getOrDefault(key, 0) + 1);
        }

        int leading = (first.get(Calendar.DAY_OF_WEEK) - first.getFirstDayOfWeek() + 7) % 7;
        for (int index = 0; index < leading; index++) {
            cells.add(Cell.blank());
        }

        Calendar today = Calendar.getInstance();
        normalize(today);
        int dayCount = first.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int day = 1; day <= dayCount; day++) {
            String date = TaskDateUtils.toStorageDate(
                    first.get(Calendar.YEAR),
                    first.get(Calendar.MONTH),
                    day
            );
            boolean isToday = first.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                    && first.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                    && day == today.get(Calendar.DAY_OF_MONTH);

            cells.add(Cell.day(
                    day,
                    date,
                    taskCounts.getOrDefault(date, 0),
                    isToday
            ));
        }

        while ((cells.size() - 7) % 7 != 0) {
            cells.add(Cell.blank());
        }

        notifyDataSetChanged();
    }

    private void addWeekdayHeaders(int firstDayOfWeek) {
        String[] shortWeekdays = new DateFormatSymbols(Locale.getDefault()).getShortWeekdays();
        for (int offset = 0; offset < 7; offset++) {
            int dayOfWeek = ((firstDayOfWeek - 1 + offset) % 7) + 1;
            String label = shortWeekdays[dayOfWeek];
            if (label == null || label.trim().isEmpty()) {
                label = String.valueOf(dayOfWeek);
            }
            cells.add(Cell.weekday(label.replace(".", "")));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return cells.get(position).weekday ? TYPE_WEEKDAY : TYPE_DAY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_WEEKDAY) {
            return new WeekdayViewHolder(
                    inflater.inflate(R.layout.calendar_weekday_item, parent, false)
            );
        }
        return new DayViewHolder(
                inflater.inflate(R.layout.calendar_day_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Cell cell = cells.get(position);
        if (holder instanceof WeekdayViewHolder) {
            ((WeekdayViewHolder) holder).label.setText(cell.label);
            return;
        }

        DayViewHolder dayHolder = (DayViewHolder) holder;
        if (cell.blank) {
            dayHolder.itemView.setVisibility(View.INVISIBLE);
            dayHolder.itemView.setOnClickListener(null);
            return;
        }

        dayHolder.itemView.setVisibility(View.VISIBLE);
        dayHolder.dayNumber.setText(String.valueOf(cell.day));
        dayHolder.dayNumber.setTypeface(
                null,
                cell.today ? Typeface.BOLD : Typeface.NORMAL
        );

        if (cell.taskCount > 0) {
            dayHolder.taskCount.setVisibility(View.VISIBLE);
            dayHolder.taskCount.setText(cell.taskCount > 9 ? "9+" : String.valueOf(cell.taskCount));
        } else {
            dayHolder.taskCount.setVisibility(View.INVISIBLE);
            dayHolder.taskCount.setText("");
        }

        boolean selected = cell.storageDate.equals(selectedDate);
        if (selected) {
            dayHolder.itemView.setBackgroundResource(R.drawable.bg_calendar_day_selected);
        } else if (cell.today) {
            dayHolder.itemView.setBackgroundResource(R.drawable.bg_calendar_day_today);
        } else {
            dayHolder.itemView.setBackgroundResource(android.R.color.transparent);
        }

        Calendar date = TaskDateUtils.calendarForDue(cell.storageDate);
        if (date != null) {
            String description = new SimpleDateFormat(
                    "EEEE, d MMMM",
                    Locale.getDefault()
            ).format(date.getTime());
            if (cell.taskCount == 1) {
                description += ", 1 task";
            } else if (cell.taskCount > 1) {
                description += ", " + cell.taskCount + " tasks";
            }
            dayHolder.itemView.setContentDescription(description);
        }

        dayHolder.itemView.setOnClickListener(v ->
                listener.onDateSelected(cell.storageDate)
        );
    }

    @Override
    public int getItemCount() {
        return cells.size();
    }

    private static void normalize(@NonNull Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static class Cell {
        final boolean weekday;
        final boolean blank;
        final String label;
        final int day;
        final String storageDate;
        final int taskCount;
        final boolean today;

        private Cell(boolean weekday,
                     boolean blank,
                     String label,
                     int day,
                     String storageDate,
                     int taskCount,
                     boolean today) {
            this.weekday = weekday;
            this.blank = blank;
            this.label = label;
            this.day = day;
            this.storageDate = storageDate;
            this.taskCount = taskCount;
            this.today = today;
        }

        static Cell weekday(@NonNull String label) {
            return new Cell(true, false, label, 0, "", 0, false);
        }

        static Cell blank() {
            return new Cell(false, true, "", 0, "", 0, false);
        }

        static Cell day(int day,
                        @NonNull String storageDate,
                        int taskCount,
                        boolean today) {
            return new Cell(false, false, "", day, storageDate, taskCount, today);
        }
    }

    private static class WeekdayViewHolder extends RecyclerView.ViewHolder {
        final TextView label;

        WeekdayViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tvWeekday);
        }
    }

    private static class DayViewHolder extends RecyclerView.ViewHolder {
        final TextView dayNumber;
        final TextView taskCount;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.tvDayNumber);
            taskCount = itemView.findViewById(R.id.tvTaskCount);
        }
    }

    public interface OnDateSelectedListener {
        void onDateSelected(@NonNull String storageDate);
    }
}

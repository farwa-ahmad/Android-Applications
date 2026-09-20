package com.farwaahmad.mylist.adapter;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
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
    private final boolean showTaskCounts;

    public CalendarMonthAdapter(@NonNull OnDateSelectedListener listener) {
        this(listener, true);
    }

    public CalendarMonthAdapter(@NonNull OnDateSelectedListener listener, boolean showTaskCounts) {
        this.listener = listener;
        this.showTaskCounts = showTaskCounts;
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
            if (task.getStatus() != 0) {
                continue;
            }

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
                    isToday,
                    TaskDateUtils.isPastDate(date)
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
            WeekdayViewHolder weekdayHolder = (WeekdayViewHolder) holder;
            weekdayHolder.label.setText(cell.label);
            if (!showTaskCounts) {
                ViewGroup.LayoutParams params = weekdayHolder.itemView.getLayoutParams();
                params.height = dpToPx(weekdayHolder.itemView, 24);
                weekdayHolder.itemView.setLayoutParams(params);
            }
            return;
        }

        DayViewHolder dayHolder = (DayViewHolder) holder;
        if (cell.blank) {
            dayHolder.itemView.setVisibility(View.INVISIBLE);
            dayHolder.itemView.setOnClickListener(null);
            return;
        }

        dayHolder.itemView.setVisibility(View.VISIBLE);

        if (!showTaskCounts) {
            ViewGroup.LayoutParams itemParams = dayHolder.itemView.getLayoutParams();
            itemParams.height = dpToPx(dayHolder.itemView, 44);
            dayHolder.itemView.setLayoutParams(itemParams);

            ViewGroup.LayoutParams numberParams = dayHolder.dayNumber.getLayoutParams();
            numberParams.width = dpToPx(dayHolder.dayNumber, 36);
            numberParams.height = dpToPx(dayHolder.dayNumber, 36);
            dayHolder.dayNumber.setLayoutParams(numberParams);
        }

        dayHolder.dayNumber.setText(String.valueOf(cell.day));
        dayHolder.dayNumber.setTypeface(
                null,
                cell.today ? Typeface.BOLD : Typeface.NORMAL
        );

        boolean selected = cell.storageDate.equals(selectedDate);
        boolean todaySelected = selected && cell.today;

        // Both calendar surfaces use a true circle around the date number.
        // The Schedule tab adds task dots; the date picker does not.
        dayHolder.itemView.setBackgroundResource(android.R.color.transparent);

        if (todaySelected) {
            dayHolder.dayNumber.setBackgroundResource(R.drawable.bg_calendar_day_today_selected);
        } else if (selected) {
            dayHolder.dayNumber.setBackgroundResource(
                    showTaskCounts
                            ? R.drawable.bg_calendar_day_selected_schedule
                            : R.drawable.bg_calendar_day_selected
            );
        } else if (cell.today) {
            dayHolder.dayNumber.setBackgroundResource(
                    showTaskCounts
                            ? R.drawable.bg_calendar_day_today_schedule
                            : R.drawable.bg_calendar_day_today
            );
        } else {
            dayHolder.dayNumber.setBackgroundResource(android.R.color.transparent);
        }
        dayHolder.itemView.setSelected(selected);

        int dayTextColor;
        if (selected) {
            dayTextColor = R.color.white;
        } else if (cell.past) {
            dayTextColor = R.color.task_circle_unchecked;
        } else {
            dayTextColor = R.color.secondary;
        }
        dayHolder.dayNumber.setTextColor(
                ContextCompat.getColor(
                        holder.itemView.getContext(),
                        dayTextColor
                )
        );

        if (!showTaskCounts) {
            dayHolder.taskDots.setVisibility(View.GONE);
        } else if (cell.taskCount == 0) {
            dayHolder.taskDots.setVisibility(View.INVISIBLE);
        } else {
            dayHolder.taskDots.setVisibility(View.VISIBLE);
            int dotCount = Math.min(cell.taskCount, 3);
            dayHolder.dotOne.setVisibility(dotCount >= 1 ? View.VISIBLE : View.GONE);
            dayHolder.dotTwo.setVisibility(dotCount >= 2 ? View.VISIBLE : View.GONE);
            dayHolder.dotThree.setVisibility(dotCount >= 3 ? View.VISIBLE : View.GONE);

            int dotColor = cell.past
                    ? R.color.delete_color
                    : (cell.today ? R.color.primary : R.color.due_text);
            ColorStateList tint = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.getContext(), dotColor)
            );
            ViewCompat.setBackgroundTintList(dayHolder.dotOne, tint);
            ViewCompat.setBackgroundTintList(dayHolder.dotTwo, tint);
            ViewCompat.setBackgroundTintList(dayHolder.dotThree, tint);
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

    private static int dpToPx(@NonNull View view, int dp) {
        return Math.round(dp * view.getResources().getDisplayMetrics().density);
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
        final boolean past;

        private Cell(boolean weekday,
                     boolean blank,
                     String label,
                     int day,
                     String storageDate,
                     int taskCount,
                     boolean today,
                     boolean past) {
            this.weekday = weekday;
            this.blank = blank;
            this.label = label;
            this.day = day;
            this.storageDate = storageDate;
            this.taskCount = taskCount;
            this.today = today;
            this.past = past;
        }

        static Cell weekday(@NonNull String label) {
            return new Cell(true, false, label, 0, "", 0, false, false);
        }

        static Cell blank() {
            return new Cell(false, true, "", 0, "", 0, false, false);
        }

        static Cell day(int day,
                        @NonNull String storageDate,
                        int taskCount,
                        boolean today,
                        boolean past) {
            return new Cell(false, false, "", day, storageDate, taskCount, today, past);
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
        final View taskDots;
        final View dotOne;
        final View dotTwo;
        final View dotThree;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.tvDayNumber);
            taskDots = itemView.findViewById(R.id.taskDots);
            dotOne = itemView.findViewById(R.id.taskDotOne);
            dotTwo = itemView.findViewById(R.id.taskDotTwo);
            dotThree = itemView.findViewById(R.id.taskDotThree);
        }
    }

    public interface OnDateSelectedListener {
        void onDateSelected(@NonNull String storageDate);
    }
}

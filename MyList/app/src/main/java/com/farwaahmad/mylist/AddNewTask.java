package com.farwaahmad.mylist;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farwaahmad.mylist.databinding.AddTaskLayoutBinding;
import com.farwaahmad.mylist.util.TaskDateUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private AddTaskLayoutBinding binding;
    private String dueDate = "";
    private String dueTime = "";
    private boolean newTaskCreated;
    private TaskSaveListener taskSaveListener;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AddTaskLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvSheetTitle.setText(R.string.new_task);
        binding.btnSave.setText(R.string.add_task);

        binding.btnToday.setCheckable(true);
        binding.btnTomorrow.setCheckable(true);
        binding.btnPickDate.setCheckable(true);
        binding.btnPickTime.setCheckable(true);

        updateDueDateUi();
        updateSaveButtonState();

        binding.etTaskText.addTextChangedListener(new SimpleTextWatcher(this::updateSaveButtonState));
        binding.etTaskText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && binding.btnSave.isEnabled()) {
                saveTask();
                return true;
            }
            return false;
        });

        binding.btnToday.setOnClickListener(v -> {
            String today = storageDateForOffset(0);
            if (today.equals(dueDate)) {
                dueDate = "";
                dueTime = "";
            } else {
                dueDate = today;
            }
            updateDueDateUi();
        });

        binding.btnTomorrow.setOnClickListener(v -> {
            String tomorrow = storageDateForOffset(1);
            if (tomorrow.equals(dueDate)) {
                dueDate = "";
                dueTime = "";
            } else {
                dueDate = tomorrow;
            }
            updateDueDateUi();
        });

        binding.btnPickDate.setOnClickListener(v -> {
            String today = storageDateForOffset(0);
            String tomorrow = storageDateForOffset(1);
            boolean customDateSelected = !dueDate.isEmpty()
                    && !today.equals(dueDate)
                    && !tomorrow.equals(dueDate);

            if (customDateSelected) {
                dueDate = "";
                dueTime = "";
                updateDueDateUi();
            } else {
                showDatePicker();
            }
        });

        binding.btnPickTime.setOnClickListener(v -> {
            if (dueDate.isEmpty()) {
                return;
            }

            if (!dueTime.isEmpty()) {
                dueTime = "";
                updateDueTimeUi();
            } else {
                showTimePicker();
            }
        });

        binding.btnSave.setOnClickListener(v -> saveTask());

        binding.etTaskText.requestFocus();
        binding.etTaskText.postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }
            InputMethodManager inputMethodManager =
                    (InputMethodManager) requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(
                    binding.etTaskText,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }, 180);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        View bottomSheet =
                getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        Window window = getDialog().getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void showDatePicker() {
        Calendar calendar = TaskDateUtils.calendarForDue(dueDate);
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (picker, selectedYear, selectedMonth, dayOfMonth) -> {
                    dueDate = TaskDateUtils.toStorageDate(
                            selectedYear,
                            selectedMonth,
                            dayOfMonth
                    );
                    updateDueDateUi();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.setOnCancelListener(dialog -> updateDueDateUi());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        if (dueDate.isEmpty()) {
            return;
        }

        Calendar time = TaskDateUtils.calendarForDueTime(dueTime);
        if (time == null) {
            time = Calendar.getInstance();
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (picker, hourOfDay, minute) -> {
                    dueTime = TaskDateUtils.toStorageTime(hourOfDay, minute);
                    updateDueTimeUi();
                },
                time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE),
                android.text.format.DateFormat.is24HourFormat(requireContext())
        );
        timePickerDialog.show();
    }

    private void updateDueDateUi() {
        boolean hasDueDate = !dueDate.isEmpty();

        String today = storageDateForOffset(0);
        String tomorrow = storageDateForOffset(1);
        boolean isToday = today.equals(dueDate);
        boolean isTomorrow = tomorrow.equals(dueDate);
        boolean isCustomDate = hasDueDate && !isToday && !isTomorrow;

        binding.btnToday.setChecked(isToday);
        binding.btnTomorrow.setChecked(isTomorrow);
        binding.btnPickDate.setChecked(isCustomDate);
        binding.btnPickDate.setText(
                isCustomDate
                        ? TaskDateUtils.formatForDisplay(requireContext(), dueDate)
                        : getString(R.string.pick_date)
        );

        updateDueTimeUi();
    }

    private void updateDueTimeUi() {
        boolean hasDueDate = !dueDate.isEmpty();
        if (!hasDueDate) {
            dueTime = "";
        }

        binding.timeOptionsRow.setVisibility(hasDueDate ? View.VISIBLE : View.GONE);

        boolean hasTime = !dueTime.isEmpty();
        binding.btnPickTime.setChecked(hasTime);
        binding.btnPickTime.setText(
                hasTime
                        ? TaskDateUtils.formatTimeForDisplay(requireContext(), dueTime)
                        : getString(R.string.pick_time)
        );
    }

    @NonNull
    private String storageDateForOffset(int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, dayOffset);
        return TaskDateUtils.toStorageDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    private void saveTask() {
        if (binding == null || taskSaveListener == null) {
            return;
        }

        String taskText = binding.etTaskText.getText() == null
                ? ""
                : binding.etTaskText.getText().toString().trim();

        if (taskText.isEmpty()) {
            binding.taskInputLayout.setError(getString(R.string.no_task_entered));
            return;
        }

        binding.taskInputLayout.setError(null);
        setSaving(true);

        taskSaveListener.onTaskSaveRequested(
                taskText,
                dueDate,
                dueTime,
                new SaveCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) {
                            return;
                        }

                        newTaskCreated = true;
                        Toast.makeText(
                                requireContext(),
                                R.string.task_saved,
                                Toast.LENGTH_SHORT
                        ).show();
                        dismiss();
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        if (!isAdded() || binding == null) {
                            return;
                        }

                        setSaving(false);
                        Toast.makeText(
                                requireContext(),
                                R.string.save_task_error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setSaving(boolean saving) {
        binding.btnSave.setEnabled(!saving);
        binding.btnToday.setEnabled(!saving);
        binding.btnTomorrow.setEnabled(!saving);
        binding.btnPickDate.setEnabled(!saving);
        binding.btnPickTime.setEnabled(!saving);
        binding.etTaskText.setEnabled(!saving);
        binding.btnSave.setText(saving ? R.string.saving : R.string.add_task);
    }

    private void updateSaveButtonState() {
        if (binding == null) {
            return;
        }

        boolean hasText = binding.etTaskText.getText() != null
                && !binding.etTaskText.getText().toString().trim().isEmpty();
        binding.btnSave.setEnabled(hasText);
        if (hasText) {
            binding.taskInputLayout.setError(null);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof TaskSaveListener)) {
            throw new IllegalStateException("Host activity must implement TaskSaveListener");
        }
        taskSaveListener = (TaskSaveListener) context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (taskSaveListener != null) {
            taskSaveListener.onTaskSheetDismissed(newTaskCreated);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onDetach() {
        taskSaveListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    public interface TaskSaveListener {
        void onTaskSaveRequested(@NonNull String taskText,
                                 @NonNull String dueDate,
                                 @NonNull String dueTime,
                                 @NonNull SaveCallback callback);

        void onTaskSheetDismissed(boolean taskCreated);
    }

    public interface SaveCallback {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {

        private final Runnable onChanged;

        SimpleTextWatcher(@NonNull Runnable onChanged) {
            this.onChanged = onChanged;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChanged.run();
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {
        }
    }
}

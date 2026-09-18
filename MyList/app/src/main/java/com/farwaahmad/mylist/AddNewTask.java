package com.farwaahmad.mylist;

import android.app.DatePickerDialog;
import android.content.Context;
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

    private static final String ARG_ID = "id";
    private static final String ARG_TASK = "task";
    private static final String ARG_DUE = "due";

    private AddTaskLayoutBinding binding;
    private String dueDate = "";
    private String id = "";
    private boolean isUpdate;
    private TaskSaveListener taskSaveListener;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    public static AddNewTask newInstance(@NonNull String id,
                                         @NonNull String task,
                                         @NonNull String dueDate) {
        AddNewTask fragment = new AddNewTask();
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ID, id);
        arguments.putString(ARG_TASK, task);
        arguments.putString(ARG_DUE, dueDate);
        fragment.setArguments(arguments);
        return fragment;
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

        Bundle arguments = getArguments();
        if (arguments != null) {
            isUpdate = true;
            id = arguments.getString(ARG_ID, "");
            dueDate = TaskDateUtils.normalizeForStorage(
                    arguments.getString(ARG_DUE, "")
            );

            binding.tvSheetTitle.setText(R.string.edit_task);
            binding.etTaskText.setText(arguments.getString(ARG_TASK, ""));
            binding.btnSave.setText(R.string.save_task);
        } else {
            binding.tvSheetTitle.setText(R.string.new_task);
            binding.btnSave.setText(R.string.add_task);
        }

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

        binding.btnDueDate.setOnClickListener(v -> showDatePicker());
        binding.btnClearDueDate.setOnClickListener(v -> {
            dueDate = "";
            updateDueDateUi();
        });
        binding.btnSave.setOnClickListener(v -> saveTask());

        if (!isUpdate) {
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

        datePickerDialog.show();
    }

    private void updateDueDateUi() {
        boolean hasDueDate = !dueDate.isEmpty();
        binding.btnClearDueDate.setVisibility(hasDueDate ? View.VISIBLE : View.GONE);

        if (!hasDueDate) {
            binding.btnDueDate.setText(R.string.add_due_date);
            return;
        }

        binding.btnDueDate.setText(
                TaskDateUtils.formatForDisplay(requireContext(), dueDate)
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
                id,
                taskText,
                dueDate,
                isUpdate,
                new SaveCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) {
                            return;
                        }

                        Toast.makeText(
                                requireContext(),
                                isUpdate ? R.string.task_updated : R.string.task_saved,
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
        binding.btnDueDate.setEnabled(!saving);
        binding.btnClearDueDate.setEnabled(!saving);
        binding.etTaskText.setEnabled(!saving);
        binding.btnSave.setText(
                saving
                        ? R.string.saving
                        : (isUpdate ? R.string.save_task : R.string.add_task)
        );
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
        void onTaskSaveRequested(@NonNull String id,
                                 @NonNull String taskText,
                                 @NonNull String dueDate,
                                 boolean isUpdate,
                                 @NonNull SaveCallback callback);
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

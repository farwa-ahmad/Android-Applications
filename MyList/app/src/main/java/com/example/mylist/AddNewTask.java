package com.example.mylist;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mylist.databinding.AddTaskLayoutBinding;
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
            dueDate = arguments.getString(ARG_DUE, "");

            binding.etTaskText.setText(arguments.getString(ARG_TASK, ""));
            binding.tvSetDueDate.setText(
                    dueDate.isEmpty() ? getString(R.string.set_due_date) : dueDate
            );
        }

        updateSaveButtonState(binding.etTaskText.getText());

        binding.etTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButtonState(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.tvSetDueDate.setOnClickListener(v -> showDatePicker());
        binding.btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view,
                                          int selectedYear,
                                          int selectedMonth,
                                          int dayOfMonth) {
                        int displayMonth = selectedMonth + 1;
                        dueDate = dayOfMonth + "/" + displayMonth + "/" + selectedYear;
                        binding.tvSetDueDate.setText(dueDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DATE)
        );
        datePickerDialog.show();
    }

    private void saveTask() {
        String taskText = binding.etTaskText.getText().toString().trim();
        if (taskText.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_task_entered, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSave.setEnabled(false);

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
                        if (!isAdded()) {
                            return;
                        }
                        binding.btnSave.setEnabled(true);
                        String message = exception.getMessage() != null
                                ? exception.getMessage()
                                : getString(R.string.generic_error);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void updateSaveButtonState(CharSequence text) {
        boolean hasText = text != null && !text.toString().trim().isEmpty();
        binding.btnSave.setEnabled(hasText);
        binding.btnSave.setTextColor(
                ContextCompat.getColor(
                        requireContext(),
                        hasText ? R.color.primary : R.color.dark_gray
                )
        );
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
}

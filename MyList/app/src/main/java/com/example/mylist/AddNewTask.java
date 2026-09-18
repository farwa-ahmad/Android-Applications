package com.example.mylist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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

import com.example.mylist.databinding.AddTaskLayoutBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

// class for the bottom dialog option to enter tasks
public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    AddTaskLayoutBinding binding;

    private String dueDate = "";
    private String id = "";

    private Context context;

    private FirebaseFirestore firestore;

    public static AddNewTask newInstance() {
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddTaskLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        boolean isUpdate = false;
        final Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task", "");
            id = bundle.getString("id", "");
            dueDate = bundle.getString("due", "");

            binding.etTaskText.setText(task);
            binding.tvSetDueDate.setText(dueDate.isEmpty() ? getString(R.string.set_due_date) : dueDate);
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

        binding.tvSetDueDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DATE);

            DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int dayOfMonth) {
                    int displayMonth = selectedMonth + 1;
                    dueDate = dayOfMonth + "/" + displayMonth + "/" + selectedYear;
                    binding.tvSetDueDate.setText(dueDate);
                }
            }, year, month, day);
            datePickerDialog.show();
        });

        boolean finalIsUpdate = isUpdate;
        binding.btnSave.setOnClickListener(v -> {
            String taskText = binding.etTaskText.getText().toString().trim();

            if (taskText.isEmpty()) {
                Toast.makeText(context, "No task entered", Toast.LENGTH_SHORT).show();
                return;
            }

            binding.btnSave.setEnabled(false);

            if (finalIsUpdate) {
                firestore.collection("task")
                        .document(id)
                        .update("task", taskText, "due", dueDate)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Task updated", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                binding.btnSave.setEnabled(true);
                                showError(task.getException());
                            }
                        });
            } else {
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("task", taskText);
                taskMap.put("due", dueDate);
                taskMap.put("status", 0);
                taskMap.put("time", FieldValue.serverTimestamp());

                firestore.collection("task")
                        .add(taskMap)
                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Task saved", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                } else {
                                    binding.btnSave.setEnabled(true);
                                    showError(task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void updateSaveButtonState(CharSequence text) {
        boolean hasText = text != null && !text.toString().trim().isEmpty();
        binding.btnSave.setEnabled(hasText);
        binding.btnSave.setTextColor(getResources().getColor(hasText ? R.color.primary : R.color.dark_gray));
        binding.btnSave.setBackgroundColor(Color.TRANSPARENT);
    }

    private void showError(Exception exception) {
        String message = exception != null && exception.getMessage() != null
                ? exception.getMessage()
                : "Something went wrong";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}

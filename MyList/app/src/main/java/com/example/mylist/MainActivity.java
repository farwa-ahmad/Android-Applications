package com.example.mylist;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mylist.Adapters.TaskAdapter;
import com.example.mylist.Models.TaskModel;
import com.example.mylist.data.TaskRepository;
import com.example.mylist.databinding.ActivityMainBinding;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements TaskAdapter.TaskActionListener, AddNewTask.TaskSaveListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private final List<TaskModel> tasks = new ArrayList<>();
    private TaskRepository taskRepository;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        taskRepository = new TaskRepository();

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlBackground.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        binding.rvTasks.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(this, tasks, this);
        binding.rvTasks.setAdapter(taskAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(binding.rvTasks);

        binding.fabAddTask.setOnClickListener(v ->
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG)
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        startListeningForTasks();
    }

    @Override
    protected void onStop() {
        stopListeningForTasks();
        super.onStop();
    }

    private void startListeningForTasks() {
        stopListeningForTasks();

        listenerRegistration = taskRepository.listenForTasks(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> updatedTasks) {
                tasks.clear();
                tasks.addAll(updatedTasks);
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, "Could not load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopListeningForTasks() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    @Override
    public void onEditTask(@NonNull TaskModel task) {
        AddNewTask.newInstance(
                task.getId(),
                task.getTask() == null ? "" : task.getTask(),
                task.getDue() == null ? "" : task.getDue()
        ).show(getSupportFragmentManager(), AddNewTask.TAG);
    }

    @Override
    public void onDeleteTask(@NonNull TaskModel task, int adapterPosition) {
        taskRepository.deleteTask(task.getId(), new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                // The realtime listener updates the list after Firestore confirms the deletion.
            }

            @Override
            public void onError(@NonNull Exception exception) {
                taskAdapter.restoreItem(adapterPosition);
                Toast.makeText(MainActivity.this, "Could not delete task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete) {
        taskRepository.updateStatus(task.getId(), isComplete, new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                // The realtime listener keeps the UI synchronized with Firestore.
            }

            @Override
            public void onError(@NonNull Exception exception) {
                taskAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Could not update task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTaskSaveRequested(@NonNull String id,
                                    @NonNull String taskText,
                                    @NonNull String dueDate,
                                    boolean isUpdate,
                                    @NonNull AddNewTask.SaveCallback callback) {
        TaskRepository.OperationCallback repositoryCallback =
                new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
                    }
                };

        if (isUpdate) {
            taskRepository.updateTask(id, taskText, dueDate, repositoryCallback);
        } else {
            taskRepository.addTask(taskText, dueDate, repositoryCallback);
        }
    }
}

package com.farwaahmad.mylist;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.farwaahmad.mylist.adapter.TaskAdapter;
import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.data.TaskRepository;
import com.farwaahmad.mylist.databinding.ActivityMainBinding;
import com.farwaahmad.mylist.model.TaskModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements TaskAdapter.TaskActionListener,
        AddNewTask.TaskSaveListener,
        AccountBottomSheet.AccountActionListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private final List<TaskModel> tasks = new ArrayList<>();

    private AuthRepository authRepository;
    private TaskRepository taskRepository;
    private ListenerRegistration listenerRegistration;
    private boolean activityStarted;
    private boolean signInInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository();

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

        binding.fabAddTask.setEnabled(false);
        binding.fabAddTask.setOnClickListener(v ->
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG)
        );

        binding.btnAccount.setOnClickListener(v -> openAccountSheet());
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityStarted = true;
        ensureSignedIn();
    }

    @Override
    protected void onStop() {
        activityStarted = false;
        stopListeningForTasks();
        super.onStop();
    }

    private void ensureSignedIn() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            startListeningForTasks(currentUser);
            return;
        }

        if (signInInProgress) return;

        signInInProgress = true;
        authRepository.ensureUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                signInInProgress = false;
                if (activityStarted) {
                    startListeningForTasks(user);
                }
            }

            @Override
            public void onError(@NonNull Exception exception) {
                signInInProgress = false;
                if (!activityStarted) return;
                binding.fabAddTask.setEnabled(false);
                Toast.makeText(MainActivity.this, R.string.auth_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startListeningForTasks(@NonNull FirebaseUser user) {
        stopListeningForTasks();

        taskRepository = new TaskRepository(user.getUid());
        binding.fabAddTask.setEnabled(true);

        listenerRegistration = taskRepository.listenForTasks(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> updatedTasks) {
                tasks.clear();
                tasks.addAll(updatedTasks);
                taskAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                Toast.makeText(MainActivity.this, R.string.load_tasks_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void stopListeningForTasks() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void openAccountSheet() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.auth_error, Toast.LENGTH_SHORT).show();
            return;
        }

        AccountBottomSheet.newInstance(
                user.isAnonymous(),
                user.getEmail(),
                !tasks.isEmpty()
        ).show(getSupportFragmentManager(), AccountBottomSheet.TAG);
    }

    private void resetForIdentityChange(@NonNull FirebaseUser user) {
        stopListeningForTasks();
        tasks.clear();
        taskAdapter.notifyDataSetChanged();
        startListeningForTasks(user);
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
        if (taskRepository == null) return;

        taskRepository.deleteTask(task.getId(), new TaskRepository.OperationCallback() {
            @Override public void onSuccess() {}

            @Override
            public void onError(@NonNull Exception exception) {
                taskAdapter.restoreItem(adapterPosition);
                Toast.makeText(MainActivity.this, R.string.delete_task_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete) {
        if (taskRepository == null) return;

        taskRepository.updateStatus(task.getId(), isComplete, new TaskRepository.OperationCallback() {
            @Override public void onSuccess() {}

            @Override
            public void onError(@NonNull Exception exception) {
                taskAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, R.string.update_task_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTaskSaveRequested(@NonNull String id,
                                    @NonNull String taskText,
                                    @NonNull String dueDate,
                                    boolean isUpdate,
                                    @NonNull AddNewTask.SaveCallback callback) {
        if (taskRepository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        TaskRepository.OperationCallback repositoryCallback =
                new TaskRepository.OperationCallback() {
                    @Override public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
                    }
                };

        if (isUpdate) {
            taskRepository.updateTask(id, taskText, dueDate, repositoryCallback);
        } else {
            taskRepository.addTask(taskText, dueDate, repositoryCallback);
        }
    }

    @Override
    public void onBackupRequested(@NonNull String email,
                                  @NonNull String password,
                                  @NonNull AccountBottomSheet.ActionCallback callback) {
        authRepository.linkAnonymousWithEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                callback.onSuccess();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void onRestoreRequested(@NonNull String email,
                                   @NonNull String password,
                                   @NonNull AccountBottomSheet.ActionCallback callback) {
        if (!tasks.isEmpty()) {
            callback.onError(new IllegalStateException(getString(R.string.restore_blocked_with_tasks)));
            return;
        }

        stopListeningForTasks();
        authRepository.restoreEmailAccount(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                resetForIdentityChange(user);
                callback.onSuccess();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                ensureSignedIn();
                callback.onError(exception);
            }
        });
    }

    @Override
    public void onSignOutRequested() {
        stopListeningForTasks();
        tasks.clear();
        taskAdapter.notifyDataSetChanged();
        authRepository.signOut();
        ensureSignedIn();
    }

    @Override
    public void onDeleteRequested(@Nullable String password,
                                  @NonNull AccountBottomSheet.ActionCallback callback) {
        if (taskRepository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        taskRepository.deleteAllTasks(new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                authRepository.deleteCurrentAccount(password, new AuthRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        stopListeningForTasks();
                        tasks.clear();
                        taskAdapter.notifyDataSetChanged();
                        taskRepository = null;
                        callback.onSuccess();
                        ensureSignedIn();
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
                    }
                });
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }
}

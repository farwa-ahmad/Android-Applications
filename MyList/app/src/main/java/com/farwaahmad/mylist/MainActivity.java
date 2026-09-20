package com.farwaahmad.mylist;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.adapter.TaskAdapter;
import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.data.TaskRepository;
import com.farwaahmad.mylist.databinding.ActivityMainBinding;
import com.farwaahmad.mylist.model.TaskModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private LaunchManager launchManager;
    private boolean activityStarted;
    private boolean signInInProgress;
    private boolean initialStateReady;
    private static final int SWIPE_HINT_MAX_RETRIES = 20;
    private static final long SWIPE_HINT_RETRY_DELAY_MS = 150L;

    private boolean pendingSwipeHintAfterSheetCloses;
    private boolean swipeHintRetryScheduled;
    private int swipeHintRetryCount;
    private String pendingSwipeHintTaskId;

    private final Runnable swipeHintRetryRunnable = () -> {
        swipeHintRetryScheduled = false;
        showPendingSwipeHint();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    systemBars.top,
                    view.getPaddingRight(),
                    systemBars.bottom
            );
            return windowInsets;
        });

        authRepository = new AuthRepository();
        launchManager = new LaunchManager(this);

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlBackground.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        binding.rvTasks.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(this, this);
        binding.rvTasks.setAdapter(taskAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(binding.rvTasks);

        binding.fabAddTask.setEnabled(false);
        binding.btnAccount.setEnabled(false);

        binding.fabAddTask.setOnClickListener(v ->
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG)
        );

        binding.btnAccount.setOnClickListener(v -> openAccountSheet());
        binding.btnRetry.setOnClickListener(v -> retryLoading());

        updateCurrentDate();
        consumeStartupState();
    }

    private void consumeStartupState() {
        StartupTaskStore.State startupState = StartupTaskStore.consume();
        if (startupState == null) {
            showLoading(true);
            return;
        }

        initialStateReady = true;

        if (startupState.isLoadFailed()) {
            showConnectionError();
            return;
        }

        tasks.clear();
        tasks.addAll(startupState.getTasks());
        taskAdapter.submitTasks(tasks);
        showContentState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentDate();
    }

    private void updateCurrentDate() {
        binding.tvCurrentDate.setText(
                new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
                        .format(new Date())
        );
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
        cancelSwipeHintRetry();
        stopListeningForTasks();
        super.onStop();
    }

    private void ensureSignedIn() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            startListeningForTasks(currentUser);
            return;
        }

        if (signInInProgress) {
            return;
        }

        if (!initialStateReady) {
            showLoading(true);
        }

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
                if (!activityStarted) {
                    return;
                }

                binding.fabAddTask.setEnabled(false);
                binding.btnAccount.setEnabled(false);
                initialStateReady = true;
                showConnectionError();
                Toast.makeText(MainActivity.this, R.string.auth_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startListeningForTasks(@NonNull FirebaseUser user) {
        stopListeningForTasks();

        if (!initialStateReady) {
            showLoading(true);
        }

        taskRepository = new TaskRepository(user.getUid());
        binding.fabAddTask.setEnabled(true);
        binding.btnAccount.setEnabled(true);

        listenerRegistration = taskRepository.listenForTasks(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> updatedTasks) {
                tasks.clear();
                tasks.addAll(updatedTasks);
                taskAdapter.submitTasks(tasks);
                initialStateReady = true;
                showContentState();
                showPendingSwipeHint();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                initialStateReady = true;
                if (tasks.isEmpty()) {
                    showConnectionError();
                } else {
                    showContentState();
                }
                Toast.makeText(
                        MainActivity.this,
                        R.string.load_tasks_error,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void retryLoading() {
        initialStateReady = false;
        showLoading(true);
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            startListeningForTasks(user);
        } else {
            ensureSignedIn();
        }
    }

    private void showLoading(boolean loading) {
        binding.loadingState.setVisibility(
                loading ? android.view.View.VISIBLE : android.view.View.GONE
        );
        if (loading) {
            binding.emptyState.setVisibility(android.view.View.GONE);
        }
    }

    private void showContentState() {
        showLoading(false);
        binding.btnRetry.setVisibility(android.view.View.GONE);
        binding.emptyState.setVisibility(
                tasks.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE
        );
        binding.tvEmptyTitle.setText(R.string.empty_title);
        binding.tvEmptyMessage.setText(R.string.empty_message);
    }

    private void showConnectionError() {
        showLoading(false);
        binding.emptyState.setVisibility(android.view.View.VISIBLE);
        binding.tvEmptyTitle.setText(R.string.connection_error_title);
        binding.tvEmptyMessage.setText(R.string.connection_error_message);
        binding.btnRetry.setVisibility(android.view.View.VISIBLE);
    }

    private void showPendingSwipeHint() {
        if (!pendingSwipeHintAfterSheetCloses
                || pendingSwipeHintTaskId == null
                || !launchManager.shouldShowSwipeHint()) {
            return;
        }

        int position = taskAdapter.showSwipeHint(pendingSwipeHintTaskId);
        if (position == RecyclerView.NO_POSITION) {
            scheduleSwipeHintRetry();
            return;
        }

        cancelSwipeHintRetry();

        String taskId = pendingSwipeHintTaskId;
        pendingSwipeHintTaskId = null;
        pendingSwipeHintAfterSheetCloses = false;
        swipeHintRetryCount = 0;
        launchManager.markSwipeHintShown();

        binding.rvTasks.smoothScrollToPosition(position);
        binding.rvTasks.postDelayed(
                () -> taskAdapter.animateSwipeHint(binding.rvTasks, taskId),
                350L
        );
    }

    private void scheduleSwipeHintRetry() {
        if (swipeHintRetryScheduled || swipeHintRetryCount >= SWIPE_HINT_MAX_RETRIES) {
            return;
        }

        swipeHintRetryCount++;
        swipeHintRetryScheduled = true;
        binding.rvTasks.postDelayed(swipeHintRetryRunnable, SWIPE_HINT_RETRY_DELAY_MS);
    }

    private void cancelSwipeHintRetry() {
        if (!swipeHintRetryScheduled) {
            return;
        }

        binding.rvTasks.removeCallbacks(swipeHintRetryRunnable);
        swipeHintRetryScheduled = false;
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

        if (user.isAnonymous()) {
            showAccountSheet(user);
            return;
        }

        binding.btnAccount.setEnabled(false);
        authRepository.reloadCurrentUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser refreshedUser) {
                binding.btnAccount.setEnabled(true);
                showAccountSheet(refreshedUser);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                binding.btnAccount.setEnabled(true);
                FirebaseUser currentUser = authRepository.getCurrentUser();
                if (currentUser != null) {
                    showAccountSheet(currentUser);
                } else {
                    Toast.makeText(
                            MainActivity.this,
                            R.string.auth_error,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private void showAccountSheet(@NonNull FirebaseUser user) {
        AccountBottomSheet.newInstance(
                user.isAnonymous(),
                user.getEmail(),
                !tasks.isEmpty(),
                user.isEmailVerified()
        ).show(getSupportFragmentManager(), AccountBottomSheet.TAG);
    }

    private void resetForIdentityChange(@NonNull FirebaseUser user) {
        stopListeningForTasks();
        tasks.clear();
        taskAdapter.submitTasks(tasks);
        initialStateReady = false;
        startListeningForTasks(user);
    }

    @Override
    public void onTaskEditSaveRequested(@NonNull TaskModel task,
                                        @NonNull String taskText,
                                        @NonNull String dueDate,
                                        @NonNull String dueTime,
                                        @NonNull TaskAdapter.EditSaveCallback callback) {
        if (taskRepository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        taskRepository.updateTask(
                task.getId(),
                taskText,
                dueDate,
                dueTime,
                new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                        Toast.makeText(
                                MainActivity.this,
                                R.string.task_updated,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
                        Toast.makeText(
                                MainActivity.this,
                                R.string.save_task_error,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    public void onDeleteTask(@NonNull TaskModel task) {
        if (taskRepository == null) {
            return;
        }

        taskRepository.deleteTask(task.getId(), new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(@NonNull Exception exception) {
                Toast.makeText(
                        MainActivity.this,
                        R.string.delete_task_error,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete) {
        if (taskRepository == null) {
            return;
        }

        taskRepository.updateStatus(task.getId(), isComplete, new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(@NonNull Exception exception) {
                taskAdapter.submitTasks(tasks);
                Toast.makeText(
                        MainActivity.this,
                        R.string.update_task_error,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    @Override
    public void onTaskSaveRequested(@NonNull String taskText,
                                    @NonNull String dueDate,
                                    @NonNull String dueTime,
                                    @NonNull AddNewTask.SaveCallback callback) {
        if (taskRepository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        boolean shouldTeachSwipe = launchManager.shouldShowSwipeHint();
        taskRepository.addTask(taskText, dueDate, dueTime, new TaskRepository.AddTaskCallback() {
            @Override
            public void onSuccess(@NonNull String taskId) {
                if (shouldTeachSwipe) {
                    pendingSwipeHintTaskId = taskId;
                }
                callback.onSuccess();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void onTaskSheetDismissed(boolean taskCreated) {
        if (!taskCreated || pendingSwipeHintTaskId == null) {
            return;
        }

        pendingSwipeHintAfterSheetCloses = true;
        swipeHintRetryCount = 0;
        cancelSwipeHintRetry();
        binding.rvTasks.postDelayed(this::showPendingSwipeHint, 200L);
    }

    @Override
    public void onBackupRequested(@NonNull String email,
                                  @NonNull String password,
                                  @NonNull AccountBottomSheet.BackupCallback callback) {
        authRepository.linkAnonymousWithEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                authRepository.sendVerificationEmail(new AuthRepository.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess(true);
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        // The account is already linked, so backup succeeded even if
                        // the verification email could not be sent right now.
                        callback.onSuccess(false);
                    }
                });
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void onPasswordResetRequested(@NonNull String email,
                                         @NonNull AccountBottomSheet.ActionCallback callback) {
        authRepository.sendPasswordResetEmail(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }

    @Override
    public void onVerificationEmailRequested(
            @NonNull AccountBottomSheet.ActionCallback callback) {
        authRepository.sendVerificationEmail(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
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
            callback.onError(new IllegalStateException(
                    getString(R.string.restore_blocked_with_tasks)
            ));
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
        taskAdapter.submitTasks(tasks);
        initialStateReady = false;
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

        authRepository.reauthenticateForDeletion(password, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                taskRepository.deleteAllTasks(new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        authRepository.deleteCurrentAccount(new AuthRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                stopListeningForTasks();
                                tasks.clear();
                                taskAdapter.submitTasks(tasks);
                                taskRepository = null;
                                initialStateReady = false;
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

            @Override
            public void onError(@NonNull Exception exception) {
                callback.onError(exception);
            }
        });
    }
}

package com.farwaahmad.mylist;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.adapter.CalendarMonthAdapter;
import com.farwaahmad.mylist.adapter.ScheduleTaskAdapter;
import com.farwaahmad.mylist.adapter.TaskAdapter;
import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.data.TaskRepository;
import com.farwaahmad.mylist.databinding.ActivityMainBinding;
import com.farwaahmad.mylist.model.TaskModel;
import com.farwaahmad.mylist.util.TaskDateUtils;
import com.farwaahmad.mylist.viewmodel.MainViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements TaskAdapter.TaskActionListener,
        AddNewTask.TaskSaveListener,
        AccountBottomSheet.AccountActionListener {

    private ActivityMainBinding binding;
    private TaskAdapter taskAdapter;
    private CalendarMonthAdapter calendarMonthAdapter;
    private ScheduleTaskAdapter scheduleTaskAdapter;
    private final List<TaskModel> tasks = new ArrayList<>();
    private final Calendar visibleMonth = Calendar.getInstance();
    private String selectedScheduleDate;
    private boolean scheduleMode;

    private MainViewModel viewModel;
    private AuthRepository authRepository;
    private LaunchManager launchManager;
    private static final int SWIPE_HINT_MAX_RETRIES = 20;
    private static final long SWIPE_HINT_RETRY_DELAY_MS = 150L;
    private static final String EDIT_TASK_DATE_REQUEST = "editTaskDate";
    private static final String EDIT_TASK_TIME_REQUEST = "editTaskTime";

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

        int taskListBaseBottomPadding = binding.rvTasks.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            view.setPadding(
                    view.getPaddingLeft(),
                    systemBars.top,
                    view.getPaddingRight(),
                    systemBars.bottom
            );

            int keyboardInset = Math.max(0, ime.bottom - systemBars.bottom);
            binding.rvTasks.setPadding(
                    binding.rvTasks.getPaddingLeft(),
                    binding.rvTasks.getPaddingTop(),
                    binding.rvTasks.getPaddingRight(),
                    taskListBaseBottomPadding + keyboardInset
            );

            return windowInsets;
        });

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        authRepository = viewModel.getAuthRepository();
        launchManager = new LaunchManager(this);

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlBackground.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        binding.rvTasks.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(this, this);
        binding.rvTasks.setAdapter(taskAdapter);
        registerEditPickerResults();

        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);
        Calendar today = Calendar.getInstance();
        selectedScheduleDate = TaskDateUtils.toStorageDate(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        calendarMonthAdapter = new CalendarMonthAdapter(this::selectScheduleDate);
        binding.rvCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        binding.rvCalendar.setAdapter(calendarMonthAdapter);

        scheduleTaskAdapter = new ScheduleTaskAdapter(
                this::onTaskStatusChanged,
                task -> {
                    setScheduleMode(false);
                    binding.rvTasks.post(() -> taskAdapter.requestEdit(task));
                }
        );
        binding.rvScheduleTasks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvScheduleTasks.setAdapter(scheduleTaskAdapter);

        binding.btnViewList.setChecked(true);
        binding.btnViewSchedule.setChecked(false);
        binding.btnViewList.setOnClickListener(v -> setScheduleMode(false));
        binding.btnViewSchedule.setOnClickListener(v -> setScheduleMode(true));

        binding.btnPreviousMonth.setOnClickListener(v -> moveScheduleMonth(-1));
        binding.btnNextMonth.setOnClickListener(v -> moveScheduleMonth(1));

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
        observeViewModel();
        viewModel.initialize(StartupTaskStore.consume());
    }

    private void registerEditPickerResults() {
        getSupportFragmentManager().setFragmentResultListener(
                EDIT_TASK_DATE_REQUEST,
                this,
                (key, result) -> taskAdapter.onDatePickerResult(
                        result.getBoolean(TaskDatePicker.RESULT_CONFIRMED, false),
                        result.getString(TaskDatePicker.RESULT)
                )
        );

        getSupportFragmentManager().setFragmentResultListener(
                EDIT_TASK_TIME_REQUEST,
                this,
                (key, result) -> taskAdapter.onTimePickerResult(
                        result.getString(TaskTimePicker.RESULT)
                )
        );
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(this, this::renderUiState);
        viewModel.getErrorEvents().observe(this, event -> {
            if (event == null) {
                return;
            }

            MainViewModel.ErrorType error = event.getContentIfNotHandled();
            if (error == null) {
                return;
            }

            int message = error == MainViewModel.ErrorType.AUTH
                    ? R.string.auth_error
                    : R.string.load_tasks_error;
            int duration = error == MainViewModel.ErrorType.AUTH
                    ? Toast.LENGTH_LONG
                    : Toast.LENGTH_SHORT;
            Toast.makeText(this, message, duration).show();
        });
    }

    private void renderUiState(@NonNull MainViewModel.UiState state) {
        tasks.clear();
        tasks.addAll(state.getTasks());
        taskAdapter.submitTasks(tasks);

        binding.fabAddTask.setEnabled(state.areControlsEnabled());
        binding.btnAccount.setEnabled(state.areControlsEnabled());

        if (state.isLoading()) {
            binding.btnRetry.setVisibility(View.GONE);
            showLoading(true);
            return;
        }

        if (state.hasConnectionError()) {
            showConnectionError();
            return;
        }

        showContentState();
        showPendingSwipeHint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentDate();
        refreshScheduleView();
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
        viewModel.start();
    }

    @Override
    protected void onStop() {
        cancelSwipeHintRetry();
        viewModel.stop();
        super.onStop();
    }

    private void retryLoading() {
        viewModel.retry();
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
        refreshScheduleView();
        binding.btnRetry.setVisibility(android.view.View.GONE);
        binding.emptyState.setVisibility(
                !scheduleMode && tasks.isEmpty()
                        ? android.view.View.VISIBLE
                        : android.view.View.GONE
        );
        binding.tvEmptyTitle.setText(R.string.empty_title);
        binding.tvEmptyMessage.setText(R.string.empty_message);
    }

    private void setScheduleMode(boolean enabled) {
        scheduleMode = enabled;

        // Keep this custom segmented control mutually exclusive without letting
        // MaterialButtonToggleGroup reshape the inner corners.
        binding.btnViewList.setChecked(!enabled);
        binding.btnViewSchedule.setChecked(enabled);
        binding.btnViewList.setSelected(!enabled);
        binding.btnViewSchedule.setSelected(enabled);

        binding.rvTasks.setVisibility(enabled ? View.GONE : View.VISIBLE);
        binding.scheduleContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);

        if (enabled) {
            taskAdapter.hideSwipeHint();
            cancelSwipeHintRetry();
        }

        showContentState();
    }

    private void selectScheduleDate(@NonNull String storageDate) {
        Calendar selected = TaskDateUtils.calendarForDue(storageDate);
        if (selected == null) {
            return;
        }

        selectedScheduleDate = TaskDateUtils.toStorageDate(
                selected.get(Calendar.YEAR),
                selected.get(Calendar.MONTH),
                selected.get(Calendar.DAY_OF_MONTH)
        );
        visibleMonth.set(
                selected.get(Calendar.YEAR),
                selected.get(Calendar.MONTH),
                1
        );
        refreshScheduleView();
    }

    private void moveScheduleMonth(int monthOffset) {
        Calendar selected = TaskDateUtils.calendarForDue(selectedScheduleDate);
        int preferredDay = selected == null
                ? 1
                : selected.get(Calendar.DAY_OF_MONTH);

        visibleMonth.add(Calendar.MONTH, monthOffset);
        visibleMonth.set(Calendar.DAY_OF_MONTH, 1);

        Calendar nextSelection = (Calendar) visibleMonth.clone();
        nextSelection.set(
                Calendar.DAY_OF_MONTH,
                Math.min(
                        preferredDay,
                        nextSelection.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
        );

        selectedScheduleDate = TaskDateUtils.toStorageDate(
                nextSelection.get(Calendar.YEAR),
                nextSelection.get(Calendar.MONTH),
                nextSelection.get(Calendar.DAY_OF_MONTH)
        );
        refreshScheduleView();
    }

    private void refreshScheduleView() {
        if (calendarMonthAdapter == null || scheduleTaskAdapter == null) {
            return;
        }

        binding.tvMonthTitle.setText(
                new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                        .format(visibleMonth.getTime())
        );

        calendarMonthAdapter.submitMonth(
                visibleMonth,
                tasks,
                selectedScheduleDate
        );
        scheduleTaskAdapter.submitTasks(tasks, selectedScheduleDate);

        Calendar selected = TaskDateUtils.calendarForDue(selectedScheduleDate);
        if (selected != null) {
            binding.tvSelectedDate.setText(
                    new SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
                            .format(selected.getTime())
            );
        } else {
            binding.tvSelectedDate.setText("");
        }

        int openCount = scheduleTaskAdapter.getOpenTaskCount();
        binding.tvSelectedTaskCount.setText(
                openCount == 0
                        ? getString(R.string.no_tasks)
                        : getResources().getQuantityString(
                                R.plurals.scheduled_task_count,
                                openCount,
                                openCount
                        )
        );

        int totalCount = scheduleTaskAdapter.getTaskCount();
        binding.tvNoScheduledTasks.setVisibility(
                totalCount == 0 ? View.VISIBLE : View.GONE
        );
        binding.rvScheduleTasks.setVisibility(
                totalCount == 0 ? View.GONE : View.VISIBLE
        );
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
        viewModel.pauseTaskListening();
    }

    private void openAccountSheet() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user == null) {
            showStatusMessage(R.string.auth_error);
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
                    showStatusMessage(R.string.auth_error);
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
        viewModel.resetForIdentityChange(user);
    }

    @Override
    public void onTaskEditSaveRequested(@NonNull TaskModel task,
                                        @NonNull String taskText,
                                        @NonNull String dueDate,
                                        @NonNull String dueTime,
                                        @NonNull TaskAdapter.EditSaveCallback callback) {
        TaskRepository repository = viewModel.getTaskRepository();
        if (repository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        repository.updateTask(
                task.getId(),
                taskText,
                dueDate,
                dueTime,
                new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                        showStatusMessage(R.string.task_updated);
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        callback.onError(exception);
                        showStatusMessage(R.string.save_task_error);
                    }
                }
        );
    }

    @Override
    public void onDeleteTask(@NonNull TaskModel task) {
        TaskRepository repository = viewModel.getTaskRepository();
        if (repository == null) {
            taskAdapter.submitTasks(tasks);
            return;
        }

        repository.deleteTask(task.getId(), new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(@NonNull Exception exception) {
                showStatusMessage(R.string.delete_task_error);
            }
        });

        Snackbar snackbar = Snackbar.make(
                binding.getRoot(),
                R.string.task_deleted,
                Snackbar.LENGTH_LONG
        );

        snackbar.setAction(R.string.undo, view ->
                repository.restoreTask(task, new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        showStatusMessage(R.string.restore_task_error);
                    }
                })
        );

        styleStatusSnackbar(snackbar);
        snackbar.show();
    }

    private void showStatusMessage(int messageRes) {
        Snackbar snackbar = Snackbar.make(binding.getRoot(), messageRes, Snackbar.LENGTH_LONG);
        styleStatusSnackbar(snackbar);
        snackbar.show();
    }

    private void styleStatusSnackbar(@NonNull Snackbar snackbar) {
        int white = getColor(R.color.white);
        snackbar.setTextColor(white);
        snackbar.setActionTextColor(white);

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundResource(R.drawable.bg_status_pill);
        snackbarView.setElevation(dpToPx(5));
        snackbarView.setMinimumWidth(0);

        TextView message =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        message.setMaxLines(1);

        TextView action =
                snackbarView.findViewById(com.google.android.material.R.id.snackbar_action);
        action.setAllCaps(false);

        ViewGroup.LayoutParams rawParams = snackbarView.getLayoutParams();
        rawParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (rawParams instanceof FrameLayout.LayoutParams) {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) rawParams;
            // Match the app's existing Toast-style status messages: compact and
            // centered near the bottom, while keeping Undo tappable.
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.setMarginStart(0);
            params.setMarginEnd(0);
            params.bottomMargin = Math.max(params.bottomMargin, dpToPx(64));
            snackbarView.setLayoutParams(params);
        } else {
            snackbarView.setLayoutParams(rawParams);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public boolean onTaskDatePickerRequested(@NonNull String initialDate) {
        return TaskDatePicker.show(
                getSupportFragmentManager(),
                EDIT_TASK_DATE_REQUEST,
                initialDate
        );
    }

    @Override
    public void onTaskTimePickerRequested(@NonNull String initialTime) {
        TaskTimePicker.show(
                this,
                getSupportFragmentManager(),
                EDIT_TASK_TIME_REQUEST,
                initialTime
        );
    }

    @Override
    public void onTaskStatusChanged(@NonNull TaskModel task, boolean isComplete) {
        TaskRepository repository = viewModel.getTaskRepository();
        if (repository == null) {
            return;
        }

        repository.updateStatus(task.getId(), isComplete, new TaskRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(@NonNull Exception exception) {
                taskAdapter.submitTasks(tasks);
                refreshScheduleView();
                showStatusMessage(R.string.update_task_error);
            }
        });
    }

    @Override
    public void onTaskSaveRequested(@NonNull String taskText,
                                    @NonNull String dueDate,
                                    @NonNull String dueTime,
                                    @NonNull AddNewTask.SaveCallback callback) {
        TaskRepository repository = viewModel.getTaskRepository();
        if (repository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        boolean shouldTeachSwipe = launchManager.shouldShowSwipeHint();
        repository.addTask(taskText, dueDate, dueTime, new TaskRepository.AddTaskCallback() {
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
        FirebaseUser sourceUser = authRepository.getCurrentUser();
        TaskRepository sourceRepository = viewModel.getTaskRepository();

        if (sourceUser == null
                || !sourceUser.isAnonymous()
                || sourceRepository == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        String sourceUserId = sourceUser.getUid();
        List<TaskModel> guestTasks = new ArrayList<>(tasks);

        stopListeningForTasks();

        // Verify and open the destination account in a secondary Firebase session.
        // The default auth session deliberately stays on the guest account until
        // its Firestore data has been copied and cleaned up.
        authRepository.openExistingAccountSession(
                this,
                email,
                password,
                new AuthRepository.ExistingAccountSessionCallback() {
                    @Override
                    public void onSuccess(
                            @NonNull AuthRepository.ExistingAccountSession targetSession) {
                        TaskRepository targetRepository;
                        try {
                            targetRepository = targetSession.taskRepository();
                        } catch (Exception exception) {
                            targetSession.close();
                            resumeGuestSession(sourceUserId);
                            callback.onError(exception);
                            return;
                        }

                        targetRepository.mergeTasks(
                                sourceUserId,
                                guestTasks,
                                new TaskRepository.OperationCallback() {
                                    @Override
                                    public void onSuccess() {
                                        sourceRepository.deleteAllTasks(
                                                new TaskRepository.OperationCallback() {
                                                    @Override
                                                    public void onSuccess() {
                                                        switchToRestoredAccount(
                                                                email,
                                                                password,
                                                                sourceUserId,
                                                                sourceRepository,
                                                                guestTasks,
                                                                targetSession,
                                                                callback
                                                        );
                                                    }

                                                    @Override
                                                    public void onError(
                                                            @NonNull Exception exception) {
                                                        recoverGuestSnapshot(
                                                                sourceUserId,
                                                                sourceRepository,
                                                                guestTasks,
                                                                targetSession,
                                                                exception,
                                                                callback
                                                        );
                                                    }
                                                }
                                        );
                                    }

                                    @Override
                                    public void onError(@NonNull Exception exception) {
                                        // The source account is still untouched, so a
                                        // failed target merge can be retried safely.
                                        targetSession.close();
                                        resumeGuestSession(sourceUserId);
                                        callback.onError(exception);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        resumeGuestSession(sourceUserId);
                        callback.onError(exception);
                    }
                }
        );
    }

    private void switchToRestoredAccount(
            @NonNull String email,
            @NonNull String password,
            @NonNull String sourceUserId,
            @NonNull TaskRepository sourceRepository,
            @NonNull List<TaskModel> guestTasks,
            @NonNull AuthRepository.ExistingAccountSession targetSession,
            @NonNull AccountBottomSheet.ActionCallback callback) {
        authRepository.restoreEmailAccount(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                targetSession.close();
                resetForIdentityChange(user);
                callback.onSuccess();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                // The destination already has the imported tasks. Restore the
                // guest snapshot as well so a transient final sign-in failure
                // never leaves the current user looking at an empty account.
                recoverGuestSnapshot(
                        sourceUserId,
                        sourceRepository,
                        guestTasks,
                        targetSession,
                        exception,
                        callback
                );
            }
        });
    }

    private void recoverGuestSnapshot(
            @NonNull String sourceUserId,
            @NonNull TaskRepository sourceRepository,
            @NonNull List<TaskModel> guestTasks,
            @NonNull AuthRepository.ExistingAccountSession targetSession,
            @NonNull Exception originalException,
            @NonNull AccountBottomSheet.ActionCallback callback) {
        targetSession.close();

        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser == null || !sourceUserId.equals(currentUser.getUid())) {
            viewModel.resumeUserSession(sourceUserId);
            callback.onError(originalException);
            return;
        }

        sourceRepository.restoreTasks(
                guestTasks,
                new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        resumeGuestSession(sourceUserId);
                        callback.onError(originalException);
                    }

                    @Override
                    public void onError(@NonNull Exception restoreException) {
                        // The imported target copy is already safe and stable.
                        // Surface the original migration failure, then reconnect
                        // whatever identity Firebase currently has.
                        resumeGuestSession(sourceUserId);
                        callback.onError(originalException);
                    }
                }
        );
    }

    private void resumeGuestSession(@NonNull String sourceUserId) {
        viewModel.resumeUserSession(sourceUserId);
    }

    @Override
    public void onSignOutRequested() {
        viewModel.signOut();
    }

    @Override
    public void onDeleteRequested(@Nullable String password,
                                  @NonNull AccountBottomSheet.ActionCallback callback) {
        TaskRepository repository = viewModel.getTaskRepository();
        FirebaseUser user = authRepository.getCurrentUser();

        if (repository == null || user == null) {
            callback.onError(new IllegalStateException(getString(R.string.auth_error)));
            return;
        }

        String userId = user.getUid();

        authRepository.reauthenticateForDeletion(password, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                // Always take a fresh server snapshot before deleting anything.
                // The in-memory list may be stale or may have come from cache.
                repository.loadServerTasksOnce(new TaskRepository.TaskListener() {
                    @Override
                    public void onTasksChanged(@NonNull List<TaskModel> serverTasks) {
                        List<TaskModel> deletionSnapshot = new ArrayList<>(serverTasks);

                        // Freeze live updates during the destructive part so the UI
                        // does not temporarily render an empty account before the
                        // full account deletion has actually succeeded.
                        stopListeningForTasks();

                        repository.deleteAllTasks(new TaskRepository.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                authRepository.deleteCurrentAccount(
                                        new AuthRepository.SimpleCallback() {
                                            @Override
                                            public void onSuccess() {
                                                viewModel.completeAccountDeletion();
                                                callback.onSuccess();
                                            }

                                            @Override
                                            public void onError(
                                                    @NonNull Exception exception) {
                                                rollbackAccountDeletion(
                                                        userId,
                                                        repository,
                                                        deletionSnapshot,
                                                        exception,
                                                        callback
                                                );
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onError(@NonNull Exception exception) {
                                // deleteAllTasks works in batches, so a failure can
                                // happen after an earlier batch already succeeded.
                                // Restore the complete server snapshot in that case.
                                rollbackAccountDeletion(
                                        userId,
                                        repository,
                                        deletionSnapshot,
                                        exception,
                                        callback
                                );
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull Exception exception) {
                        // Nothing has been deleted yet, so simply abort.
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

    private void rollbackAccountDeletion(
            @NonNull String userId,
            @NonNull TaskRepository repository,
            @NonNull List<TaskModel> deletionSnapshot,
            @NonNull Exception originalException,
            @NonNull AccountBottomSheet.ActionCallback callback) {
        FirebaseUser currentUser = authRepository.getCurrentUser();

        if (currentUser == null || !userId.equals(currentUser.getUid())) {
            // If Firebase no longer exposes the original identity, we cannot
            // safely write back into that user's owner-protected Firestore path.
            // Do not create a different anonymous identity and pretend rollback
            // succeeded.
            callback.onError(originalException);
            return;
        }

        repository.restoreTasks(
                deletionSnapshot,
                new TaskRepository.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        viewModel.resumeUserSession(userId);
                        callback.onError(originalException);
                    }

                    @Override
                    public void onError(@NonNull Exception restoreException) {
                        // Reconnect to the surviving account even if compensating
                        // writes fail, then surface the original deletion failure.
                        viewModel.resumeUserSession(userId);
                        callback.onError(originalException);
                    }
                }
        );
    }
}

package com.farwaahmad.mylist.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.farwaahmad.mylist.StartupTaskStore;
import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.data.TaskRepository;
import com.farwaahmad.mylist.model.TaskModel;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owns the long-lived state for the main screen.
 *
 * Views render this state and handle Android UI concerns; Firebase identity,
 * repository lifetime and the active Firestore subscription live here so they
 * survive Activity recreation and are cleaned up in one place.
 */
public class MainViewModel extends ViewModel {

    public enum ErrorType {
        AUTH,
        LOAD_TASKS
    }

    public static final class UiState {

        @NonNull
        private final List<TaskModel> tasks;
        private final boolean loading;
        private final boolean connectionError;
        private final boolean controlsEnabled;

        private UiState(@NonNull List<TaskModel> tasks,
                        boolean loading,
                        boolean connectionError,
                        boolean controlsEnabled) {
            this.tasks = tasks;
            this.loading = loading;
            this.connectionError = connectionError;
            this.controlsEnabled = controlsEnabled;
        }

        @NonNull
        public List<TaskModel> getTasks() {
            return tasks;
        }

        public boolean isLoading() {
            return loading;
        }

        public boolean hasConnectionError() {
            return connectionError;
        }

        public boolean areControlsEnabled() {
            return controlsEnabled;
        }
    }

    public static final class Event<T> {

        @NonNull
        private final T content;
        private boolean handled;

        private Event(@NonNull T content) {
            this.content = content;
        }

        @Nullable
        public synchronized T getContentIfNotHandled() {
            if (handled) {
                return null;
            }
            handled = true;
            return content;
        }
    }

    private final AuthRepository authRepository = new AuthRepository();
    private final List<TaskModel> tasks = new ArrayList<>();
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Event<ErrorType>> errorEvents = new MutableLiveData<>();

    @Nullable
    private TaskRepository taskRepository;
    @Nullable
    private ListenerRegistration listenerRegistration;

    private boolean initialized;
    private boolean started;
    private boolean signInInProgress;
    private boolean initialStateReady;

    public MainViewModel() {
        publishState(true, false);
    }

    @NonNull
    public LiveData<UiState> getUiState() {
        return uiState;
    }

    @NonNull
    public LiveData<Event<ErrorType>> getErrorEvents() {
        return errorEvents;
    }

    @NonNull
    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    @Nullable
    public TaskRepository getTaskRepository() {
        return taskRepository;
    }

    public void initialize(@Nullable StartupTaskStore.State startupState) {
        if (initialized) {
            return;
        }
        initialized = true;

        if (startupState == null) {
            publishState(true, false);
            return;
        }

        initialStateReady = true;

        if (startupState.isLoadFailed()) {
            publishState(false, true);
            return;
        }

        tasks.clear();
        tasks.addAll(startupState.getTasks());
        publishState(false, false);
    }

    public void start() {
        started = true;
        ensureSignedIn();
    }

    public void stop() {
        started = false;
        stopListeningForTasks();
    }

    public void retry() {
        initialStateReady = false;
        publishState(true, false);

        if (!started) {
            return;
        }

        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            startListeningForTasks(user);
        } else {
            ensureSignedIn();
        }
    }

    public void pauseTaskListening() {
        stopListeningForTasks();
    }

    public void resetForIdentityChange(@NonNull FirebaseUser user) {
        stopListeningForTasks();
        taskRepository = null;
        tasks.clear();
        initialStateReady = false;
        publishState(true, false);

        if (started) {
            startListeningForTasks(user);
        }
    }

    public void resumeUserSession(@NonNull String userId) {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (!started) {
            return;
        }

        if (currentUser != null && userId.equals(currentUser.getUid())) {
            startListeningForTasks(currentUser);
        } else {
            ensureSignedIn();
        }
    }

    public void signOut() {
        stopListeningForTasks();
        taskRepository = null;
        tasks.clear();
        initialStateReady = false;
        authRepository.signOut();
        publishState(true, false);

        if (started) {
            ensureSignedIn();
        }
    }

    public void completeAccountDeletion() {
        stopListeningForTasks();
        taskRepository = null;
        tasks.clear();
        initialStateReady = false;
        publishState(true, false);

        if (started) {
            ensureSignedIn();
        }
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
            publishState(true, false);
        }

        signInInProgress = true;
        authRepository.ensureUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                signInInProgress = false;
                if (started) {
                    startListeningForTasks(user);
                }
            }

            @Override
            public void onError(@NonNull Exception exception) {
                signInInProgress = false;
                if (!started) {
                    return;
                }

                taskRepository = null;
                initialStateReady = true;
                publishState(false, true);
                errorEvents.setValue(new Event<>(ErrorType.AUTH));
            }
        });
    }

    private void startListeningForTasks(@NonNull FirebaseUser user) {
        stopListeningForTasks();

        if (!initialStateReady) {
            publishState(true, false);
        }

        taskRepository = new TaskRepository(user.getUid());
        publishState(!initialStateReady, false);

        listenerRegistration = taskRepository.listenForTasks(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> updatedTasks) {
                tasks.clear();
                tasks.addAll(updatedTasks);
                initialStateReady = true;
                publishState(false, false);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                initialStateReady = true;
                publishState(false, tasks.isEmpty());
                errorEvents.setValue(new Event<>(ErrorType.LOAD_TASKS));
            }
        });
    }

    private void stopListeningForTasks() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void publishState(boolean loading, boolean connectionError) {
        boolean controlsEnabled =
                taskRepository != null && authRepository.getCurrentUser() != null;

        uiState.setValue(
                new UiState(
                        Collections.unmodifiableList(new ArrayList<>(tasks)),
                        loading,
                        connectionError,
                        controlsEnabled
                )
        );
    }

    @Override
    protected void onCleared() {
        stopListeningForTasks();
    }
}

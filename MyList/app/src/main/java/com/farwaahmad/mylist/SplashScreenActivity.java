package com.farwaahmad.mylist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.data.TaskRepository;
import com.farwaahmad.mylist.model.TaskModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long STARTUP_TIMEOUT_MS = 5000L;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private AuthRepository authRepository;

    @Nullable
    private ArrayList<TaskModel> cachedTasks;

    private boolean cacheReadFinished;
    private boolean serverLoadFinished;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> !navigated);

        authRepository = new AuthRepository();

        LaunchManager launchManager = new LaunchManager(this);
        if (launchManager.isFirstTime()) {
            launchOnboarding();
            return;
        }

        handler.postDelayed(this::finishStartupAtDeadline, STARTUP_TIMEOUT_MS);
        preloadTasks();
    }

    private void launchOnboarding() {
        if (!canNavigate()) {
            return;
        }

        navigated = true;
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, SliderScreenActivity.class));
        finish();
    }

    private void preloadTasks() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            loadTasks(currentUser);
            return;
        }

        authRepository.ensureUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                loadTasks(user);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                StartupTaskStore.publishError();
                launchMain();
            }
        });
    }

    private void loadTasks(@NonNull FirebaseUser user) {
        TaskRepository taskRepository = new TaskRepository(user.getUid());

        taskRepository.loadCachedTasksOnce(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> tasks) {
                cacheReadFinished = true;
                cachedTasks = new ArrayList<>(tasks);

                if (!tasks.isEmpty()) {
                    StartupTaskStore.publishTasks(tasks);
                    launchMain();
                    return;
                }

                finishAfterServerFailureIfPossible();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                cacheReadFinished = true;
                cachedTasks = null;
                finishAfterServerFailureIfPossible();
            }
        });

        taskRepository.loadServerTasksOnce(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> tasks) {
                serverLoadFinished = true;
                StartupTaskStore.publishTasks(tasks);
                launchMain();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                serverLoadFinished = true;
                finishAfterServerFailureIfPossible();
            }
        });
    }

    private void finishAfterServerFailureIfPossible() {
        if (!serverLoadFinished || !cacheReadFinished || navigated) {
            return;
        }

        if (cachedTasks != null) {
            StartupTaskStore.publishTasks(cachedTasks);
        } else {
            StartupTaskStore.publishError();
        }
        launchMain();
    }

    private void finishStartupAtDeadline() {
        if (navigated) {
            return;
        }

        if (cachedTasks != null && !cachedTasks.isEmpty()) {
            StartupTaskStore.publishTasks(cachedTasks);
        } else {
            StartupTaskStore.publishError();
        }
        launchMain();
    }

    private void launchMain() {
        if (!canNavigate()) {
            return;
        }

        navigated = true;
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private boolean canNavigate() {
        return !navigated && !isFinishing() && !isDestroyed();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

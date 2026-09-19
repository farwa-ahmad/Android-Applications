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

    public static final String EXTRA_INITIAL_TASKS = "initialTasks";
    public static final String EXTRA_INITIAL_LOAD_FAILED = "initialLoadFailed";
    private static final long MAX_SPLASH_DURATION_MS = 1000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AuthRepository authRepository;
    @Nullable
    private ArrayList<TaskModel> cachedTasks;
    private boolean cacheReadFinished;
    private boolean networkLoadFailed;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> !navigated);

        authRepository = new AuthRepository();
        LaunchManager launchManager = new LaunchManager(this);
        if (launchManager.isFirstTime()) {
            warmUpAuthentication();
            handler.postDelayed(this::launchOnboarding, MAX_SPLASH_DURATION_MS);
            return;
        }

        handler.postDelayed(
                this::finishSplashAtDeadline,
                MAX_SPLASH_DURATION_MS
        );
        preloadTasks();
    }

    private void warmUpAuthentication() {
        if (authRepository.getCurrentUser() != null) {
            return;
        }

        authRepository.ensureUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                // Authentication is ready by the time onboarding is completed.
            }

            @Override
            public void onError(@NonNull Exception exception) {
                // MainActivity will retry after onboarding if this warm-up fails.
            }
        });
    }

    private void launchOnboarding() {
        if (navigated || isFinishing() || isDestroyed()) {
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
                launchMain(new ArrayList<>(), true);
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

                if (!cachedTasks.isEmpty() || networkLoadFailed) {
                    launchMain(cachedTasks, false);
                }
            }

            @Override
            public void onError(@NonNull Exception exception) {
                cacheReadFinished = true;
                if (networkLoadFailed) {
                    launchMain(null, true);
                }
            }
        });

        taskRepository.loadTasksOnce(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> tasks) {
                launchMain(new ArrayList<>(tasks), false);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                networkLoadFailed = true;
                if (cacheReadFinished) {
                    if (cachedTasks != null) {
                        launchMain(cachedTasks, false);
                    } else {
                        launchMain(null, true);
                    }
                }
            }
        });
    }

    private void finishSplashAtDeadline() {
        if (cachedTasks != null) {
            launchMain(cachedTasks, false);
            return;
        }

        launchMain(null, networkLoadFailed);
    }

    private void launchMain(@Nullable ArrayList<TaskModel> tasks, boolean loadFailed) {
        if (navigated || isFinishing() || isDestroyed()) {
            return;
        }

        navigated = true;
        handler.removeCallbacksAndMessages(null);

        Intent intent = new Intent(this, MainActivity.class);
        if (tasks != null) {
            intent.putExtra(EXTRA_INITIAL_TASKS, tasks);
        }
        intent.putExtra(EXTRA_INITIAL_LOAD_FAILED, loadFailed);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}

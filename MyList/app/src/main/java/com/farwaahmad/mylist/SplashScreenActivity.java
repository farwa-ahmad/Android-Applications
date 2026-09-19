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
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> !navigated);
        splashScreen.setOnExitAnimationListener(provider -> provider.remove());
        super.onCreate(savedInstanceState);

        LaunchManager launchManager = new LaunchManager(this);
        if (launchManager.isFirstTime()) {
            handler.postDelayed(this::launchOnboarding, MAX_SPLASH_DURATION_MS);
            return;
        }

        authRepository = new AuthRepository();
        handler.postDelayed(
                () -> launchMain(null, false),
                MAX_SPLASH_DURATION_MS
        );
        preloadTasks();
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
        taskRepository.loadTasksOnce(new TaskRepository.TaskListener() {
            @Override
            public void onTasksChanged(@NonNull List<TaskModel> tasks) {
                launchMain(new ArrayList<>(tasks), false);
            }

            @Override
            public void onError(@NonNull Exception exception) {
                launchMain(new ArrayList<>(), true);
            }
        });
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

package com.farwaahmad.mylist;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.data.TaskRepository;
import com.farwaahmad.mylist.databinding.ActivitySplashScreenBinding;
import com.farwaahmad.mylist.model.TaskModel;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_TASKS = "initialTasks";
    public static final String EXTRA_INITIAL_LOAD_FAILED = "initialLoadFailed";

    private ActivitySplashScreenBinding binding;
    private AuthRepository authRepository;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        LaunchManager launchManager = new LaunchManager(this);
        if (launchManager.isFirstTime()) {
            startActivity(new Intent(this, SliderScreenActivity.class));
            finish();
            return;
        }

        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlSplashScreen.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        authRepository = new AuthRepository();
        preloadTasks();
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

    private void launchMain(@NonNull ArrayList<TaskModel> tasks, boolean loadFailed) {
        if (navigated || isFinishing() || isDestroyed()) {
            return;
        }

        navigated = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_INITIAL_TASKS, tasks);
        intent.putExtra(EXTRA_INITIAL_LOAD_FAILED, loadFailed);
        startActivity(intent);
        finish();
    }
}

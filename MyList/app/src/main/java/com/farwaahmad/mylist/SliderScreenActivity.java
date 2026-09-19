package com.farwaahmad.mylist;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.farwaahmad.mylist.data.AuthRepository;
import com.farwaahmad.mylist.databinding.ActivitySliderScreenBinding;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;

public class SliderScreenActivity extends AppCompatActivity {

    private ActivitySliderScreenBinding binding;
    private LaunchManager launchManager;
    private AuthRepository authRepository;

    private boolean authenticationInProgress;
    private boolean continueWhenAuthenticationFinishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        launchManager = new LaunchManager(this);
        authRepository = new AuthRepository();

        binding = ActivitySliderScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.getInsetsController(getWindow(), binding.getRoot())
                .setAppearanceLightStatusBars(true);

        ViewCompat.setOnApplyWindowInsetsListener(binding.rlSlider, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    systemBars.top,
                    view.getPaddingRight(),
                    systemBars.bottom
            );
            return windowInsets;
        });

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlSlider.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        warmUpAuthentication();

        binding.btnNext.setOnClickListener(v -> finishOnboarding());
    }

    private void warmUpAuthentication() {
        if (authRepository.getCurrentUser() != null || authenticationInProgress) {
            return;
        }

        authenticationInProgress = true;
        authRepository.ensureUser(new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(@NonNull FirebaseUser user) {
                authenticationInProgress = false;
                if (continueWhenAuthenticationFinishes && !isFinishing()) {
                    launchMainWithEmptyTaskList();
                }
            }

            @Override
            public void onError(@NonNull Exception exception) {
                authenticationInProgress = false;
                if (continueWhenAuthenticationFinishes && !isFinishing()) {
                    StartupTaskStore.publishError();
                    launchMain();
                }
            }
        });
    }

    private void finishOnboarding() {
        launchManager.setFirstLaunch(false);

        if (authRepository.getCurrentUser() != null) {
            launchMainWithEmptyTaskList();
            return;
        }

        continueWhenAuthenticationFinishes = true;
        binding.btnNext.setEnabled(false);
        binding.btnNext.setText(R.string.getting_ready);

        if (!authenticationInProgress) {
            warmUpAuthentication();
        }
    }

    private void launchMainWithEmptyTaskList() {
        StartupTaskStore.publishTasks(Collections.emptyList());
        launchMain();
    }

    private void launchMain() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}

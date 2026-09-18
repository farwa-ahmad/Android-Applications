package com.example.mylist;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mylist.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends AppCompatActivity {

    private ActivitySplashScreenBinding binding;
    private LaunchManager launchManager;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable navigateToNextScreen = () -> {
        if (launchManager.isFirstTime()) {
            launchManager.setFirstLaunch(false);
            startActivity(new Intent(this, SliderScreenActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchManager = new LaunchManager(this);

        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlSplashScreen.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        handler.postDelayed(navigateToNextScreen, 1500);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(navigateToNextScreen);
        super.onDestroy();
    }
}

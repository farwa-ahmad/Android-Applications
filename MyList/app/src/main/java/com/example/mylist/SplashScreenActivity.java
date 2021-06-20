package com.example.mylist;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mylist.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends AppCompatActivity {

    ActivitySplashScreenBinding binding;
    LaunchManager launchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchManager = new LaunchManager(this);

        //view binding
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //animated background
        AnimationDrawable animationDrawable = (AnimationDrawable) binding.rlSplashScreen.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        //setting time and intents
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(launchManager.isFirstTime()) {
                    launchManager.setFirstLaunch(false);
                    startActivity(new Intent(getApplicationContext(), SliderScreenActivity.class));
                }
                else {
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                finish();
            }
        },1500);

    }

}
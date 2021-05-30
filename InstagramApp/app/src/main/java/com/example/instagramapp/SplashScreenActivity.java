package com.example.instagramapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;

public class SplashScreenActivity extends AppCompatActivity {
LaunchManager launchManager;
ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
progressBar = findViewById(R.id.progressBar);
        launchManager = new LaunchManager(this);
        ConstraintLayout constraintLayout = findViewById(R.id.layoutSplashBG);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < 100; i++)
                {
                    progressBar.incrementProgressBy(1);
                }
                if(launchManager.isFirstTime()) {
                    launchManager.setFirstLaunch(false);
                    startActivity(new Intent(getApplicationContext(), SliderActivity.class));
                }
                    else {
                    startActivity(new Intent(getApplicationContext(),SigninActivity.class));
                }
            }
        },4000);


    }
}
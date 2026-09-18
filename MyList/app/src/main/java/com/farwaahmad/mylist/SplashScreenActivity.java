package com.farwaahmad.mylist;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        LaunchManager launchManager = new LaunchManager(this);
        Class<?> destination = launchManager.isFirstTime()
                ? SliderScreenActivity.class
                : MainActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}

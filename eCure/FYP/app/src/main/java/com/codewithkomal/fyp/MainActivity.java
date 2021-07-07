package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.codewithkomal.fyp.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    //Variables
    Animation bottomAnimation; //topAnim,;
    //ImageView logo;
    TextView appName;
    LottieAnimationView lottieAnimationView;
    ActivityMainBinding binding;

    LaunchManager launchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        launchManager = new LaunchManager(this);

        //topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        //Hooks
        //logo = findViewById(R.id.logo);
        //appName = findViewById(R.id.app_name);
        //lottieAnimationView = findViewById(R.id.lottie);

        //logo.setAnimation(topAnim);
        binding.appName.setAnimation(bottomAnimation);

       // logo.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        //appName.animate().translationY(1400).setDuration(1000).setStartDelay(4000);
        //lottieAnimationView.animate().translationY(1400).setDuration(1000).setStartDelay(4000);

        //setting time and intents
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(launchManager.isFirstTime()) {
                    launchManager.setFirstLaunch(false);
                    startActivity(new Intent(getApplicationContext(), WelcomeScreen.class));
                }
                else {
                    startActivity(new Intent(getApplicationContext(),SelectionDoctorPatient.class));
                }
                finish();
            }
        },5000);

    }
}



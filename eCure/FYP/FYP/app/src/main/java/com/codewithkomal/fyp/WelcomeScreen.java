package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;

import com.codewithkomal.fyp.databinding.ActivityWelcomeScreenBinding;


public class WelcomeScreen extends AppCompatActivity {

    ActivityWelcomeScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


            //view binding
            binding = ActivityWelcomeScreenBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());

            //animated background
            AnimationDrawable animationDrawable = (AnimationDrawable) binding.rlSlider.getBackground();
            animationDrawable.setEnterFadeDuration(2000);
            animationDrawable.setExitFadeDuration(4000);
            animationDrawable.start();

            //Next intent
            binding.btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(),SelectionDoctorPatient.class));
                    finish();
                }
            });


        }
}
package com.example.mylist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;

import com.example.mylist.databinding.ActivitySliderScreenBinding;
import com.example.mylist.databinding.ActivitySplashScreenBinding;

public class SliderScreenActivity extends AppCompatActivity {

    ActivitySliderScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivitySliderScreenBinding.inflate(getLayoutInflater());
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
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
        });








    }
}
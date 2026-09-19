package com.farwaahmad.mylist;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.farwaahmad.mylist.databinding.ActivitySliderScreenBinding;

public class SliderScreenActivity extends AppCompatActivity {

    private ActivitySliderScreenBinding binding;
    private LaunchManager launchManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        launchManager = new LaunchManager(this);

        binding = ActivitySliderScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AnimationDrawable animationDrawable =
                (AnimationDrawable) binding.rlSlider.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        binding.btnNext.setOnClickListener(v -> {
            launchManager.setFirstLaunch(false);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}

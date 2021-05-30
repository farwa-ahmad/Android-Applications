package com.example.instagramapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class SliderActivity extends AppCompatActivity {

    ViewPager viewPager;
    Button btnNext;
    int[] layouts;
    SliderAdapter sliderAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slider);

        viewPager = findViewById(R.id.vpSlider);
        btnNext = findViewById(R.id.btnNext);

        layouts = new int[]{
                R.layout.slider1,
                R.layout.slider2
        };

        sliderAdapter = new SliderAdapter(this,layouts);

        viewPager.setAdapter(sliderAdapter);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()+1 <layouts.length)
                    viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                else {
                    startActivity(new Intent(getApplicationContext(),SigninActivity.class));
                }
            }
        });

        viewPager.addOnPageChangeListener(viewPagerChangeListener);
    }

    ViewPager.OnPageChangeListener viewPagerChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if(position == layouts.length -1)
                btnNext.setText("Continue");
            else
                btnNext.setText("Next");

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

}
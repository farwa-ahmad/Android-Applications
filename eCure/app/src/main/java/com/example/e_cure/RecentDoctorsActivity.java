package com.example.e_cure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.e_cure.databinding.ActivityPatientProfilesBinding;
import com.example.e_cure.databinding.ActivityRecentDoctorsBinding;

public class RecentDoctorsActivity extends AppCompatActivity {

    ActivityRecentDoctorsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_doctors);

        //view binding
        binding = ActivityRecentDoctorsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //back button
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RecentDoctorsActivity.this,PatientMainActivity.class));
                finish();
            }
        });


    }
    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}
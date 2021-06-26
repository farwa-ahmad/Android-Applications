package com.example.e_cure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.e_cure.databinding.ActivityPatientProfilesBinding;
import com.example.e_cure.databinding.ActivityRecentDoctorsBinding;

public class PDoctorProfileActivity extends AppCompatActivity {

    ActivityPatientProfilesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdoctor_profile);

        //view binding
        binding = ActivityPatientProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //back button
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PDoctorProfileActivity.this,FindDoctorActivity.class));
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
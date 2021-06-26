package com.example.e_cure;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.e_cure.databinding.ActivityPatientProfilesBinding;


public class PatientProfilesActivity extends AppCompatActivity {

    ActivityPatientProfilesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profiles);

        //view binding
        binding = ActivityPatientProfilesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //back button
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientProfilesActivity.this,PatientMainActivity.class));
                finish();
            }
        });

        //Add New Profile Button
        binding.tvAddNewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientProfilesActivity.this,AddNewProfileActivity.class));
            }
        });
    }
    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}
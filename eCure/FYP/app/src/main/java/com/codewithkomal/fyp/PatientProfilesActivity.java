package com.codewithkomal.fyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.codewithkomal.fyp.databinding.ActivityPatientProfilesBinding;


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
                startActivity(new Intent(PatientProfilesActivity.this,MainActivityPatient.class));
                finish();
            }
        });

        //Add New Profile Button
        binding.tvAddNewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientProfilesActivity.this,EditPatientProfile.class));
            }
        });
    }
    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}
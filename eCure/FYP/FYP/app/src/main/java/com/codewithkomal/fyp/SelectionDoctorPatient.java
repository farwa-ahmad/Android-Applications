package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.codewithkomal.fyp.controller.DoctorHome;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SelectionDoctorPatient extends AppCompatActivity {

    Button callDrScreen, callPatientScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_doctor_patient);

        callDrScreen = findViewById(R.id.dr);
        callPatientScreen = findViewById(R.id.patient);

        callDrScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    Intent i = new Intent(SelectionDoctorPatient.this, DoctorHome.class);
                    startActivity(i);
                    finish();
                } else{
                    Intent i = new Intent(SelectionDoctorPatient.this, DoctorLoginSignUpSelection.class);
                    startActivity(i);
                }
            }
        });

        callPatientScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    Intent i = new Intent(SelectionDoctorPatient.this, MainActivityPatient.class);
                    startActivity(i);
                    finish();
                } else{
                    Intent i = new Intent(SelectionDoctorPatient.this, PatientLoginSignUpSelection.class);
                    startActivity(i);
                }
            }
        });
    }
}
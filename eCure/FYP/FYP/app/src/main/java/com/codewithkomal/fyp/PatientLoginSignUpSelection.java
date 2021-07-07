package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PatientLoginSignUpSelection extends AppCompatActivity {

    Button pLogin, pSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login_sign_up_selection);

        pLogin = findViewById(R.id.btnPatientlogin);
        pSignUp = findViewById(R.id.btnPatientsignup);
        pLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientLoginSignUpSelection.this,PatientLogin.class);
                startActivity(intent);
                finish();
            }
        });

        pSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientLoginSignUpSelection.this,PatientSignUp.class);
                startActivity(intent);
            }
        });
    }
}
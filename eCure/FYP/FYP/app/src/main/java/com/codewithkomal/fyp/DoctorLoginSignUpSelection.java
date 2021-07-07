package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DoctorLoginSignUpSelection extends AppCompatActivity {

    Button drLogin, drSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login_sign_up_selection);

        drLogin = findViewById(R.id.btnDrlogin);
        drSignUp = findViewById(R.id.btnDrsignup);
        drLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorLoginSignUpSelection.this,DoctorLogin.class);
                startActivity(intent);
            }
        });

        drSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorLoginSignUpSelection.this,DoctorSignUp.class);
                startActivity(intent);
            }
        });

    }
}
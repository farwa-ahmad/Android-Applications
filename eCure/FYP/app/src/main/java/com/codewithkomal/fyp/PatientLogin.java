package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class PatientLogin extends AppCompatActivity {

    Button callLogin;
    TextView forgetPassword, callSignUp;
    TextInputLayout email, password;
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_login);

        email = findViewById(R.id.patientEmail);
        password = findViewById(R.id.patientPassword);
        callLogin = findViewById(R.id.btnLoginPatient);
        callSignUp = findViewById(R.id.btnPatientCreateAcc);
        fAuth = FirebaseAuth.getInstance();
        forgetPassword = findViewById(R.id.btnPatientForgetPass);

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),ForgotPassword.class);
                startActivity(intent);
            }
        });

    }
    public void MoveToPatientSignUP(View view) {
        Intent intent = new Intent(this,PatientSignUp.class);
        startActivity(intent);
    }

    public void MoveToPatientHome(View view) {
        if(!validateEmail() | !validatePassword()){
            return;
        }
        else{
            isUser();
        }

    }

    private void isUser(){
        final String userEnteredUsername = email.getEditText().getText().toString().trim();
        final String userEnteredPassword = password.getEditText().getText().toString().trim();
        FirebaseAuth mauth = FirebaseAuth.getInstance();
        mauth.signInWithEmailAndPassword(userEnteredUsername, userEnteredPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(getApplicationContext(), MainActivityPatient.class);
                    startActivity(intent);
                    finish();
                }else{
                    Exception e = task.getException();
                    Toast.makeText(PatientLogin.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /*
    private void isUser() {
        final String userEnteredUsername = email.getEditText().getText().toString().trim();
        final String userEnteredPassword = password.getEditText().getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Patient");
        Query checkUser = reference.orderByChild("email").equalTo(userEnteredUsername);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    email.setError(null);
                    email.setErrorEnabled(false);

                    password.setError(null);
                    password.setErrorEnabled(false);

                    String passwordFromDB = snapshot.child(userEnteredUsername).child("password").getValue(String.class);

                    if(passwordFromDB.equals(userEnteredPassword)){

                        password.setError(null);
                        password.setErrorEnabled(false);

                        email.setError(null);
                        email.setErrorEnabled(false);
                          *//*
                        // get data to add in profile section
                        String fullNameFromDB = snapshot.child(userEnteredUsername).child("fname").getValue(String.class);
                        String usernameFromDB = snapshot.child(userEnteredUsername).child("uname").getValue(String.class);
                        String emailFromDB = snapshot.child(userEnteredUsername).child("email").getValue(String.class);
                        String ageFromDB = snapshot.child(userEnteredUsername).child("age").getValue(String.class);
                        String genderFromDB = snapshot.child(userEnteredUsername).child("gender").getValue(String.class);
                        String phoneNumberFromDB = snapshot.child(userEnteredUsername).child("phoneNo").getValue(String.class);*//*

                        Intent intent = new Intent(getApplicationContext(), PatientProfile.class);
                        *//*
                        //put data in profile
                        intent.putExtra("fname",fullNameFromDB);
                        intent.putExtra("uname",usernameFromDB);
                        intent.putExtra("email",emailFromDB);
                        intent.putExtra("age",ageFromDB);
                        intent.putExtra("gender",genderFromDB);
                        intent.putExtra("phoneNo",phoneNumberFromDB);
                        intent.putExtra("password",passwordFromDB);*//*

                        startActivity(intent);
                    }
                    else{
                        password.setError("Wrong Password");
                        password.requestFocus();
                    }
                }
                else{
                    email.setError("No such user exist");
                    email.requestFocus();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
*/
    public void BackToPatientLoginSignUp(View view) {
        Intent intent = new Intent(this,PatientLoginSignUpSelection.class);
        startActivity(intent);
        finish();
    }

    private boolean validateEmail() {
        String val = email.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            email.setError("Field cannot be empty");
            return false;
        } else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = password.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;
        } else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }


}
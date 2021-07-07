package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.codewithkomal.fyp.controller.DoctorHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class DoctorLogin extends AppCompatActivity {

    Button callLogin;
    TextView callSignUp, forgot_pass;
    TextInputLayout email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_login);

        email = findViewById(R.id.emailOfDr);
        password = findViewById(R.id.passwordOfDr);
        callLogin = findViewById(R.id.btnLoginDr);
        callSignUp = findViewById(R.id.btnCreateAccDr);
        forgot_pass = findViewById(R.id.dr_forgotPass);

        forgot_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(DoctorLogin.this, ForgotPassword.class);
                startActivity(it);
            }
        });
    }
    public void MoveToDrHome(View view) {
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
                    Intent intent = new Intent(getApplicationContext(), DoctorHome.class);
                    startActivity(intent);
                }else{
                    Exception e = task.getException();
                    Toast.makeText(DoctorLogin.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void BackToDoctorLoginSignup(View view) {
        Intent intent = new Intent(this,DoctorLoginSignUpSelection.class);
        startActivity(intent);
        finish();
    }

    public void MoveToDoctorSignUp(View view) {
        Intent intent = new Intent(this,DoctorSignUp.class);
        startActivity(intent);
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
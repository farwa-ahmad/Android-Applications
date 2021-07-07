package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

public class PatientSignUp extends AppCompatActivity {

    TextInputLayout fullName, email, password, age, phoneNo;
    Button signUp;
    CountryCodePicker countryCodePicker;
    RadioGroup radioGroup;
    RadioButton radioButton;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_sign_up);

        //Hooks
        fullName = findViewById(R.id.fullname);
        //userName = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        age = findViewById(R.id.age);
        phoneNo = findViewById(R.id.phoneNo);
        countryCodePicker = findViewById(R.id.patientCountryCode);
        radioGroup = findViewById(R.id.gender);
        signUp = findViewById(R.id.btnPatientSignUp);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Patient");

                if(!validateFullName() | !validateEmail() | !validatePassword() | !validateAge() |
                !validateGender() | !validatePhone()){
                    return;
                }

                FirebaseAuth mauth = FirebaseAuth.getInstance();
                mauth.createUserWithEmailAndPassword(email.getEditText().getText().toString(), password.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Get all the values
                            String _fname = fullName.getEditText().getText().toString().trim();
                            //String _uname = userName.getEditText().getText().toString().trim();
                            String _email = email.getEditText().getText().toString().trim();
                            //String _password = password.getEditText().getText().toString().trim();
                            String _age = age.getEditText().getText().toString().trim();
                            radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                            String _gender = radioButton.getText().toString().trim();
                            String _getUserEnteredPhoneNumber = phoneNo.getEditText().getText().toString().trim();
                            String _phoneNo = "+" + countryCodePicker.getFullNumber() + _getUserEnteredPhoneNumber;
                            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            PatientHelperClass helperClass = new PatientHelperClass(id, _fname, _email, _age, _phoneNo, _gender, "Location");
                            reference.child(id).setValue(helperClass);
                            Toast.makeText(PatientSignUp.this, "Account Created Successfully.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(),PatientLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            Exception e = task.getException();
                            Toast.makeText(PatientSignUp.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                //FirebaseAuth.getInstance().signOut();
                //finish();
            }
        });
    }

    private boolean validateFullName() {
        String val = fullName.getEditText().getText().toString();
        if (val.isEmpty()) {
            fullName.setError("Field cannot be empty");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
    }

    /*private boolean validateUserName() {
        String val = userName.getEditText().getText().toString();
        //String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            userName.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 20) {
            userName.setError("Username cannot be more than 20 characters");
            return false;
        } /*else if (val.matches(noWhiteSpace)) {
            userName.setError("White spaces are not allowed");
            return false;
        } *//*else {
            userName.setError(null);
            userName.setErrorEnabled(false);
            return true;
        }
    }*/

    private boolean validateEmail() {
        String val = email.getEditText().getText().toString().trim();
        String checkEmail = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            email.setError("Field cannot be empty");
            return false;
        } else  if(!val.matches(checkEmail)){
            email.setError("Invalid Email!");
            return false;
        }
        else {
            email.setError(null);
            email.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        String val = password.getEditText().getText().toString().trim();
        String checkPassword = "^" +
                "(?=.*[0-9])" +                //at least 1 digit
                "(?=.*[a-z])" +                //at least 1 lower case letter;
                "(?=.*[A-Z])" +                //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +               //any letter
                "(?=.*[@#$%^&+=])" +             //at least 1 special character
                "(?=\\S+$)" +                    //no white space;
                ".{8,}" +                        //at least 8 characters
                "$" ;
        if (val.isEmpty()) {
            password.setError("Field cannot be empty");
            return false;
        } else  if(!val.matches(checkPassword)){
            password.setError("Password is too weak");
            return false;
        }
        else {
            password.setError(null);
            password.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateGender() {
        if(radioGroup.getCheckedRadioButtonId()==-1){
            Toast.makeText(this,"Please Select Gender",Toast.LENGTH_SHORT).show();
            return false;
        }else{
            return true;
        }
    }

    private boolean validatePhone() {
        String val = phoneNo.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            phoneNo.setError("Field cannot be empty");
            return false;
        }
        else {
            phoneNo.setError(null);
            phoneNo.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateAge() {
        String val = age.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            age.setError("Field cannot be empty");
            return false;
        }
        else {
            age.setError(null);
            age.setErrorEnabled(false);
            return true;
        }
    }

    public void BackToPatientLoginSignUp(View view) {
        Intent intent = new Intent(this, PatientLoginSignUpSelection.class);
        startActivity(intent);
    }

}
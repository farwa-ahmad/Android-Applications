package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.lang.reflect.Field;

public class DoctorSignUp extends AppCompatActivity {

    TextInputLayout fullName, email,address,qualification,institute, experience,password,age,phoneNo, hospitalName;
    Button signUpdr;
    CountryCodePicker countryCodePicker;
    RadioGroup radioGroup;
    RadioButton radioButton;
    Spinner sSpecialization;
    LinearLayout specialization;

    protected LocationManager locationManager;
    private static final int REQUEST_LOCATION = 1;

    public static double latitude = 0, longitude = 0;
    FusedLocationProviderClient fusedLocationProviderClient;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_sign_up);

        //Hooks
        fullName = findViewById(R.id.fullnamedr);
        //userName = findViewById(R.id.usernamedr);
        email = findViewById(R.id.emaildr);
        address = findViewById(R.id.addressdr);
        qualification = findViewById(R.id.qualificationdr);
        sSpecialization = findViewById(R.id.sSpecialization);
        specialization = findViewById(R.id.specializationdr);
        institute = findViewById(R.id.institutedr);
        experience = findViewById(R.id.experiencedr);
        password = findViewById(R.id.passworddr);
        age = findViewById(R.id.agedr);
        phoneNo = findViewById(R.id.phonenumberdr);
        signUpdr = findViewById(R.id.btnDoctorsignup);
        countryCodePicker = findViewById(R.id.countrycodepickerdr);
        radioGroup = findViewById(R.id.radiogroupdr);
        hospitalName = findViewById(R.id.hospitalNameDr);
        sSpecialization = findViewById(R.id.sSpecialization);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            ListPopupWindow popupWindow = (ListPopupWindow) popup.get(sSpecialization);
            popupWindow.setHeight(500);
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DoctorSignUp.this);
        locHandler();

        //Relationship spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.specializations_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sSpecialization.setAdapter(adapter);

        signUpdr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Doctor");

                if (!validateFullName() | !validateEmail() | !validatePassword() | !validateGender() | !validatePhone() |
                        !validateAddress() | !validateQualification() | !validateInstitute() | !validateAge()| !validateHospital()) {
                    return;
                }
                if (latitude == 0 || longitude == 0){
                    new AlertDialog.Builder(DoctorSignUp.this)
                            .setTitle("Error")
                            .setMessage("Unable to get your location. Press Ok to try again.")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    locHandler();
                                }
                            })
                            .show();
                    return;
                }

                FirebaseAuth mauth = FirebaseAuth.getInstance();
                mauth.createUserWithEmailAndPassword(email.getEditText().getText().toString(),password.getEditText().getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Get all the values
                            String _fname = fullName.getEditText().getText().toString().trim();
                            //String _uname = userName.getEditText().getText().toString().trim();
                            String _email = email.getEditText().getText().toString().trim();
                            String _address = address.getEditText().getText().toString().trim();
                            String _qualification = qualification.getEditText().getText().toString().trim();
                            String _specialization = sSpecialization.getSelectedItem().toString().trim();
                            String _institute = institute.getEditText().getText().toString().trim();
                            String _experience = experience.getEditText().getText().toString().trim();
                            //String _password = password.getEditText().getText().toString().trim();
                            String _age = age.getEditText().getText().toString().trim();
                            radioButton = findViewById(radioGroup.getCheckedRadioButtonId());
                            String _gender = radioButton.getText().toString().trim();
                            String _getUserEnteredPhoneNumber = phoneNo.getEditText().getText().toString().trim();
                            String _phoneNo = "+"+countryCodePicker.getFullNumber()+_getUserEnteredPhoneNumber;
                            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            String _hospital = hospitalName.getEditText().getText().toString().trim();


                            DoctorHelperClass helperClass = new DoctorHelperClass(id,_fname, _email,_address,_qualification,_specialization,_institute,_experience, _age,_phoneNo,_gender, String.valueOf(latitude), String.valueOf(longitude),_hospital);
                            reference.child(id).setValue(helperClass);
                            Toast.makeText(DoctorSignUp.this, "Account Created Successfully.", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(),DoctorLogin.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            Exception e = task.getException();
                            Toast.makeText(DoctorSignUp.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }
    public void MoveBackToDrLoginSignUp(View view) {
        Intent intent = new Intent(this,DoctorLoginSignUpSelection.class);
        startActivity(intent);
    }

    private boolean validateFullName() {
        String val = fullName.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            fullName.setError("Field cannot be empty");
            return false;
        } else {
            fullName.setError(null);
            fullName.setErrorEnabled(false);
            return true;
        }
    }

   /* private boolean validateUserName() {
        String val = userName.getEditText().getText().toString();
        //String noWhiteSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            userName.setError("Field cannot be empty");
            return false;
        } else if (val.length() >= 20) {
            userName.setError("Username cannot be more than 20 characters");
            return false;
        }/* else if (val.matches(noWhiteSpace)) {
            userName.setError("White spaces are not allowed");
            return false;
        }*//* else {
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
            email.setError("Invalid Email");
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

    private boolean validateAddress() {
        String val = address.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            address.setError("Field cannot be empty");
            return false;
        } else {
            address.setError(null);
            address.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateQualification() {
        String val = qualification.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            qualification.setError("Field cannot be empty");
            return false;
        } else {
            qualification.setError(null);
            qualification.setErrorEnabled(false);
            return true;
        }
    }


    private boolean validateInstitute() {
        String val = institute.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            institute.setError("Field cannot be empty");
            return false;
        } else {
            institute.setError(null);
            institute.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateAge() {
        String val = address.getEditText().getText().toString().trim();
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

    private boolean validateHospital() {
        String val = hospitalName.getEditText().getText().toString().trim();
        if (val.isEmpty()) {
            hospitalName.setError("Field cannot be empty");
            return false;
        } else {
            hospitalName.setError(null);
            hospitalName.setErrorEnabled(false);
            return true;
        }
    }




    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("It seemed as your GPS is not enabled. Please enable your GPS so that we can get your nearest cities.").setCancelable(false).setPositiveButton("Ok", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(
                DoctorSignUp.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                DoctorSignUp.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {

            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location locationGPS = task.getResult();
                    if (locationGPS != null) {
                        latitude = locationGPS.getLatitude();
                        longitude = locationGPS.getLongitude();

                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                Location mlocation = locationResult.getLastLocation();
                                latitude = mlocation.getLatitude();
                                longitude = mlocation.getLongitude();

                            }
                        };
                    }



                }
            });

        }
    }

    private void locHandler(){
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }
    }

}
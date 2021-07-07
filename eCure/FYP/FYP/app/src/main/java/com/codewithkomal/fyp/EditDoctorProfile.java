package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.codewithkomal.fyp.databinding.ActivityEditDoctorProfileBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditDoctorProfile extends AppCompatActivity {

    ActivityEditDoctorProfileBinding binding;
    boolean flag1=false;
    String gender;
    DatabaseReference dbRef;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivityEditDoctorProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        id = getIntent().getStringExtra("id");

        //Relationship spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.relationships_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        binding.sSpecialization.setAdapter(adapter);

        //back button
        binding.ivBack.setOnClickListener(v -> {
            startActivity(new Intent(EditDoctorProfile.this, PatientProfileActivity.class));
            finish();
        });

        binding.rbMale.setOnClickListener(v -> {

            flag1 = true;

            //for text color of radio buttons
            binding.rbMale.setTextColor(ContextCompat.getColor(this,R.color.e_cure_blue));
            binding.rbFemale.setTextColor(ContextCompat.getColor(this,R.color.icontintblack));

            gender = "Male";


        });

        binding.rbFemale.setOnClickListener(v -> {

            flag1 = true;

            //for text color of radio buttons
            binding.rbFemale.setTextColor(ContextCompat.getColor(this,R.color.e_cure_blue));
            binding.rbMale.setTextColor(ContextCompat.getColor(this,R.color.icontintblack));

            gender = "Female";
        });



        binding.btnAddProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dbRef = FirebaseDatabase.getInstance().getReference("Patient").child(id);

                boolean flag = false;

                String firstName = binding.etPatientName.getText().toString();
                if (!firstName.equals("")) {
                    dbRef.child("fname").setValue(firstName);
                    flag = true;
                }

                String phone = binding.etPatientPhone.getText().toString();
                if (!phone.equals(""))
                {
                    dbRef.child("phoneNo").setValue(phone);
                    flag = true;
                }

                String age = binding.etPatientAge.getText().toString();
                if (!age.equals("")) {
                    dbRef.child("age").setValue(age);
                    flag = true;
                }
                String experience = binding.etDoctorExperience.getText().toString();
                if (!experience.equals("")) {
                    dbRef.child("experience").setValue(experience);
                    flag = true;
                }
                String hospitalName = binding.etDoctorHospitalName.getText().toString();
                if (!hospitalName.equals("")) {
                    dbRef.child("hospitalName").setValue(hospitalName);
                    flag = true;
                }
                String specialization = binding.sSpecialization.getSelectedItem().toString();
                if (!specialization.equals("")) {
                    dbRef.child("specializationField").setValue(specialization);
                    flag = true;
                }

                if(flag1==true) {

                    dbRef.child("gender").setValue(gender);
                    flag=true;

                }
                if(flag == true)
                {
                    Toast.makeText(EditDoctorProfile.this, "Profile Updated", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(EditDoctorProfile.this, "No changes were made", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed(){
        finish();
    }
}
package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.codewithkomal.fyp.databinding.ActivityEditPatientProfileBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditPatientProfile extends AppCompatActivity {

    ActivityEditPatientProfileBinding binding;
    String gender;
    boolean flag1=false;

    DatabaseReference dbRef;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivityEditPatientProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        id = getIntent().getStringExtra("id");

        //back button
        binding.ivBack.setOnClickListener(v -> {
            startActivity(new Intent(EditPatientProfile.this, PatientProfileActivity.class));
            finish();
        });

        /*
        //Relationship spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.relationships_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        binding.sRelationship.setAdapter(adapter);

         */
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

                if(flag1==true) {

                    dbRef.child("gender").setValue(gender);
                    flag=true;

                }
                if(flag == true)
                {
                    Toast.makeText(EditPatientProfile.this, "Profile Updated", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Toast.makeText(EditPatientProfile.this, "No changes were made", Toast.LENGTH_LONG).show();
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
package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.codewithkomal.fyp.databinding.ActivityPatientProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PatientProfileActivity extends AppCompatActivity {

    ActivityPatientProfileBinding binding;

    DatabaseReference dbRef;
    String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //view binding
        binding = ActivityPatientProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        id = getIntent().getStringExtra("id");

        //back button
        binding.ivBack.setOnClickListener(v -> {
            startActivity(new Intent(PatientProfileActivity.this, MainActivityPatient.class));
            finish();
        });

        binding.tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientProfileActivity.this, EditPatientProfile.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        dbRef = FirebaseDatabase.getInstance().getReference("Patient").child(id);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PatientHelperClass phc = snapshot.getValue(PatientHelperClass.class);
                binding.profileName.setText(phc.getFname());
                binding.profileEmail.setText(phc.getEmail());
                //binding.profileUserName.setText(phc.getUname());
                binding.profilePhoneNo.setText(phc.getPhoneNo());
                binding.profileAge.setText(phc.getAge()+" years old");
                binding.profileGender.setText(phc.getGender());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}

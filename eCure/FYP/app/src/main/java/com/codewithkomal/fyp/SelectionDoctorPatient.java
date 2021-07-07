package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.codewithkomal.fyp.controller.DoctorHome;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SelectionDoctorPatient extends AppCompatActivity {

    Button callDrScreen, callPatientScreen;
    DatabaseReference dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_doctor_patient);

        callDrScreen = findViewById(R.id.dr);
        callPatientScreen = findViewById(R.id.patient);

        dbRef = FirebaseDatabase.getInstance().getReference();

        callDrScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                /*if (user != null){
                    dbRef=dbRef.child("patient").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ////////////////
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists())
                            {
                                Intent i = new Intent(SelectionDoctorPatient.this, DoctorHome.class);
                                startActivity(i);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                    ////////////////

                }*/
                Intent i = new Intent(SelectionDoctorPatient.this, DoctorLoginSignUpSelection.class);
                startActivity(i);

            }
        });

        callPatientScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null){
                    dbRef=dbRef.child("doctor").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    ////////////////
                    dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(!snapshot.exists())
                            {
                                Intent i = new Intent(SelectionDoctorPatient.this, MainActivityPatient.class);
                                startActivity(i);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                    ////////////////

                }*/
                Intent i = new Intent(SelectionDoctorPatient.this, PatientLoginSignUpSelection.class);
                startActivity(i);
            }
        });
    }
}
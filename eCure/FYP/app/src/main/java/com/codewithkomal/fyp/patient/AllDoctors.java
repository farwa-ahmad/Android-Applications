package com.codewithkomal.fyp.patient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.codewithkomal.fyp.DoctorHelperClass;
import com.codewithkomal.fyp.MainActivityPatient;
import com.codewithkomal.fyp.PatientHelperClass;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.SearchLocationActivity;
import com.codewithkomal.fyp.adapters.DoctorsAdapters;
import com.codewithkomal.fyp.adapters.PatientAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class AllDoctors extends AppCompatActivity {

    RecyclerView recyclerView;
    String passed;
    ImageView ivBackAllDoctors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_doctors);

        ivBackAllDoctors = findViewById(R.id.ivvBackAllDoctors);

        passed = getIntent().getStringExtra("s");

        if (passed == null){
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            return;
        }

        //back button
        ivBackAllDoctors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AllDoctors.this, MainActivityPatient.class));
                finish();
            }
        });

        recyclerView = findViewById(R.id.doctorRv);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Doctor");
        dbRef.keepSynced(true);

        Query query = dbRef.orderByChild("specializationField").equalTo(passed);

        FirebaseRecyclerOptions<DoctorHelperClass> options = new FirebaseRecyclerOptions.Builder<DoctorHelperClass>()
                .setQuery(query, DoctorHelperClass.class).build();
        DoctorsAdapters adapter = new DoctorsAdapters(options, AllDoctors.this);

        LinearLayoutManager GLM = new LinearLayoutManager(this);

        GLM.setReverseLayout(true);
        GLM.setStackFromEnd(true);
        recyclerView.setLayoutManager(GLM);
        recyclerView.setHasFixedSize(true);
        adapter.startListening();
        recyclerView.setAdapter(adapter);


    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}
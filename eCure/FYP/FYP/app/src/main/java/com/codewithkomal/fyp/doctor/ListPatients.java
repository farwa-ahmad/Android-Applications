package com.codewithkomal.fyp.doctor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.codewithkomal.fyp.PatientHelperClass;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.adapters.AppointmentAdapter;
import com.codewithkomal.fyp.adapters.PatientAdapter;
import com.codewithkomal.fyp.models.Appointment;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ListPatients extends AppCompatActivity {

    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_patients);


        recyclerView = findViewById(R.id.patientRv);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Patient");
        dbRef.keepSynced(true);
        FirebaseRecyclerOptions<PatientHelperClass> options = new FirebaseRecyclerOptions.Builder<PatientHelperClass>()
                .setQuery(dbRef, PatientHelperClass.class).build();


        PatientAdapter adapter = new PatientAdapter(options);

        LinearLayoutManager GLM = new LinearLayoutManager(this);

        GLM.setReverseLayout(true);
        GLM.setStackFromEnd(true);
        recyclerView.setLayoutManager(GLM);
        recyclerView.setHasFixedSize(true);
        adapter.startListening();
        recyclerView.setAdapter(adapter);

    }
}
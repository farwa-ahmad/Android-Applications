package com.codewithkomal.fyp.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.adapters.AppointmentAdapter;
import com.codewithkomal.fyp.models.Appointment;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class DoctorApointments extends AppCompatActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_apointments);

        recyclerView = findViewById(R.id.appointmentRv);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Appointments");
        dbRef.keepSynced(true);

        Query query = dbRef.orderByChild("status").equalTo("Appointment");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Appointment> list = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Appointment appointment = ds.getValue(Appointment.class);
                    if (appointment.getdId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        list.add(appointment);
                    }
                }
                Collections.reverse(list);
                recyclerView.setLayoutManager(new LinearLayoutManager(DoctorApointments.this));
                recyclerView.setAdapter(new AppointmentAdapter(list, DoctorApointments.this));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
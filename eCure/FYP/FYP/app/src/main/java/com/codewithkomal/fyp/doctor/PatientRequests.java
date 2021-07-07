package com.codewithkomal.fyp.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.adapters.AppointmentAdapter;
import com.codewithkomal.fyp.models.Appointment;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class PatientRequests extends AppCompatActivity {

    RecyclerView recyclerView;
    private int selectedHours, selectedMins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_requests);


        recyclerView = findViewById(R.id.appointmentRv);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Appointments");
        dbRef.keepSynced(true);


        Query query = dbRef.orderByChild("status").equalTo("Request");
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
                recyclerView.setLayoutManager(new LinearLayoutManager(PatientRequests.this));
                AppointmentAdapter adapter = new AppointmentAdapter(list, PatientRequests.this);
                recyclerView.setAdapter(adapter);


                adapter.setOnItemClickListener(new AppointmentAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PatientRequests.this);
                        ViewGroup viewGroup = findViewById(android.R.id.content);
                        View dialogView = LayoutInflater.from(PatientRequests.this).inflate(R.layout.confirm_appointment, viewGroup, false);

                        builder.setView(dialogView);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();

                        Button cancel = dialogView.findViewById(R.id.cancelBtn);
                        Button add = dialogView.findViewById(R.id.confirmBtn);

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Appointments").child(list.get(position).getaId());
                                new AlertDialog.Builder(PatientRequests.this)
                                        .setTitle("Confirmation")
                                        .setMessage("Are you sure want to cancel this request. I will be deleted permanently.")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dbRef.removeValue();
                                            }
                                        })

                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton(android.R.string.no, null)
                                        .show();
                            }
                        });

                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(PatientRequests.this)
                                        .setTitle("Confirmation")
                                        .setMessage("Are you sure you want to give appointment to the patient?")

                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                selectTime(list.get(position).getaId(), list.get(position).getTime());
                                            }
                                        })

                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton(android.R.string.no, null)
                                        .show();
                            }
                        });
                    }
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void selectTime(String strId, String date){
        final Calendar c = Calendar.getInstance();
        selectedHours = c.get(Calendar.HOUR_OF_DAY);
        selectedMins = c.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        selectedHours = hourOfDay;
                        selectedMins = minute;
                        DatabaseReference aRef = FirebaseDatabase.getInstance().getReference("Appointments").child(strId);
                        aRef.child("status").setValue("Appointment");
                        aRef.child("time").setValue(date+ " "+selectedHours+":"+selectedMins);
                        Toast.makeText(PatientRequests.this, "Appointment Confirmed", Toast.LENGTH_SHORT).show();
                    }
                }, selectedHours, selectedMins, false);
        timePickerDialog.show();
    }
}
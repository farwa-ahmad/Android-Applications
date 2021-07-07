package com.codewithkomal.fyp.doctor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.SolverVariableValues;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.codewithkomal.fyp.DoctorHelperClass;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.models.Appointment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class Profile extends AppCompatActivity  implements OnMapReadyCallback {

    TextView name, speciality, institute, institudeAddress, experience, qualification, phone, email;
    String id, latitude = "0", longitude = "0";
    Button appo ;
    String selectedDateTime = "";
    int selectedYear, selectedMonth, selectedDay;
    SupportMapFragment supportMapFragment;
    Appointment appointment ;
    DatabaseReference aRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = getIntent().getStringExtra("id");

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert supportMapFragment != null;

        name = findViewById(R.id.docname);
        speciality = findViewById(R.id.specialistText);
        institute = findViewById(R.id.institute);
        institudeAddress = findViewById(R.id.address);
        experience = findViewById(R.id.experienceshow);
        qualification = findViewById(R.id.qualificationshow);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        appo = findViewById(R.id.appointmentBookBtn);
        appointment = new Appointment();

        if (id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            appo.setVisibility(View.GONE);
        }else{
            appo.setVisibility(View.VISIBLE);
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Doctor").child(id);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DoctorHelperClass dhc = snapshot.getValue(DoctorHelperClass.class);
                name.setText(dhc.getFullName());
                speciality.setText(dhc.getSpecializationField());
                institute.setText(dhc.getInstituteName());
                institudeAddress.setText(dhc.getAddress());
                experience.setText(dhc.getExperience()+ "years");
                qualification.setText(dhc.getQualification());
                phone.setText(dhc.getPhoneNo());
                email.setText(dhc.getEmail());
                latitude = dhc.getLatitude();
                longitude = dhc.getLongitude();

                supportMapFragment.getMapAsync(Profile.this);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        appo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectDate();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Patient").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                         aRef = FirebaseDatabase.getInstance().getReference("Appointments");

                        appointment.setdId(id);
                        appointment.setpId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        appointment.setpName(snapshot.child("fname").getValue(String.class));
                        appointment.setTime(selectedDateTime);
                        appointment.setP_phone(snapshot.child("phoneNo").getValue(String.class));
                        appointment.setD_name(name.getText().toString());
                        appointment.setaId(aRef.push().getKey());
                        appointment.setStatus("Request");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (latitude == null || longitude == null){
            latitude = "0";
            longitude = "0";
        }
        Double flat = Double.parseDouble(latitude);
        Double flon = Double.parseDouble(longitude);

        LatLng latLng = new LatLng(flat, flon);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Here is the doctor");
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5f));
        googleMap.addMarker(markerOptions);
    }

    private void selectDate(){

        final Calendar c = Calendar.getInstance();
        selectedYear = c.get(Calendar.YEAR);
        selectedMonth = c.get(Calendar.MONTH);
        selectedDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                selectedDateTime = (monthOfYear + 1) + "-" + dayOfMonth + "-" + year;

                showCustomDialog();
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, listener, selectedYear, selectedMonth, selectedDay);
        datePickerDialog.setCancelable(false);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() + 86400000);
        datePickerDialog.show();
    }

    public void showCustomDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(Profile.this).inflate(R.layout.appointment_note_dialog, viewGroup, false);

        builder.setView(dialogView);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

        EditText note = dialogView.findViewById(R.id.noteEt);
        Button saveBtn = dialogView.findViewById(R.id.saveBtn);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (note.getText().toString().trim().isEmpty()){
                    note.setError("Required");
                    return;
                }

                appointment.setNote(note.getText().toString());


                aRef.child(appointment.getaId()).setValue(appointment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(Profile.this, "Appointment Request Sent.", Toast.LENGTH_SHORT).show();
                        }else{
                            Exception e = task.getException();
                            Toast.makeText(Profile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                alertDialog.dismiss();
            }
        });
    }

}
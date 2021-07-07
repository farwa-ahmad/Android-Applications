package com.codewithkomal.fyp.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codewithkomal.fyp.Common.Common;
import com.codewithkomal.fyp.DoctorLogin;
import com.codewithkomal.fyp.EditDoctorProfile;
import com.codewithkomal.fyp.EditPatientProfile;
import com.codewithkomal.fyp.PatientProfileActivity;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.SelectionDoctorPatient;
import com.codewithkomal.fyp.doctor.DoctorApointments;
import com.codewithkomal.fyp.doctor.ListPatients;
import com.codewithkomal.fyp.doctor.PatientRequests;
import com.codewithkomal.fyp.doctor.Profile;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;

public class DoctorHome extends AppCompatActivity  implements DatePickerDialog.OnDateSetListener{

        static String doc;
        Button SignOutBtn2;
        Button BtnRequst;
        Button listPatients;
        Button appointementBtn;
        ImageView ivEditProfile;
        TextView tvEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        Common.CurreentDoctor = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
        Common.CurrentUserType = "doctor";
        listPatients = findViewById(R.id.listPatients);
        BtnRequst=findViewById(R.id.btnRequst);
        SignOutBtn2=findViewById(R.id.signOutBtn);
        appointementBtn = findViewById(R.id.appointment);
        ivEditProfile = findViewById(R.id.ivEditProfile);
        tvEditProfile = findViewById(R.id.tvEditProfile);

        ivEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorHome.this, EditDoctorProfile.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        tvEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DoctorHome.this, EditDoctorProfile.class);
                intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                startActivity(intent);
            }
        });

        SignOutBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), SelectionDoctorPatient.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                FirebaseAuth.getInstance().signOut();
                finish();

            }
        });

        BtnRequst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent k = new Intent(DoctorHome.this, PatientRequests.class);
                startActivity(k);
            }
        });

        listPatients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent k = new Intent(DoctorHome.this, ListPatients.class);
                startActivity(k);
            }
        });

        appointementBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 // doc = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();
                //showDatePickerDialog(v.getContext());
                Intent k = new Intent(DoctorHome.this, DoctorApointments.class);
                startActivity(k);
            }
        });
    }

    public void profileBtnClick(View view) {

        Intent k = new Intent(DoctorHome.this, Profile.class);
        k.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(k);
    }


    public void myCalendarOnclick(View view) {
        Toast.makeText(this, "My Calender Click", Toast.LENGTH_SHORT).show();
        /*Intent k = new Intent(DoctorHome.this, MyCalendarDoctorActivity.class);
        startActivity(k);*/
    }

    public void showDatePickerDialog(Context wf){
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                wf,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }


    private void openPage(Context wf, String d,String day){
        Toast.makeText(wf, "Open Page method", Toast.LENGTH_SHORT).show();
        /* Intent i = new Intent(wf, AppointementActivity.class);
        i.putExtra("key1",d+"");
        i.putExtra("key2",day);
        i.putExtra("key3","doctor");
        wf.startActivity(i);*/
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = "month_day_year: " + month + "_" + dayOfMonth + "_" + year;
        openPage(view.getContext(),doc,date);
    }
}
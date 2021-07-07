package com.codewithkomal.fyp.doctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.codewithkomal.fyp.MainActivityPatient;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.SearchLocationActivity;

public class AppointmentBook extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_book);

    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}
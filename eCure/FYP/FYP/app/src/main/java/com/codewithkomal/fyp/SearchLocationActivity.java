package com.codewithkomal.fyp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.codewithkomal.fyp.MainActivityPatient;
import com.codewithkomal.fyp.databinding.ActivitySearchLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SearchLocationActivity extends AppCompatActivity {

    ActivitySearchLocationBinding binding;
    //for current location
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        //view binding
        binding = ActivitySearchLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //back button
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchLocationActivity.this, MainActivityPatient.class));
                finish();
            }
        });

        //search on GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SearchLocationActivity.this);
        binding.ivSearchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Checking permission
                if(ActivityCompat.checkSelfPermission(SearchLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    //When permission granted
                    fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            //Initialise Location
                            Location location = task.getResult();
                            if(location!=null)
                            {

                                try {
                                    Geocoder geocoder = new Geocoder(SearchLocationActivity.this, Locale.getDefault());
                                    //Initialize address list
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    //Set Locality on textview of MainActivityPatient
                                    Intent intent = new Intent(SearchLocationActivity.this, MainActivityPatient.class);
                                    intent.putExtra("locality", Html.fromHtml(addresses.get(0).getLocality()));
                                    startActivity(intent);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                }
                else
                {
                    //When permission denied
                    ActivityCompat.requestPermissions(SearchLocationActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
                }

            }
        });


    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }


}
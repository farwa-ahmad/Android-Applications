package com.example.e_cure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.example.e_cure.databinding.ActivitySearchLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SearchLocationActivity extends AppCompatActivity {

    ActivitySearchLocationBinding binding;
    //for current location
    FusedLocationProviderClient fusedLocationProviderClient;

    //CurrentLocationModel currentLocationModel;
    //DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        //view binding
        binding = ActivitySearchLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //database
        //myDb = new DatabaseHelper(this);

        //back button
        binding.ivBack.setOnClickListener(v -> {
            startActivity(new Intent(SearchLocationActivity.this, PatientMainActivity.class));
            finish();
        });

        //search on GPS
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(SearchLocationActivity.this);
        binding.ivSearchLocation.setOnClickListener(v -> {
            //Checking permission
            if(ActivityCompat.checkSelfPermission(SearchLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                //When permission granted
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                    //Initialise Location
                    Location location = task.getResult();
                    if(location!=null)
                    {

                        try {
                            Geocoder geocoder = new Geocoder(SearchLocationActivity.this, Locale.getDefault());
                            //Initialize address list
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            //Set Locality on textview of MainActivity
                            Intent intent = new Intent(SearchLocationActivity.this, PatientMainActivity.class);
                            /*//database
                            boolean isUpdated = myDb.updateData("0", addresses.get(0).getLatitude(),addresses.get(0).getLongitude(),addresses.get(0).getLocality());

                            if(isUpdated==true)
                                Toast.makeText(SearchLocationActivity.this,"Location updated",Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(SearchLocationActivity.this,"No data updated",Toast.LENGTH_SHORT).show();*/
                            startActivity(intent);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                });
            }
            else
            {
                //When permission denied
                ActivityCompat.requestPermissions(SearchLocationActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
            }

        });


    }
    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
    
}
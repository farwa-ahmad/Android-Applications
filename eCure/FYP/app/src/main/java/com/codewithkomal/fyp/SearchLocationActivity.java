package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.codewithkomal.fyp.databinding.ActivitySearchLocationBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class SearchLocationActivity extends AppCompatActivity {

    ActivitySearchLocationBinding binding;
    //for current location
    FusedLocationProviderClient fusedLocationProviderClient;

    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivitySearchLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbRef = FirebaseDatabase.getInstance().getReference("Patient").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

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

                                    Toast.makeText(SearchLocationActivity.this, addresses.get(0).getLocality().toString(),Toast.LENGTH_SHORT).show();
                                    dbRef.child("location").setValue(addresses.get(0).getLocality()+" is selected");

                                    /*//Set Locality on textview of MainActivityPatient
                                    Intent intent = new Intent(SearchLocationActivity.this, MainActivityPatient.class);
                                    intent.putExtra("locality", Html.fromHtml(addresses.get(0).getLocality()));
                                    startActivity(intent);*/

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

        //listview cities
        String[] StringArray = {"Lahore","Karachi","Islamabad","Rawalpindi","Multan","Faisalabad","Peshawar","Gujaranwala","Bahawalpur"};
        ArrayAdapter adapter = new ArrayAdapter<String>(this,R.layout.activity_listview,StringArray);
        binding.lvCities.setAdapter(adapter);

        binding.lvCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(SearchLocationActivity.this, parent.getItemAtPosition(position).toString()+" is selected",Toast.LENGTH_SHORT).show();
                dbRef.child("location").setValue(parent.getItemAtPosition(position).toString());
            }
        });

    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        startActivity(new Intent(SearchLocationActivity.this, MainActivityPatient.class));
        finish();
    }


}
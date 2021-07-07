package com.codewithkomal.fyp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.codewithkomal.fyp.Fragments.PAppointmentsFragment;
import com.codewithkomal.fyp.Fragments.PHomeFragment;
import com.codewithkomal.fyp.Fragments.PAppRequestsFragment;
import com.codewithkomal.fyp.databinding.ActivityMainPatientBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivityPatient extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    ActivityMainPatientBinding binding;

    //DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivityMainPatientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateNavHeader();

        /*//image view logout
        binding.ivLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityPatient.this,SelectionDoctorPatient.class));
                finish();
            }
        });*/

        //myDb = new DatabaseHelper(this);

        /*binding.ivLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Cursor result = myDb.getAllData();

                if (result.getCount() == 0) {
                    binding.tvLocation.setText("Location");
                    return;
                }

                binding.tvLocation.setText(result.getString(3));

                result.close();
                myDb.close();

            }

        });*/


        //To remove default tinting from bottom navigation icons
        binding.bottomNavView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        binding.bottomNavView.setItemIconTintList(null);

        //To make home screen fragment appear on opening the app
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new PHomeFragment()).commit();

        //drawer layout
        setSupportActionBar(binding.toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        //drawer items
        binding.drawerNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //on selecting patient profiles move to PatientProfilesActivity
                    case R.id.drawer_nav_patient_profiles:
                        Intent intent = new Intent(MainActivityPatient.this, PatientProfileActivity.class);
                        intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        startActivity(intent);
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //on selecting patient profiles move to RecentDoctorsActivity
                    /*case R.id.drawer_nav_recent_doctors:
                        startActivity(new Intent(MainActivityPatient.this,RecentDoctorsActivity.class));
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;*/
                    //on selecting logout and move to sign in sign up screen
                    case R.id.drawer_nav_patient_logout:
                        startActivity(new Intent(MainActivityPatient.this,SelectionDoctorPatient.class));
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        FirebaseAuth.getInstance().signOut();
                        finish();
                        break;
                }
                return true;
            }
        });

        /*
        //location toolbar
        binding.ivLocationMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityPatient.this,SearchLocationActivity.class));
            }
        });*/

    }

    //To make the drawer close, if it is open, on clicking the back button
    @Override
    public void onBackPressed() {
        if(binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    //Bottom Navigation
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment selectedFragment = null;

            switch(item.getItemId())
            {
                case R.id.navHome:
                    selectedFragment = new PHomeFragment();
                    break;
                case R.id.navPatientAppointments:
                    selectedFragment = new PAppointmentsFragment();
                    break;
                case R.id.navPatientRecords:
                    selectedFragment = new PAppRequestsFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();

            return true;
        }
    };

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }

    public void updateNavHeader() {

        NavigationView navigationView = findViewById(R.id.drawerNav);
        View headerView = navigationView.getHeaderView(0);
        TextView navName = headerView.findViewById(R.id.tvPatientName);
        TextView navEmail = headerView.findViewById(R.id.tvPatientEmail);
        TextView navPatientPhoneNo = headerView.findViewById(R.id.tvPatientPhoneNumber);

        DatabaseReference dbRef;
        dbRef = FirebaseDatabase.getInstance().getReference("Patient").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PatientHelperClass phc = snapshot.getValue(PatientHelperClass.class);
                navName.setText(phc.getFname());
                navEmail.setText(phc.getEmail());
                navPatientPhoneNo.setText(phc.getPhoneNo());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

        // now we will use Glide to load user image
        // first we need to import the library

        //Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhot);




    }

   /* public void CallPatientProfile(View view) {
        Intent intent = new Intent(this, PatientProfileActivity.class);
        intent.putExtra("id", FirebaseAuth.getInstance().getCurrentUser().getUid());
        startActivity(intent);
    }*/
}
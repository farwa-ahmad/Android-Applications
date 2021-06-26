package com.example.e_cure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.e_cure.Fragments.PAppointmentsFragment;
import com.example.e_cure.Fragments.PHomeFragment;
import com.example.e_cure.Fragments.PMedRecordsFragment;
import com.example.e_cure.databinding.PatientActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class PatientMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    PatientActivityMainBinding binding;

    //DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = PatientActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                        startActivity(new Intent(PatientMainActivity.this,PatientProfilesActivity.class));
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //on selecting patient profiles move to RecentDoctorsActivity
                    case R.id.drawer_nav_recent_doctors:
                        startActivity(new Intent(PatientMainActivity.this,RecentDoctorsActivity.class));
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    //on selecting logout and move to sign in sign up screen
                    case R.id.drawer_nav_patient_logout:
                        Toast.makeText(PatientMainActivity.this, "Logout is clicked", Toast.LENGTH_SHORT).show();
                        binding.drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });

        //location toolbar
        binding.ivLocationMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PatientMainActivity.this,SearchLocationActivity.class));
            }
        });

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
                    selectedFragment = new PMedRecordsFragment();
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

}
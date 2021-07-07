package com.codewithkomal.fyp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.codewithkomal.fyp.adapters.DoctorsAdapters;
import com.codewithkomal.fyp.databinding.ActivityFindDoctorBinding;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FindDoctorActivity extends AppCompatActivity {

    ActivityFindDoctorBinding binding;
    DoctorsAdapters adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_find_doctor);

        //view binding
        binding = ActivityFindDoctorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //back button
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FindDoctorActivity.this, MainActivityPatient.class));
                finish();
            }
        });

        //to display doctors in RecyclerView
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Doctor");
        dbRef.keepSynced(true);

        FirebaseRecyclerOptions<DoctorHelperClass> options = new FirebaseRecyclerOptions.Builder<DoctorHelperClass>().setQuery(dbRef,DoctorHelperClass.class).build();
        adapter = new DoctorsAdapters(options, FindDoctorActivity.this);

        binding.rvListDoctors.setLayoutManager(new LinearLayoutManager(this));

        //binding.rvListDoctors.setHasFixedSize(true);
        adapter.startListening();
        binding.rvListDoctors.setAdapter(adapter);


        //for search functionality
        binding.svSearchDoctor.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProcess(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProcess(newText);
                return false;
            }
        });

    }

    private void searchProcess(String s)
    {
        FirebaseRecyclerOptions<DoctorHelperClass> options =
                new FirebaseRecyclerOptions.Builder<DoctorHelperClass>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("Doctor").orderByChild("fullName").startAt(s.toUpperCase()).endAt(s.toLowerCase()+"\uf8ff"), DoctorHelperClass.class)
                        .build();

        adapter = new DoctorsAdapters(options, FindDoctorActivity.this);
        adapter.startListening();
        binding.rvListDoctors.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }


    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }

}
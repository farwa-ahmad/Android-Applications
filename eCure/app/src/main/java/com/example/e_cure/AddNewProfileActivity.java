package com.example.e_cure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.e_cure.databinding.ActivityAddNewProfileBinding;


public class AddNewProfileActivity extends AppCompatActivity {

    ActivityAddNewProfileBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_profile);

        //view binding
        binding = ActivityAddNewProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //back button
        binding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddNewProfileActivity.this,PatientProfilesActivity.class));
                finish();
            }
        });

        //Relationship spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.relationships_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        binding.sRelationship.setAdapter(adapter);

        binding.rbMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //for text color of radio buttons
                binding.rbMale.setTextColor(getColor(R.color.e_cure_blue));
                binding.rbFemale.setTextColor(getColor(R.color.icontintblack));



            }
        });

        binding.rbFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //for text color of radio buttons
                binding.rbFemale.setTextColor(getColor(R.color.e_cure_blue));
                binding.rbMale.setTextColor(getColor(R.color.icontintblack));


            }
        });



    }

    //To make the activity end on clicking the back button
    @Override
    public void onBackPressed() {
        finish();
    }
}


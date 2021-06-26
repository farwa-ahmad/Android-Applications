package com.example.e_cure.Fragments;

import android.content.Intent;
import android.os.Bundle;


import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.e_cure.FindDoctorActivity;
import com.example.e_cure.databinding.FragmentPHomeBinding;

public class PHomeFragment extends Fragment {

    FragmentPHomeBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPHomeBinding.inflate(inflater,container,false);

        //to open FindDoctorActivity on clicking
        binding.etSearchDoctor.setOnClickListener(v -> startActivity(new Intent(getContext(), FindDoctorActivity.class)));

        return binding.getRoot();
    }

}
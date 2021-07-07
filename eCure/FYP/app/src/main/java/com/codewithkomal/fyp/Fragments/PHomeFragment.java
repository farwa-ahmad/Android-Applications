package com.codewithkomal.fyp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.patient.AllDoctors;
import com.codewithkomal.fyp.FindDoctorActivity;


public class PHomeFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_p_home, container, false);

        EditText etHomeSearchDoctor = view.findViewById(R.id.etHomeSearchDoctor);

        RelativeLayout skin = view.findViewById(R.id.rlSkinSpecialist);
        RelativeLayout consultant = view.findViewById(R.id.rlConsultantPhysician);
        RelativeLayout child = view.findViewById(R.id.rlChildSpecialist);
        RelativeLayout orthopedic = view.findViewById(R.id.rlOrthopedicSurgeon);
        RelativeLayout eye = view.findViewById(R.id.rlEyeSpecialist);
        RelativeLayout dentist = view.findViewById(R.id.rlDentist);
        RelativeLayout generalPhysician = view.findViewById(R.id.rlGeneralPhysician);
        RelativeLayout neurologist = view.findViewById(R.id.rlNeurologist);
        RelativeLayout gynecologist = view.findViewById(R.id.rlGynecologist);
        RelativeLayout psychologist = view.findViewById(R.id.rlPsychologist);
        RelativeLayout heartSpecialist = view.findViewById(R.id.rlHeartSpecialist);
        RelativeLayout kidneySpecialist = view.findViewById(R.id.rlKidneySpecialist);
        RelativeLayout generalSurgeon = view.findViewById(R.id.rlGeneralSurgeon);
        RelativeLayout pulmonologist = view.findViewById(R.id. rlPulmonologist);
        RelativeLayout Urologist = view.findViewById(R.id.rlUrologist);
        RelativeLayout endocrinologist = view.findViewById(R.id.rlEndocrinologist);
        RelativeLayout entSpecialist = view.findViewById(R.id.rlENTSpecialist);
        RelativeLayout gastroenterologist = view.findViewById(R.id.rlGastroenterologist);

        //to open FindDoctorActivity on clicking
        etHomeSearchDoctor.setOnClickListener(v -> startActivity(new Intent(getContext(), FindDoctorActivity.class)));


        skin.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Skin Specialist");
            startActivity(it);
        });
        consultant.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Consultant Physician");
            startActivity(it);
        });
        child.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Child Specialist");
            startActivity(it);
        });
        orthopedic.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Orthopedic Surgeon");
            startActivity(it);
        });
        eye.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Eye Specialist");
            startActivity(it);
        });
        dentist.setOnClickListener(v -> {

            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Dentist");
            startActivity(it);
        });
        generalPhysician.setOnClickListener(v -> {

            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "General Physician");
            startActivity(it);
        });
        neurologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Neurologist");
            startActivity(it);
        });
        gynecologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Gynecologist");
            startActivity(it);
        });
        psychologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Psychologist");
            startActivity(it);
        });
        heartSpecialist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Heart Specialist");
            startActivity(it);
        });
        kidneySpecialist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Kidney Specialist");
            startActivity(it);
        });
        generalSurgeon.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "General Surgeon");
            startActivity(it);
        });
        pulmonologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Pulmonologist");
            startActivity(it);
        });
        Urologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Urologist");
            startActivity(it);
        });
        endocrinologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Endocrinologist");
            startActivity(it);
        });
        entSpecialist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "ENT Specialist");
            startActivity(it);
        });
        gastroenterologist.setOnClickListener(v -> {
            Intent it = new Intent(getContext(), AllDoctors.class);
            it.putExtra("s", "Gastroenterologist");
            startActivity(it);
        });

        return view;
    }
}
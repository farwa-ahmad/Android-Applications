package com.codewithkomal.fyp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithkomal.fyp.PatientHelperClass;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.adapters.AppointmentAdapter;
import com.codewithkomal.fyp.adapters.PatientAdapter;
import com.codewithkomal.fyp.databinding.AppointmentItemBinding;
import com.codewithkomal.fyp.models.Appointment;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PAppointmentsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PAppointmentsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PAppointmentsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PAppointmentsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PAppointmentsFragment newInstance(String param1, String param2) {
        PAppointmentsFragment fragment = new PAppointmentsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_p_appointments, container, false);

        RecyclerView recyclerView = v.findViewById(R.id.appointmentRv);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Appointments");
        dbRef.keepSynced(true);


        Query query = dbRef.orderByChild("status").equalTo("Appointment");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Appointment> list = new ArrayList<>();
                for (DataSnapshot ds: snapshot.getChildren()){
                    Appointment appointment = ds.getValue(Appointment.class);
                    if (appointment.getpId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        list.add(appointment);
                    }
                }
                Collections.reverse(list);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new AppointmentAdapter(list, getContext()));

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return v;
    }
}
package com.codewithkomal.fyp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithkomal.fyp.PatientHelperClass;
import com.codewithkomal.fyp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class PatientAdapter extends FirebaseRecyclerAdapter<PatientHelperClass, PatientAdapter.PatientViewHolder> {


    public PatientAdapter(@NonNull  FirebaseRecyclerOptions<PatientHelperClass> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull  PatientAdapter.PatientViewHolder holder, int position, @NonNull PatientHelperClass model) {
        holder.name.setText(model.getFname());
        holder.phone.setText(model.getPhoneNo());
        holder.email.setText(model.getEmail());
    }

    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item, parent, false);
        return new PatientViewHolder(view);
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder{

        TextView name, phone, email;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            phone = itemView.findViewById(R.id.phone);
            email = itemView.findViewById(R.id.email);
        }
    }
}

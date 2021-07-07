package com.codewithkomal.fyp.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithkomal.fyp.DoctorHelperClass;
import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.doctor.Profile;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;

public class DoctorsAdapters extends FirebaseRecyclerAdapter<DoctorHelperClass, DoctorsAdapters.DoctorViewHolder> {

    Context context;

    public DoctorsAdapters(@NonNull FirebaseRecyclerOptions<DoctorHelperClass> options, Context context) {
        super(options);
        this.context = context;
    }

    //combining data from model to view
    @Override
    public void onBindViewHolder(@NonNull  DoctorsAdapters.DoctorViewHolder holder, int position, @NonNull DoctorHelperClass model) {
        holder.name.setText(model.getFullName());
        holder.tvDrItemDetails.setText(model.getSpecializationField()+"\n"+model.getQualification()+"\n"+model.getHospitalName()+"\n"+model.getEmail());
        //holder.qualification.setText(model.getQualification());
        //holder.hospitalName.setText(model.getHospital());



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(context, Profile.class);
                it.putExtra("id", model.getUserId());
                context.startActivity(it);
            }
        });

        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + model.getPhoneNo()));
                    context.startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(context, "Error" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Converting the xml doctor item into view
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.doctor_item, parent, false);
        return new DoctorViewHolder(view);
    }

    //recyclerview single item
    public class DoctorViewHolder extends RecyclerView.ViewHolder{

        TextView name, tvDrItemDetails;//, qualification, hospitalName;
        ImageView call;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.doctorName);
            //hospitalName = itemView.findViewById(R.id.tvDrItemHospitalName);
            tvDrItemDetails = itemView.findViewById(R.id.tvDrItemDetails);
            //qualification = itemView.findViewById(R.id.tvDrItemQualification);
            call = itemView.findViewById(R.id.callBtn);
        }
    }
}

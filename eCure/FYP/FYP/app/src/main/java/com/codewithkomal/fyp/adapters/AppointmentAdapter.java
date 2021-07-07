package com.codewithkomal.fyp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codewithkomal.fyp.R;
import com.codewithkomal.fyp.models.Appointment;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    Context context;
    OnItemClickListener listener;
    ArrayList<Appointment> mlist = new ArrayList<>();
    public AppointmentAdapter(ArrayList<Appointment> list, Context context){
        this.mlist = list;
        this.context = context;
    }



    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_item, parent, false);
        return new AppointmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  AppointmentAdapter.AppointmentViewHolder holder, int position) {
        Appointment model = mlist.get(position);
        holder.pname.setText(model.getpName());
        holder.dname.setText(model.getD_name());
        holder.date.setText(model.getTime());
        holder.pnumber.setText(model.getP_phone());
        holder.note.setText(model.getNote());
    }

    @Override
    public int getItemCount() {
        return mlist.size();
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder{

        TextView pname, dname, date, note, pnumber;

        public AppointmentViewHolder(@NonNull  View itemView) {
            super(itemView);
            pname = itemView.findViewById(R.id.patient_name);
            dname = itemView.findViewById(R.id.doctor_name);
            date = itemView.findViewById(R.id.date);
            note = itemView.findViewById(R.id.note);
            pnumber = itemView.findViewById(R.id.patient_number);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (position != -1 && listener != null) {

                        listener.onItemClick(position);

                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(AppointmentAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}

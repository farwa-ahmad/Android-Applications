package com.example.mylist;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mylist.Adapters.TaskAdapter;
import com.example.mylist.Models.TaskModel;
import com.example.mylist.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener{

    ActivityMainBinding binding;
    private FirebaseFirestore firestore;
    private TaskAdapter taskAdapter;
    private List<TaskModel> mList;
    private Query query;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //view binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //FireStoreInstance
        firestore = FirebaseFirestore.getInstance();


        //animated background
        AnimationDrawable animationDrawable = (AnimationDrawable) binding.rlBackground.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        //recycler view linking with task_layout.xml
        binding.rvTasks.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        //Floating button to add tasks
        binding.fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(),AddNewTask.TAG);

            }
        });

        mList = new ArrayList<>();

        taskAdapter = new TaskAdapter(MainActivity.this, mList);
        binding.rvTasks.setAdapter(taskAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(taskAdapter));
        itemTouchHelper.attachToRecyclerView(binding.rvTasks);
        showData();
    }

    private void showData(){
        query = firestore.collection("task").orderBy("time" , Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String id = documentChange.getDocument().getId();
                        TaskModel taskModel = documentChange.getDocument().toObject(TaskModel.class).withId(id);
                        mList.add(taskModel);
                        taskAdapter.notifyDataSetChanged();
                    }
                }
                listenerRegistration.remove();

            }

        });
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        mList.clear();
        showData();
        taskAdapter.notifyDataSetChanged();
    }
}
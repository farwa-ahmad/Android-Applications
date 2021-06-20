package com.example.mylist;


import android.app.AlertDialog;
import android.content.DialogInterface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylist.Adapters.TaskAdapter;

//import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;



public class TouchHelper extends ItemTouchHelper.SimpleCallback {
    private TaskAdapter taskAdapter;

    public TouchHelper(TaskAdapter taskAdapter) {
        super(0 , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT); //for dragging and swiping
        this.taskAdapter = taskAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.RIGHT){
            AlertDialog.Builder builder = new AlertDialog.Builder(taskAdapter.getContext());
            builder.setMessage("Are you sure you want to delete this task?")
                    .setTitle("Delete Task")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            taskAdapter.deleteTask(position);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    taskAdapter.notifyItemChanged(position);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }else{
            taskAdapter.editTask(position);
        }
    }

    /*@Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


        new RecyclerViewSwipeDecorator.Builder(c,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive)
                .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
                .addSwipeRightBackgroundColor(Color.RED)
                .addSwipeLeftActionIcon(R.drawable.ic_baseline_edit_24)
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(taskAdapter.getContext() , R.color.edit_color))
                .create()
                .decorate();
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }*/
}
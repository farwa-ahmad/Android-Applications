package com.example.mylist;

import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mylist.Adapters.TaskAdapter;

public class TouchHelper extends ItemTouchHelper.SimpleCallback {

    private final TaskAdapter taskAdapter;

    public TouchHelper(TaskAdapter taskAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.taskAdapter = taskAdapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (direction == ItemTouchHelper.RIGHT) {
            new AlertDialog.Builder(taskAdapter.getContext())
                    .setTitle(R.string.delete_task_title)
                    .setMessage(R.string.delete_task_message)
                    .setPositiveButton(R.string.yes, (dialog, which) ->
                            taskAdapter.requestDelete(position)
                    )
                    .setNegativeButton(R.string.no, (dialog, which) ->
                            taskAdapter.restoreItem(position)
                    )
                    .setOnCancelListener(dialog -> taskAdapter.restoreItem(position))
                    .show();
        } else {
            taskAdapter.requestEdit(position);
        }
    }
}

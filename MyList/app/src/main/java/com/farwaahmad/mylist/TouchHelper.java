package com.farwaahmad.mylist;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.farwaahmad.mylist.adapter.TaskAdapter;
import com.farwaahmad.mylist.model.TaskModel;

public class TouchHelper extends ItemTouchHelper.SimpleCallback {

    private final TaskAdapter taskAdapter;
    private static final int ACTION_OVERLAP_DP = 12;

    private final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public TouchHelper(TaskAdapter taskAdapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.taskAdapter = taskAdapter;

        textPaint.setColor(ContextCompat.getColor(taskAdapter.getContext(), R.color.white));
        textPaint.setTextSize(spToPx(16));
        textPaint.setFakeBoldText(true);
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getBindingAdapterPosition();
        return taskAdapter.isTaskPosition(position)
                ? super.getSwipeDirs(recyclerView, viewHolder)
                : 0;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        viewHolder.itemView.post(taskAdapter::hideSwipeHint);

        TaskModel task = taskAdapter.getTaskAt(position);
        if (task == null) {
            taskAdapter.restoreItem(position);
            return;
        }

        if (direction == ItemTouchHelper.LEFT) {
            // The task identity was captured before the row can be reordered.
            // Firestore's local snapshot removes the item immediately; Undo can
            // recreate this exact document if the user changes their mind.
            taskAdapter.requestDelete(task);
        } else {
            taskAdapter.requestEdit(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas canvas,
                            @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            View itemView = viewHolder.itemView;

            if (dX < 0) {
                drawDeleteCue(canvas, itemView, dX);
            } else if (dX > 0) {
                drawEditCue(canvas, itemView, dX);
            }
        }

        super.onChildDraw(
                canvas,
                recyclerView,
                viewHolder,
                dX,
                dY,
                actionState,
                isCurrentlyActive
        );
    }

    private void drawDeleteCue(@NonNull Canvas canvas, @NonNull View itemView, float dX) {
        backgroundPaint.setColor(
                ContextCompat.getColor(taskAdapter.getContext(), R.color.delete_color)
        );
        float overlap = dpToPx(ACTION_OVERLAP_DP);
        canvas.drawRect(
                itemView.getRight() + dX - overlap,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom(),
                backgroundPaint
        );

        drawAction(
                canvas,
                itemView,
                R.drawable.ic_baseline_delete_24,
                R.string.swipe_delete,
                false,
                dX
        );
    }

    private void drawEditCue(@NonNull Canvas canvas, @NonNull View itemView, float dX) {
        backgroundPaint.setColor(
                ContextCompat.getColor(taskAdapter.getContext(), R.color.edit_color)
        );
        float overlap = dpToPx(ACTION_OVERLAP_DP);
        canvas.drawRect(
                itemView.getLeft(),
                itemView.getTop(),
                itemView.getLeft() + dX + overlap,
                itemView.getBottom(),
                backgroundPaint
        );

        drawAction(
                canvas,
                itemView,
                R.drawable.ic_baseline_edit_24,
                R.string.swipe_edit,
                true,
                dX
        );
    }

    private void drawAction(@NonNull Canvas canvas,
                            @NonNull View itemView,
                            int iconRes,
                            int labelRes,
                            boolean onLeft,
                            float dX) {
        if (Math.abs(dX) <= dpToPx(52)) {
            return;
        }

        Drawable icon = ContextCompat.getDrawable(taskAdapter.getContext(), iconRes);
        if (icon == null) {
            return;
        }

        int iconSize = dpToPx(24);
        int margin = dpToPx(24);
        int centerY = itemView.getTop() + itemView.getHeight() / 2;

        if (onLeft) {
            int iconLeft = itemView.getLeft() + margin;
            int iconRight = iconLeft + iconSize;
            icon.setBounds(
                    iconLeft,
                    centerY - iconSize / 2,
                    iconRight,
                    centerY + iconSize / 2
            );
            icon.draw(canvas);

            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(
                    taskAdapter.getContext().getString(labelRes),
                    iconRight + dpToPx(12),
                    baselineForCenter(centerY),
                    textPaint
            );
        } else {
            int iconRight = itemView.getRight() - margin;
            int iconLeft = iconRight - iconSize;
            icon.setBounds(
                    iconLeft,
                    centerY - iconSize / 2,
                    iconRight,
                    centerY + iconSize / 2
            );
            icon.draw(canvas);

            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(
                    taskAdapter.getContext().getString(labelRes),
                    iconLeft - dpToPx(12),
                    baselineForCenter(centerY),
                    textPaint
            );
        }
    }

    private float baselineForCenter(int centerY) {
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        return centerY - (metrics.ascent + metrics.descent) / 2f;
    }

    private int dpToPx(int dp) {
        float density = taskAdapter.getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private float spToPx(int sp) {
        float scaledDensity =
                taskAdapter.getContext().getResources().getDisplayMetrics().scaledDensity;
        return sp * scaledDensity;
    }
}

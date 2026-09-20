package com.farwaahmad.mylist.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farwaahmad.mylist.model.TaskModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRepository {

    private static final String USERS_COLLECTION = "users";
    private static final String TASKS_COLLECTION = "tasks";
    private static final int DELETE_BATCH_SIZE = 400;

    private final FirebaseFirestore firestore;
    private final String userId;

    public TaskRepository(@NonNull String userId) {
        this.firestore = FirebaseFirestore.getInstance();
        this.userId = userId;
    }

    private CollectionReference tasks() {
        return firestore.collection(USERS_COLLECTION)
                .document(userId)
                .collection(TASKS_COLLECTION);
    }

    public void loadServerTasksOnce(@NonNull TaskListener listener) {
        tasks()
                .orderBy("time", Query.Direction.DESCENDING)
                .get(Source.SERVER)
                .addOnSuccessListener(snapshot ->
                        listener.onTasksChanged(mapTasks(snapshot)))
                .addOnFailureListener(listener::onError);
    }

    public void loadCachedTasksOnce(@NonNull TaskListener listener) {
        tasks()
                .orderBy("time", Query.Direction.DESCENDING)
                .get(Source.CACHE)
                .addOnSuccessListener(snapshot ->
                        listener.onTasksChanged(mapTasks(snapshot)))
                .addOnFailureListener(listener::onError);
    }

    public ListenerRegistration listenForTasks(@NonNull TaskListener listener) {
        return tasks()
                .orderBy("time", Query.Direction.DESCENDING)
                .addSnapshotListener((@Nullable QuerySnapshot value,
                                      @Nullable FirebaseFirestoreException error) -> {
                    if (error != null) {
                        listener.onError(error);
                        return;
                    }

                    if (value == null) {
                        listener.onTasksChanged(new ArrayList<>());
                        return;
                    }

                    listener.onTasksChanged(mapTasks(value));
                });
    }

    @NonNull
    private List<TaskModel> mapTasks(@NonNull QuerySnapshot snapshot) {
        List<TaskModel> taskList = new ArrayList<>();
        for (DocumentSnapshot document : snapshot.getDocuments()) {
            TaskModel task = document.toObject(TaskModel.class);
            if (task != null) {
                task.setId(document.getId());
                taskList.add(task);
            }
        }
        return taskList;
    }

    public void addTask(@NonNull String taskText,
                        @NonNull String dueDate,
                        @NonNull String dueTime,
                        @NonNull AddTaskCallback callback) {
        Map<String, Object> task = new HashMap<>();
        task.put("task", taskText);
        task.put("due", dueDate);
        if (!dueTime.isEmpty()) {
            task.put("dueTime", dueTime);
        }
        task.put("status", 0);
        task.put("time", FieldValue.serverTimestamp());

        tasks().add(task)
                .addOnSuccessListener(documentReference ->
                        callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onError);
    }

    public void updateTask(@NonNull String id,
                           @NonNull String taskText,
                           @NonNull String dueDate,
                           @NonNull String dueTime,
                           @NonNull OperationCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("task", taskText);
        updates.put("due", dueDate);
        updates.put("dueTime", dueTime.isEmpty() ? FieldValue.delete() : dueTime);

        tasks().document(id)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void updateStatus(@NonNull String id,
                             boolean isComplete,
                             @NonNull OperationCallback callback) {
        tasks().document(id)
                .update("status", isComplete ? 1 : 0)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteTask(@NonNull String id,
                           @NonNull OperationCallback callback) {
        tasks().document(id)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void restoreTask(@NonNull TaskModel task,
                            @NonNull OperationCallback callback) {
        String id = task.getId();
        String taskText = task.getTask();

        if (id == null || id.trim().isEmpty()
                || taskText == null || taskText.trim().isEmpty()) {
            callback.onError(new IllegalArgumentException("Task identity is missing."));
            return;
        }

        Map<String, Object> restored = new HashMap<>();
        restored.put("task", taskText);
        restored.put("due", task.getDue() == null ? "" : task.getDue());

        String dueTime = task.getDueTime();
        if (dueTime != null && !dueTime.isEmpty()) {
            restored.put("dueTime", dueTime);
        }

        restored.put("status", task.getStatus());
        restored.put(
                "time",
                task.getTime() != null
                        ? task.getTime()
                        : FieldValue.serverTimestamp()
        );

        tasks().document(id)
                .set(restored)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteAllTasks(@NonNull OperationCallback callback) {
        deleteNextBatch(callback);
    }

    private void deleteNextBatch(@NonNull OperationCallback callback) {
        tasks().limit(DELETE_BATCH_SIZE)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }

                    WriteBatch batch = firestore.batch();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        batch.delete(document.getReference());
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> deleteNextBatch(callback))
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public interface TaskListener {
        void onTasksChanged(@NonNull List<TaskModel> tasks);
        void onError(@NonNull Exception exception);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(@NonNull Exception exception);
    }

    public interface AddTaskCallback {
        void onSuccess(@NonNull String taskId);
        void onError(@NonNull Exception exception);
    }
}

package com.example.mylist.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mylist.Models.TaskModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRepository {

    private static final String TASK_COLLECTION = "task";

    private final FirebaseFirestore firestore;

    public TaskRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public ListenerRegistration listenForTasks(@NonNull TaskListener listener) {
        return firestore.collection(TASK_COLLECTION)
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

                    List<TaskModel> tasks = new ArrayList<>();
                    for (DocumentSnapshot document : value.getDocuments()) {
                        TaskModel task = document.toObject(TaskModel.class);
                        if (task != null) {
                            task.setId(document.getId());
                            tasks.add(task);
                        }
                    }

                    listener.onTasksChanged(tasks);
                });
    }

    public void addTask(@NonNull String taskText,
                        @NonNull String dueDate,
                        @NonNull OperationCallback callback) {
        Map<String, Object> task = new HashMap<>();
        task.put("task", taskText);
        task.put("due", dueDate);
        task.put("status", 0);
        task.put("time", FieldValue.serverTimestamp());

        firestore.collection(TASK_COLLECTION)
                .add(task)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void updateTask(@NonNull String id,
                           @NonNull String taskText,
                           @NonNull String dueDate,
                           @NonNull OperationCallback callback) {
        firestore.collection(TASK_COLLECTION)
                .document(id)
                .update("task", taskText, "due", dueDate)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void updateStatus(@NonNull String id,
                             boolean isComplete,
                             @NonNull OperationCallback callback) {
        firestore.collection(TASK_COLLECTION)
                .document(id)
                .update("status", isComplete ? 1 : 0)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteTask(@NonNull String id,
                           @NonNull OperationCallback callback) {
        firestore.collection(TASK_COLLECTION)
                .document(id)
                .delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
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
}

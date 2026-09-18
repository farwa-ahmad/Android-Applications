package com.farwaahmad.mylist.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuth auth;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public void ensureUser(@NonNull AuthCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            callback.onSuccess(currentUser);
            return;
        }

        auth.signInAnonymously()
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new IllegalStateException("Firebase returned no user."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void linkAnonymousWithEmail(@NonNull String email,
                                       @NonNull String password,
                                       @NonNull AuthCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || !currentUser.isAnonymous()) {
            callback.onError(new IllegalStateException("No anonymous account is available to back up."));
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        currentUser.linkWithCredential(credential)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new IllegalStateException("Firebase returned no user."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void restoreEmailAccount(@NonNull String email,
                                    @NonNull String password,
                                    @NonNull AuthCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null && currentUser.isAnonymous()) {
            currentUser.delete()
                    .addOnSuccessListener(unused -> signInWithEmail(email, password, callback))
                    .addOnFailureListener(callback::onError);
            return;
        }

        auth.signOut();
        signInWithEmail(email, password, callback);
    }

    private void signInWithEmail(@NonNull String email,
                                 @NonNull String password,
                                 @NonNull AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onError(new IllegalStateException("Firebase returned no user."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void signOut() {
        auth.signOut();
    }

    public void reauthenticateForDeletion(@Nullable String password,
                                          @NonNull SimpleCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("No signed-in account."));
            return;
        }

        if (currentUser.isAnonymous()) {
            callback.onSuccess();
            return;
        }

        String email = currentUser.getEmail();
        if (email == null || password == null || password.isBlank()) {
            callback.onError(new IllegalArgumentException("Enter your password to delete this account."));
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void deleteCurrentAccount(@NonNull SimpleCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("No signed-in account."));
            return;
        }

        currentUser.delete()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public interface AuthCallback {
        void onSuccess(@NonNull FirebaseUser user);
        void onError(@NonNull Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(@NonNull Exception exception);
    }
}

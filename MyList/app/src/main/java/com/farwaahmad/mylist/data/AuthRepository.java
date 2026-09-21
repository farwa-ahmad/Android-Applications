package com.farwaahmad.mylist.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AuthRepository {

    private final FirebaseAuth auth;

    public AuthRepository() {
        auth = FirebaseAuth.getInstance();
        auth.useAppLanguage();
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
            callback.onError(new IllegalStateException(
                    "No anonymous account is available to protect."
            ));
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
        // Sign-in itself switches FirebaseAuth to the existing account.
        signInWithEmail(email, password, callback);
    }

    /**
     * Authenticates an existing account without replacing the current guest session.
     *
     * This temporary secondary Firebase app lets guest tasks be copied into the
     * destination account while the default FirebaseAuth instance still owns the
     * guest Firestore documents and can safely clean them up afterwards.
     */
    public void openExistingAccountSession(@NonNull Context context,
                                           @NonNull String email,
                                           @NonNull String password,
                                           @NonNull ExistingAccountSessionCallback callback) {
        FirebaseApp secondaryApp;
        try {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            secondaryApp = FirebaseApp.initializeApp(
                    context.getApplicationContext(),
                    options,
                    "restore-" + UUID.randomUUID()
            );
        } catch (Exception exception) {
            callback.onError(exception);
            return;
        }

        if (secondaryApp == null) {
            callback.onError(new IllegalStateException(
                    "Could not create a temporary Firebase session."
            ));
            return;
        }

        FirebaseAuth secondaryAuth = FirebaseAuth.getInstance(secondaryApp);
        secondaryAuth.useAppLanguage();
        secondaryAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        secondaryApp.delete();
                        callback.onError(new IllegalStateException(
                                "Firebase returned no user."
                        ));
                        return;
                    }

                    callback.onSuccess(
                            new ExistingAccountSession(secondaryApp, user.getUid())
                    );
                })
                .addOnFailureListener(exception -> {
                    secondaryApp.delete();
                    callback.onError(exception);
                });
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

    public void reloadCurrentUser(@NonNull AuthCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onError(new IllegalStateException("No signed-in account."));
            return;
        }

        currentUser.reload()
                .addOnSuccessListener(unused -> {
                    FirebaseUser refreshedUser = auth.getCurrentUser();
                    if (refreshedUser != null) {
                        callback.onSuccess(refreshedUser);
                    } else {
                        callback.onError(new IllegalStateException("Firebase returned no user."));
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public void sendVerificationEmail(@NonNull SimpleCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || currentUser.isAnonymous()) {
            callback.onError(new IllegalStateException(
                    "A protected account is required to verify an email."
            ));
            return;
        }

        if (currentUser.isEmailVerified()) {
            callback.onSuccess();
            return;
        }

        currentUser.sendEmailVerification()
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void sendPasswordResetEmail(@NonNull String email,
                                       @NonNull SimpleCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
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
            callback.onError(new IllegalArgumentException(
                    "Enter your password to delete this account."
            ));
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

    public static final class ExistingAccountSession implements AutoCloseable {

        private final FirebaseApp app;
        private final String userId;
        private boolean closed;

        private ExistingAccountSession(@NonNull FirebaseApp app,
                                       @NonNull String userId) {
            this.app = app;
            this.userId = userId;
        }

        @NonNull
        public TaskRepository taskRepository() {
            if (closed) {
                throw new IllegalStateException("Temporary account session is closed.");
            }

            return new TaskRepository(
                    FirebaseFirestore.getInstance(app),
                    userId
            );
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            app.delete();
        }
    }

    public interface AuthCallback {
        void onSuccess(@NonNull FirebaseUser user);
        void onError(@NonNull Exception exception);
    }

    public interface ExistingAccountSessionCallback {
        void onSuccess(@NonNull ExistingAccountSession session);
        void onError(@NonNull Exception exception);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(@NonNull Exception exception);
    }
}

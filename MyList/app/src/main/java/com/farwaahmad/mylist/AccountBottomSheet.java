package com.farwaahmad.mylist;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.farwaahmad.mylist.databinding.AccountBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class AccountBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AccountBottomSheet";

    private static final String ARG_ANONYMOUS = "anonymous";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_HAS_TASKS = "hasTasks";
    private static final String ARG_EMAIL_VERIFIED = "emailVerified";

    private static final int MODE_NONE = 0;
    private static final int MODE_BACKUP = 1;
    private static final int MODE_RESTORE = 2;

    private AccountBottomSheetBinding binding;
    private AccountActionListener actionListener;
    private boolean isAnonymous;
    private boolean hasTasks;
    private boolean emailVerified;
    private int formMode = MODE_NONE;

    public static AccountBottomSheet newInstance(boolean isAnonymous,
                                                 @Nullable String email,
                                                 boolean hasTasks,
                                                 boolean emailVerified) {
        AccountBottomSheet fragment = new AccountBottomSheet();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ANONYMOUS, isAnonymous);
        args.putString(ARG_EMAIL, email == null ? "" : email);
        args.putBoolean(ARG_HAS_TASKS, hasTasks);
        args.putBoolean(ARG_EMAIL_VERIFIED, emailVerified);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = AccountBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        isAnonymous = args == null || args.getBoolean(ARG_ANONYMOUS, true);
        hasTasks = args != null && args.getBoolean(ARG_HAS_TASKS, false);
        emailVerified = args != null && args.getBoolean(ARG_EMAIL_VERIFIED, false);
        String email = args == null ? "" : args.getString(ARG_EMAIL, "");

        renderState(email);

        binding.btnProtectTasks.setOnClickListener(v -> showCredentialForm(MODE_BACKUP));
        binding.btnExistingAccount.setOnClickListener(v -> showCredentialForm(MODE_RESTORE));
        binding.btnBackCredentials.setOnClickListener(v -> showChooser());
        binding.btnCredentialsAction.setOnClickListener(v -> submitCredentials());
        binding.btnForgotPassword.setOnClickListener(v -> requestPasswordReset());
        binding.btnSendVerification.setOnClickListener(v -> requestVerificationEmail());
        binding.btnSignOut.setOnClickListener(v -> confirmSignOut());
        binding.btnDeleteData.setOnClickListener(v -> confirmDelete());
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() == null) {
            return;
        }

        View bottomSheet =
                getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundColor(Color.TRANSPARENT);
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        Window window = getDialog().getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    private void renderState(@NonNull String email) {
        setAccountSummaryVisible(true);

        if (isAnonymous) {
            binding.tvAccountStatus.setText(R.string.guest_account);
            binding.tvVerificationStatus.setVisibility(View.GONE);
            binding.tvAccountDetails.setText(R.string.temporary_account_details);
            binding.anonymousChooser.setVisibility(View.VISIBLE);
            binding.credentialsForm.setVisibility(View.GONE);
            binding.permanentSection.setVisibility(View.GONE);
            binding.deletePasswordInputLayout.setVisibility(View.GONE);
            binding.btnDeleteData.setText(R.string.delete_my_data);

            binding.btnExistingAccount.setEnabled(!hasTasks);
            binding.tvExistingAccountHint.setText(
                    hasTasks
                            ? R.string.restore_blocked_with_tasks
                            : R.string.restore_existing_hint
            );
        } else {
            binding.tvAccountStatus.setText(
                    email.isEmpty() ? getString(R.string.tasks_protected) : email
            );
            binding.tvVerificationStatus.setVisibility(View.VISIBLE);
            binding.tvVerificationStatus.setText(
                    emailVerified
                            ? R.string.verified_badge
                            : R.string.not_verified_badge
            );
            binding.tvAccountDetails.setText(R.string.protected_account_details);
            binding.anonymousChooser.setVisibility(View.GONE);
            binding.credentialsForm.setVisibility(View.GONE);
            binding.permanentSection.setVisibility(View.VISIBLE);
            binding.deletePasswordInputLayout.setVisibility(View.GONE);
            binding.btnDeleteData.setText(R.string.delete_my_account_and_data);

            binding.tvVerificationHint.setVisibility(
                    emailVerified ? View.GONE : View.VISIBLE
            );
            binding.btnSendVerification.setVisibility(
                    emailVerified ? View.GONE : View.VISIBLE
            );
        }
    }

    private void showCredentialForm(int mode) {
        if (mode == MODE_RESTORE && hasTasks) {
            Toast.makeText(
                    requireContext(),
                    R.string.restore_blocked_with_tasks,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        formMode = mode;
        setAccountSummaryVisible(false);
        binding.anonymousChooser.setVisibility(View.GONE);
        binding.credentialsForm.setVisibility(View.VISIBLE);
        binding.btnForgotPassword.setVisibility(
                mode == MODE_RESTORE ? View.VISIBLE : View.GONE
        );
        clearFieldErrors();

        if (mode == MODE_BACKUP) {
            binding.tvFormTitle.setText(R.string.protect_my_tasks);
            binding.tvFormDescription.setText(R.string.protect_form_description);
            binding.btnCredentialsAction.setText(R.string.protect_my_tasks);
        } else {
            binding.tvFormTitle.setText(R.string.sign_in);
            binding.tvFormDescription.setText(R.string.restore_form_description);
            binding.btnCredentialsAction.setText(R.string.sign_in);
        }

        binding.etEmail.requestFocus();
        binding.etEmail.postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }
            InputMethodManager inputMethodManager =
                    (InputMethodManager) requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(
                    binding.etEmail,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }, 120);
    }

    private void showChooser() {
        formMode = MODE_NONE;
        setAccountSummaryVisible(true);
        binding.credentialsForm.setVisibility(View.GONE);
        binding.btnForgotPassword.setVisibility(View.GONE);
        binding.anonymousChooser.setVisibility(View.VISIBLE);
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        clearFieldErrors();
    }

    private void setAccountSummaryVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        binding.tvAccountStatus.setVisibility(visibility);
        binding.tvAccountDetails.setVisibility(visibility);
    }

    private void submitCredentials() {
        String email = binding.etEmail.getText() == null
                ? ""
                : binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText() == null
                ? ""
                : binding.etPassword.getText().toString();

        if (!validateCredentials(email, password)) {
            return;
        }

        setBusy(true);

        if (formMode == MODE_BACKUP) {
            actionListener.onBackupRequested(email, password, new BackupCallback() {
                @Override
                public void onSuccess(boolean verificationEmailSent) {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            verificationEmailSent
                                    ? R.string.backup_complete_verification_sent
                                    : R.string.backup_complete_verification_failed,
                            Toast.LENGTH_LONG
                    ).show();
                    dismiss();
                }

                @Override
                public void onError(@NonNull Exception exception) {
                    showError(exception);
                }
            });
            return;
        }

        if (formMode == MODE_RESTORE) {
            actionListener.onRestoreRequested(email, password, new ActionCallback() {
                @Override
                public void onSuccess() {
                    if (!isAdded()) {
                        return;
                    }

                    Toast.makeText(
                            requireContext(),
                            R.string.restore_complete,
                            Toast.LENGTH_SHORT
                    ).show();
                    dismiss();
                }

                @Override
                public void onError(@NonNull Exception exception) {
                    showError(exception);
                }
            });
        }
    }

    private void requestPasswordReset() {
        String email = binding.etEmail.getText() == null
                ? ""
                : binding.etEmail.getText().toString().trim();

        binding.emailInputLayout.setError(null);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError(getString(R.string.enter_valid_email));
            return;
        }

        setBusy(true);
        actionListener.onPasswordResetRequested(email, new ActionCallback() {
            @Override
            public void onSuccess() {
                showPasswordResetConfirmation();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                if (exception instanceof FirebaseAuthInvalidUserException) {
                    showPasswordResetConfirmation();
                } else {
                    showError(exception);
                }
            }
        });
    }

    private void showPasswordResetConfirmation() {
        if (!isAdded() || binding == null) {
            return;
        }

        setBusy(false);
        Toast.makeText(
                requireContext(),
                R.string.password_reset_sent,
                Toast.LENGTH_LONG
        ).show();
    }

    private void requestVerificationEmail() {
        setBusy(true);
        actionListener.onVerificationEmailRequested(new ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded() || binding == null) {
                    return;
                }

                setBusy(false);
                Toast.makeText(
                        requireContext(),
                        R.string.verification_email_sent,
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                showError(exception);
            }
        });
    }

    private boolean validateCredentials(@NonNull String email, @NonNull String password) {
        clearFieldErrors();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError(getString(R.string.enter_valid_email));
            return false;
        }

        if (password.length() < 6) {
            binding.passwordInputLayout.setError(getString(R.string.password_minimum));
            return false;
        }

        return true;
    }

    private void confirmSignOut() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sign_out)
                .setMessage(R.string.sign_out_confirmation)
                .setPositiveButton(R.string.sign_out, (dialog, which) -> {
                    actionListener.onSignOutRequested();
                    Toast.makeText(
                            requireContext(),
                            R.string.signed_out_message,
                            Toast.LENGTH_SHORT
                    ).show();
                    dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDelete() {
        String password = null;

        if (!isAnonymous) {
            if (binding.deletePasswordInputLayout.getVisibility() != View.VISIBLE) {
                binding.deletePasswordInputLayout.setVisibility(View.VISIBLE);
                binding.etDeletePassword.requestFocus();
                binding.etDeletePassword.postDelayed(() -> {
                    if (!isAdded() || binding == null) {
                        return;
                    }
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) requireContext()
                                    .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(
                            binding.etDeletePassword,
                            InputMethodManager.SHOW_IMPLICIT
                    );
                }, 120);
                return;
            }

            password = binding.etDeletePassword.getText() == null
                    ? ""
                    : binding.etDeletePassword.getText().toString();

            if (password.isBlank()) {
                binding.deletePasswordInputLayout.setError(
                        getString(R.string.password_required_to_delete)
                );
                return;
            }
            binding.deletePasswordInputLayout.setError(null);
        }

        String finalPassword = password;
        new AlertDialog.Builder(requireContext())
                .setTitle(
                        isAnonymous
                                ? R.string.delete_my_data
                                : R.string.delete_my_account_and_data
                )
                .setMessage(R.string.delete_account_confirmation)
                .setPositiveButton(R.string.delete_everything, (dialog, which) -> {
                    setBusy(true);
                    actionListener.onDeleteRequested(finalPassword, new ActionCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) {
                                return;
                            }

                            Toast.makeText(
                                    requireContext(),
                                    R.string.account_deleted,
                                    Toast.LENGTH_SHORT
                            ).show();
                            dismiss();
                        }

                        @Override
                        public void onError(@NonNull Exception exception) {
                            showError(exception);
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setBusy(boolean busy) {
        binding.accountProgress.setVisibility(busy ? View.VISIBLE : View.GONE);
        binding.btnProtectTasks.setEnabled(!busy);
        binding.btnExistingAccount.setEnabled(!busy && !hasTasks);
        binding.btnCredentialsAction.setEnabled(!busy);
        binding.btnBackCredentials.setEnabled(!busy);
        binding.btnForgotPassword.setEnabled(!busy);
        binding.btnSendVerification.setEnabled(!busy);
        binding.btnSignOut.setEnabled(!busy);
        binding.btnDeleteData.setEnabled(!busy);
        binding.etEmail.setEnabled(!busy);
        binding.etPassword.setEnabled(!busy);
        binding.etDeletePassword.setEnabled(!busy);
    }

    private void clearFieldErrors() {
        binding.emailInputLayout.setError(null);
        binding.passwordInputLayout.setError(null);
    }

    private void showError(@NonNull Exception exception) {
        if (!isAdded()) {
            return;
        }

        setBusy(false);
        Toast.makeText(
                requireContext(),
                friendlyErrorMessage(exception),
                Toast.LENGTH_LONG
        ).show();
    }

    @NonNull
    private String friendlyErrorMessage(@NonNull Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            return getString(R.string.email_in_use_message);
        }

        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return getString(R.string.weak_password_message);
        }

        if (exception instanceof FirebaseNetworkException) {
            return getString(R.string.network_error_message);
        }

        if (exception instanceof FirebaseAuthInvalidCredentialsException
                || exception instanceof FirebaseAuthInvalidUserException) {
            return getString(R.string.invalid_credentials_message);
        }

        if (exception instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) exception).getErrorCode();
            if ("ERROR_TOO_MANY_REQUESTS".equals(code)) {
                return getString(R.string.too_many_requests_message);
            }
            if ("ERROR_REQUIRES_RECENT_LOGIN".equals(code)) {
                return getString(R.string.recent_login_message);
            }
        }

        return getString(R.string.account_action_error);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (!(context instanceof AccountActionListener)) {
            throw new IllegalStateException("Host activity must implement AccountActionListener");
        }
        actionListener = (AccountActionListener) context;
    }

    @Override
    public void onDetach() {
        actionListener = null;
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    public interface AccountActionListener {
        void onBackupRequested(@NonNull String email,
                               @NonNull String password,
                               @NonNull BackupCallback callback);

        void onRestoreRequested(@NonNull String email,
                                @NonNull String password,
                                @NonNull ActionCallback callback);

        void onPasswordResetRequested(@NonNull String email,
                                      @NonNull ActionCallback callback);

        void onVerificationEmailRequested(@NonNull ActionCallback callback);

        void onSignOutRequested();

        void onDeleteRequested(@Nullable String password,
                               @NonNull ActionCallback callback);
    }

    public interface BackupCallback {
        void onSuccess(boolean verificationEmailSent);

        void onError(@NonNull Exception exception);
    }

    public interface ActionCallback {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }
}

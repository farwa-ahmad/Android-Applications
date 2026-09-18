package com.farwaahmad.mylist;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.farwaahmad.mylist.databinding.AccountBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class AccountBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AccountBottomSheet";

    private static final String ARG_ANONYMOUS = "anonymous";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_HAS_TASKS = "hasTasks";

    private static final int MODE_NONE = 0;
    private static final int MODE_BACKUP = 1;
    private static final int MODE_RESTORE = 2;

    private AccountBottomSheetBinding binding;
    private AccountActionListener actionListener;
    private boolean isAnonymous;
    private boolean hasTasks;
    private int formMode = MODE_NONE;

    public static AccountBottomSheet newInstance(boolean isAnonymous,
                                                 @Nullable String email,
                                                 boolean hasTasks) {
        AccountBottomSheet fragment = new AccountBottomSheet();
        Bundle args = new Bundle();
        args.putBoolean(ARG_ANONYMOUS, isAnonymous);
        args.putString(ARG_EMAIL, email == null ? "" : email);
        args.putBoolean(ARG_HAS_TASKS, hasTasks);
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
        String email = args == null ? "" : args.getString(ARG_EMAIL, "");

        renderState(email);

        binding.btnProtectTasks.setOnClickListener(v -> showCredentialForm(MODE_BACKUP));
        binding.btnExistingAccount.setOnClickListener(v -> showCredentialForm(MODE_RESTORE));
        binding.btnBackCredentials.setOnClickListener(v -> showChooser());
        binding.btnCredentialsAction.setOnClickListener(v -> submitCredentials());
        binding.btnSignOut.setOnClickListener(v -> confirmSignOut());
        binding.btnDeleteData.setOnClickListener(v -> confirmDelete());
    }

    private void renderState(@NonNull String email) {
        if (isAnonymous) {
            binding.tvAccountStatus.setText(R.string.tasks_saved);
            binding.tvAccountDetails.setText(R.string.temporary_account_details);
            binding.anonymousChooser.setVisibility(View.VISIBLE);
            binding.credentialsForm.setVisibility(View.GONE);
            binding.permanentSection.setVisibility(View.GONE);
            binding.deletePasswordInputLayout.setVisibility(View.GONE);

            binding.btnExistingAccount.setEnabled(!hasTasks);
            binding.tvExistingAccountHint.setText(
                    hasTasks
                            ? R.string.restore_blocked_with_tasks
                            : R.string.restore_existing_hint
            );
        } else {
            binding.tvAccountStatus.setText(R.string.tasks_protected);
            binding.tvAccountDetails.setText(
                    getString(R.string.backed_up_account_details, email)
            );
            binding.anonymousChooser.setVisibility(View.GONE);
            binding.credentialsForm.setVisibility(View.GONE);
            binding.permanentSection.setVisibility(View.VISIBLE);
            binding.deletePasswordInputLayout.setVisibility(View.VISIBLE);
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
        binding.anonymousChooser.setVisibility(View.GONE);
        binding.credentialsForm.setVisibility(View.VISIBLE);
        clearFieldErrors();

        if (mode == MODE_BACKUP) {
            binding.tvFormTitle.setText(R.string.protect_my_tasks);
            binding.tvFormDescription.setText(R.string.protect_form_description);
            binding.btnCredentialsAction.setText(R.string.create_backup);
        } else {
            binding.tvFormTitle.setText(R.string.sign_in_existing);
            binding.tvFormDescription.setText(R.string.restore_form_description);
            binding.btnCredentialsAction.setText(R.string.sign_in);
        }
    }

    private void showChooser() {
        formMode = MODE_NONE;
        binding.credentialsForm.setVisibility(View.GONE);
        binding.anonymousChooser.setVisibility(View.VISIBLE);
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        clearFieldErrors();
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

        ActionCallback callback = new ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(
                        requireContext(),
                        formMode == MODE_BACKUP
                                ? R.string.backup_complete
                                : R.string.restore_complete,
                        Toast.LENGTH_SHORT
                ).show();
                dismiss();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                showError(exception);
            }
        };

        if (formMode == MODE_BACKUP) {
            actionListener.onBackupRequested(email, password, callback);
        } else if (formMode == MODE_RESTORE) {
            actionListener.onRestoreRequested(email, password, callback);
        }
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
                .setTitle(R.string.delete_my_data)
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

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
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
                               @NonNull ActionCallback callback);

        void onRestoreRequested(@NonNull String email,
                                @NonNull String password,
                                @NonNull ActionCallback callback);

        void onSignOutRequested();

        void onDeleteRequested(@Nullable String password,
                               @NonNull ActionCallback callback);
    }

    public interface ActionCallback {
        void onSuccess();

        void onError(@NonNull Exception exception);
    }
}

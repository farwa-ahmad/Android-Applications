package com.farwaahmad.mylist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.farwaahmad.mylist.databinding.AccountBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AccountBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AccountBottomSheet";

    private static final String ARG_ANONYMOUS = "anonymous";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_HAS_TASKS = "hasTasks";

    private AccountBottomSheetBinding binding;
    private AccountActionListener actionListener;
    private boolean isAnonymous;
    private boolean hasTasks;

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

        binding.btnBackup.setOnClickListener(v -> backUpAccount());
        binding.btnRestore.setOnClickListener(v -> restoreAccount());
        binding.btnSignOut.setOnClickListener(v -> actionListener.onSignOutRequested());
        binding.btnDeleteData.setOnClickListener(v -> confirmDelete());
    }

    private void renderState(@NonNull String email) {
        if (isAnonymous) {
            binding.tvAccountStatus.setText(R.string.temporary_account);
            binding.tvAccountDetails.setText(R.string.temporary_account_details);
            binding.anonymousSection.setVisibility(View.VISIBLE);
            binding.permanentSection.setVisibility(View.GONE);
            binding.etDeletePassword.setVisibility(View.GONE);

            if (hasTasks) {
                binding.btnRestore.setEnabled(false);
                binding.tvRestoreHint.setText(R.string.restore_blocked_with_tasks);
            } else {
                binding.btnRestore.setEnabled(true);
                binding.tvRestoreHint.setText(R.string.restore_existing_hint);
            }
        } else {
            binding.tvAccountStatus.setText(R.string.backed_up_account);
            binding.tvAccountDetails.setText(
                    getString(R.string.backed_up_account_details, email)
            );
            binding.anonymousSection.setVisibility(View.GONE);
            binding.permanentSection.setVisibility(View.VISIBLE);
            binding.etDeletePassword.setVisibility(View.VISIBLE);
        }
    }

    private void backUpAccount() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (!validateCredentials(email, password)) {
            return;
        }

        setBusy(true);
        actionListener.onBackupRequested(email, password, new ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.backup_complete, Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                showError(exception);
            }
        });
    }

    private void restoreAccount() {
        if (hasTasks) {
            Toast.makeText(requireContext(), R.string.restore_blocked_with_tasks, Toast.LENGTH_LONG).show();
            return;
        }

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();

        if (!validateCredentials(email, password)) {
            return;
        }

        setBusy(true);
        actionListener.onRestoreRequested(email, password, new ActionCallback() {
            @Override
            public void onSuccess() {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), R.string.restore_complete, Toast.LENGTH_SHORT).show();
                dismiss();
            }

            @Override
            public void onError(@NonNull Exception exception) {
                showError(exception);
            }
        });
    }

    private boolean validateCredentials(@NonNull String email, @NonNull String password) {
        if (email.isBlank() || !email.contains("@")) {
            binding.etEmail.setError(getString(R.string.enter_valid_email));
            return false;
        }

        if (password.length() < 6) {
            binding.etPassword.setError(getString(R.string.password_minimum));
            return false;
        }

        return true;
    }

    private void confirmDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_my_data)
                .setMessage(R.string.delete_account_confirmation)
                .setPositiveButton(R.string.delete_everything, (dialog, which) -> {
                    String password = isAnonymous
                            ? null
                            : binding.etDeletePassword.getText().toString();
                    setBusy(true);
                    actionListener.onDeleteRequested(password, new ActionCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded()) return;
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
        binding.btnBackup.setEnabled(!busy);
        binding.btnRestore.setEnabled(!busy && !hasTasks);
        binding.btnSignOut.setEnabled(!busy);
        binding.btnDeleteData.setEnabled(!busy);
    }

    private void showError(@NonNull Exception exception) {
        if (!isAdded()) return;
        setBusy(false);
        String message = exception.getMessage() == null
                ? getString(R.string.generic_error)
                : exception.getMessage();
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
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

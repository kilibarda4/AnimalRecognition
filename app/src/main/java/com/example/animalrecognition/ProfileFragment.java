package com.example.animalrecognition;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {
    Button logout;
    FirebaseAuth mAuth;
    public ProfileFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(v -> {
//            showLogoutConfirmationDialog();
            androidx.appcompat.app.AlertDialog dialogBuilder =
                    new MaterialAlertDialogBuilder(requireContext(),R.style.MyThemeOverlay_MaterialComponents_MaterialAlertDialog)
                            .setMessage("Are you sure you want to sign out?")
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                // Sign out the user from Firebase (if needed)
                                FirebaseAuth.getInstance().signOut();

                                // Navigate back to the LoginActivity
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            })
                            .setNegativeButton(android.R.string.no, (dialog, which) -> {
                                // User clicked No, dismiss the dialog
                                dialog.dismiss();
                            })
                            .show();
        });
        return view;
    }
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_logout, null))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    // Sign out the user from Firebase (if needed)
                    FirebaseAuth.getInstance().signOut();

                    // Navigate back to the LoginActivity
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                    // User clicked No, dismiss the dialog
                    dialog.dismiss();
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
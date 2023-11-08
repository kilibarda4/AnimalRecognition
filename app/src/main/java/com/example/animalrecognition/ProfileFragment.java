package com.example.animalrecognition;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    public static final String FIRST_NAME = "first name";
    public static final String MIDDLE_NAME = "middle name";
    public static final String LAST_NAME = "last name";
    public static final String UTA_ID = "utaID";
    public static final String PROFESSION = "profession";
    Button logout, editInfo, changePassword, saveChanges;
    ImageView profileImage;
    EditText firstName, middleName, lastName, utaID, profession;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        logout          = view.findViewById(R.id.logoutButton);
        editInfo        = view.findViewById(R.id.editInfoButton);
        changePassword  = view.findViewById(R.id.changePasswordButton);
        profileImage    = view.findViewById(R.id.profileImageView);
        firstName       = view.findViewById(R.id.firstNameEditText);
        middleName      = view.findViewById(R.id.middleNameEditText);
        lastName        = view.findViewById(R.id.lastNameEditText);
        utaID           = view.findViewById(R.id.idEditText);
        profession      = view.findViewById(R.id.professionEditText);
        saveChanges     = view.findViewById(R.id.saveButton);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null; //unnecessary since only logged in users can be in p_page
        String userId = currentUser.getUid();
        saveChanges.setVisibility(View.GONE);
        loadUserDataFromFirestore();
//        disableEditing();

        logout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });

        editInfo.setOnClickListener(view1 -> {
            enableEditing();
            saveChanges.setVisibility(View.VISIBLE);
        });

        saveChanges.setOnClickListener(view1 -> {
            saveUserDataToFirestore();
        });

        return view;
    }
private void loadUserDataFromFirestore() {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser != null) {
        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Retrieve user data from Firestore
        db.collection("userData").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fName = documentSnapshot.getString(FIRST_NAME);
                        String mName = documentSnapshot.getString(MIDDLE_NAME);
                        String lName = documentSnapshot.getString(LAST_NAME);
                        String id = documentSnapshot.getString(UTA_ID);
                        String prof = documentSnapshot.getString(PROFESSION);

//                        enableEditing();
                        // Set the retrieved data to EditText fields
                        firstName.setText(fName);
                        middleName.setText(mName);
                        lastName.setText(lName);
                        utaID.setText(id);
                        profession.setText(prof);

                        disableEditing();
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error loading user data from Firestore!", e));
    }
}

    private void saveUserDataToFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null; //unnecessary since only logged in users can be in p_page
        String userId = currentUser.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String fName = firstName.getText().toString();
        String mName = middleName.getText().toString();
        String lName = lastName.getText().toString();
        String id_   = utaID.getText().toString();
        String prof  = profession.getText().toString();

        DocumentReference userDocRef = db.collection("userData").document(userId);

        //Save to Firestore
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put(FIRST_NAME, fName);
        userInfo.put(MIDDLE_NAME, mName);
        userInfo.put(LAST_NAME, lName);
        userInfo.put(UTA_ID, id_);
        userInfo.put(PROFESSION, prof);
        userDocRef.set(userInfo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Document has been saved!");
                    //disable editText editing
                    disableEditing();
                    //make the button invisible again
                    saveChanges.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> Log.w(TAG, "Document was not saved!", e));
    }

    private void showLogoutConfirmationDialog() {
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
    }

    private void enableEditing() {
        // Enable editing for the EditText fields
        firstName.setEnabled(true);
        middleName.setEnabled(true);
        lastName.setEnabled(true);
        utaID.setEnabled(true);
        profession.setEnabled(true);

        // Set focus to the first editable EditText field
        firstName.requestFocus();
    }
    private void disableEditing() {
        //Disable editing for the information fields
        firstName.setEnabled(false);
        middleName.setEnabled(false);
        lastName.setEnabled(false);
        utaID.setEnabled(false);
        profession.setEnabled(false);
    }
}
package com.example.animalrecognition;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    public static final String FIRST_NAME = "first name";
    public static final String MIDDLE_NAME = "middle name";
    public static final String LAST_NAME = "last name";
    public static final String UTA_ID = "utaID";
    public static final String PROFESSION = "profession";
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_GALLERY = 2;

    Button logout, editInfo, changePassword, saveChanges;
    ImageView profileImage;
    private Uri imageUri;
    EditText firstName, middleName, lastName, utaID, profession;
    FirebaseStorage storage;
    StorageReference storageReference;

    public ProfileFragment() {}

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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        saveChanges.setVisibility(View.GONE);
        loadUserDataFromFirestore();
        loadPicture();


        logout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });

        editInfo.setOnClickListener(v -> {
            enableEditing();
            saveChanges.setVisibility(View.VISIBLE);
            editInfo.setVisibility(View.GONE);
        });

        saveChanges.setOnClickListener(v -> {
            saveUserDataToFirestore();
            editInfo.setVisibility(View.VISIBLE);
        });

        profileImage.setOnClickListener(v -> {
            choosePicture();
        });

        changePassword.setOnClickListener(v -> {
            changeUserPassword(view, currentUser);
        });

        return view;
    }


    private void choosePicture() {
        // Use an AlertDialog to let the user choose between gallery and camera
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle("Choose Picture Source");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // Gallery
                        Intent galleryIntent = new Intent();
                        galleryIntent.setType("image/*");
                        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);
                        break;
                    case 1:
                        // Camera
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                            startActivityForResult(cameraIntent, REQUEST_CAMERA);
                        } else {
                            Toast.makeText(getContext(), "Camera not available", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    private void changeUserPassword(View v, FirebaseUser currentUser) {
        final EditText currentPasswordEditText = new EditText(v.getContext());
        currentPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        currentPasswordEditText.setHint("Current Password");

        final EditText passwordEditText = new EditText(v.getContext());
        passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordEditText.setHint("New Password");

        final EditText repeatPasswordEditText = new EditText(v.getContext());
        repeatPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        repeatPasswordEditText.setHint("Retype Password");

        LinearLayout layout = new LinearLayout(v.getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(currentPasswordEditText);
        layout.addView(passwordEditText);
        layout.addView(repeatPasswordEditText);

        new MaterialAlertDialogBuilder(v.getContext())
                .setTitle("Change Password")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    String currentPassword = currentPasswordEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    String repeatPassword = repeatPasswordEditText.getText().toString();

                    if (password.equals(repeatPassword)) {
                        currentUser.updatePassword(password)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User password updated.");
                                        Toast.makeText(v.getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                                        // Reauthenticate the user
                                        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
                                        currentUser.reauthenticate(credential)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        // Retry password update
                                                        currentUser.updatePassword(password)
                                                                .addOnCompleteListener(task1 -> {
                                                                    if (task1.isSuccessful()) {
                                                                        Log.d(TAG, "User password updated after reauthentication.");
                                                                        Toast.makeText(v.getContext(), "Password updated", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                        dialog.dismiss();
                    } else {
                        Toast.makeText(v.getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY:
                    if (data != null && data.getData() != null) {
                        // Get selected image from gallery
                        imageUri = data.getData();
                        Bitmap galleryBitmap = ImageUtils.getBitmapFromUri(imageUri, requireContext());

                        // Crop bitmap to a circle
                        Bitmap circularGalleryBitmap = ImageUtils.cropToCircle(galleryBitmap);

                        galleryBitmap.recycle();
                        // Set the circular image to ImageView
                        profileImage.setImageBitmap(circularGalleryBitmap);
                        // Apply circular background to ImageView
                        profileImage.setBackgroundResource(R.drawable.circular_background);

                        // Upload circular image
                        uploadPicture();
                    }
                    break;
                case REQUEST_CAMERA:
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        // Get the captured image from the camera
                        Bitmap cameraBitmap = (Bitmap) extras.get("data");

                        // Crop the bitmap to a circle
                        assert cameraBitmap != null;
                        Bitmap circularCameraBitmap = ImageUtils.cropToCircle(cameraBitmap);
                        cameraBitmap.recycle();
                        // Set the circular image to your ImageView
                        profileImage.setImageBitmap(circularCameraBitmap);
                        // Apply the circular background to the ImageView
                        profileImage.setBackgroundResource(R.drawable.circular_background);

                        // Convert the circular bitmap to Uri
                        imageUri = ImageUtils.getImageUri(requireContext(), circularCameraBitmap);

                        // Upload the circular image
                        uploadPicture();
                    }
                    break;
            }
        }
    }

    private void uploadPicture() {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        String userId = currentUser.getUid();
        StorageReference profRef = storageReference.child("images/profile");
        profRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> Toast.makeText(getContext(), "Image uploaded.",
                Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to upload.",
                        Toast.LENGTH_SHORT).show());
    }

    private void loadPicture() {
            StorageReference storageRef = storage.getReference().child("images/profile");

            final long ONE_MEGABYTE = 1024 * 1024;
            storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(ImageUtils.cropToCircle(bmp));

            }).addOnFailureListener(exception -> Toast.makeText(requireContext(), "No Such file or Path found!!", Toast.LENGTH_LONG).show());
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
                            AudioViewModel.destroyInstance();
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
        profileImage.setClickable(true);
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
        profileImage.setClickable(false);
        firstName.setEnabled(false);
        middleName.setEnabled(false);
        lastName.setEnabled(false);
        utaID.setEnabled(false);
        profession.setEnabled(false);
    }
}
package com.example.animalrecognition;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.animalrecognition.databinding.ActivityHomeBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.core.BaseOptions;

import java.util.concurrent.atomic.AtomicBoolean;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    Button btnStartRecording, btnStopRecording;
    TextView result;

    private AudioClassifierHelper audioClassifierHelper;
    private AudioViewModel audioViewModel;

    String modelPath = "yamnet.tflite";
    AudioClassifier classifier;
    AudioClassifier.AudioClassifierOptions options;

    ActivityHomeBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(binding.getRoot());

        //init firebase
        FirebaseApp.initializeApp(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        btnStartRecording = findViewById(R.id.btnStartRecording);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        result = findViewById(R.id.result);
//        specs = findViewById(R.id.specs);

        AtomicBoolean home = new AtomicBoolean(false);
        //navigate using the nav bar
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                if(!home.get()) {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent);
                    home.set(true);
                }
            } else if (id == R.id.navigation_profile) {
                home.set(false);
                audioClassifierHelper.stopRecording();
                replaceFragment(new ProfileFragment());
            } else if (id == R.id.navigation_stats) {
                home.set(false);
                audioClassifierHelper.stopRecording();
                replaceFragment(new StatsFragment());
            } else if (id == R.id.navigation_info) {
                home.set(false);
                audioClassifierHelper.stopRecording();
                replaceFragment(new InfoFragment());
            }
            return true;
        });

        Bundle params = new Bundle();
        params.putString("startRCD", "startRecordingButton");

        options = AudioClassifier.AudioClassifierOptions.builder()
                .setBaseOptions(BaseOptions.builder().build())
                .setMaxResults(1)
                .build();

        audioViewModel = AudioViewModel.getInstance(this.getApplication());
//        audioViewModel.getAudioStats();
        audioClassifierHelper = new AudioClassifierHelper(result);

        btnStartRecording.setOnClickListener(view -> {

            audioClassifierHelper.checkPermissionAndRecord(HomeActivity.this, HomeActivity.this,
                    Manifest.permission.RECORD_AUDIO, REQUEST_RECORD_AUDIO_PERMISSION, modelPath, options);
            mFirebaseAnalytics.logEvent("record_button_click", params);

        });

        btnStopRecording.setOnClickListener((v -> audioClassifierHelper.stopRecording()));

        binding.bottomNavigationView.setBackground(null);
    }

    protected void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startAudioClassification();
                Toast.makeText(HomeActivity.this, "good", Toast.LENGTH_LONG) .show();
            }
            else {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
                builder.setTitle("Looks like the microphone permission is disabled. Would you like to open settings to change it?");
                builder.setItems(new CharSequence[]{"Settings", "Cancel"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                navigateToAppSettings();
                                break;
                            case 1:
                                Toast.makeText(HomeActivity.this, "The recording feature will not be available.", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
                builder.show();
            }
        }
    }
    private void navigateToAppSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_RECORD_AUDIO_PERMISSION);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioClassifierHelper.clearTextView();
    }
}
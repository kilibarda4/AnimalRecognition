package com.example.animalrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import com.example.animalrecognition.databinding.ActivityHomeBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.io.File;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private static final String LOG_TAG = "HomeActivity";
//    private static String fileName = null;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    Button btnStartRecording, btnStopRecording;
    TextView result, specs;


    BottomNavigationView bottomNavigationView;

    String modelPath = "yamnet.tflite";
    float probabilityThreshold = 0.3f;
    AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private TimerTask timerTask;

    private Interpreter interpreter;
    private MediaRecorder recorder = null;

    ActivityHomeBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(binding.getRoot());

//        fileName = getExternalCacheDir().getAbsolutePath() + "/audio.wav";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        //initialize firebase for the project
        FirebaseApp.initializeApp(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //ML model download and update
//        CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
//                .requireWifi()
//                .build();
//        FirebaseModelDownloader.getInstance()
//                .getModel("Animal-Classifier", DownloadType.LOCAL_MODEL, conditions)
//                .addOnSuccessListener(model -> {
//                    File modelFile = model.getFile();
//                    if(modelFile != null) {
//                        try {
//                            interpreter = new Interpreter(model.getFile());
//                            Toast.makeText(HomeActivity.this, "LOADED", Toast.LENGTH_LONG).show();
//                        } catch (Exception e) {
//                            Log.e("HomeActivity", "Interpreter Loading Failed", e);
//                            Toast.makeText(HomeActivity.this, "FAILED", Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });

        //fit predict


        //initialize buttons
        btnStartRecording = findViewById(R.id.btnStartRecording);
        btnStopRecording = findViewById(R.id.btnStopRecording);
        result = findViewById(R.id.result);
        specs = findViewById(R.id.specs);

        //navigate using the nav bar
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            } else if (id == R.id.navigation_profile) {
                replaceFragment(new ProfileFragment());
            } else if (id == R.id.navigation_stats) {
                replaceFragment(new StatsFragment());
            } else if (id == R.id.navigation_info) {
                replaceFragment(new InfoFragment());
            }
            return true;
        });

        Bundle params = new Bundle();
        params.putString("startRCD", "startRecordingButton");

        btnStartRecording.setOnClickListener(view -> {
            mFirebaseAnalytics.logEvent("record_button_click", params);
//            startRecording();
            try {
                classifier = AudioClassifier.createFromFile(this, modelPath);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Could not load ML model, please restart the application.",
                                Toast.LENGTH_SHORT)
                                .show();
            }

            tensor = classifier.createInputTensorAudio();

            TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
            String specsStr = "Number of channels: " + format.getChannels() + "\n"
                    + "Sample rate: " + format.getSampleRate();
            specs.setText(specsStr);

            record = classifier.createAudioRecord();
            record.startRecording();

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    int numSamples = tensor.load(record);
                    List<Classifications> output = classifier.classify(tensor);
                    List<Category> finalOutput = new ArrayList<>();
                    for(Classifications classifications : output) {
                        for(Category category : classifications.getCategories()) {
                            if(category.getScore() > probabilityThreshold) {
                                finalOutput.add(category);
                            }
                        }
                    }
                    Collections.sort(finalOutput, (o1, o2) -> (int) (o1.getScore() - o2.getScore()));

                    StringBuilder outputStr = new StringBuilder();
                    for(Category category : finalOutput) {
                        outputStr.append(category.getLabel()).append(": ")
                                .append(category.getScore()).append("\n");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finalOutput.isEmpty()) {
                                result.setText("Could not classify");
                            } else {
                                result.setText(outputStr.toString());
                            }
                        }
                    });

                }
            };
            new Timer().scheduleAtFixedRate(timerTask, 1, 500);
        });

        btnStopRecording.setOnClickListener((v -> {
//            stopRecording();
            timerTask.cancel();
            record.stop();
        }));

        binding.bottomNavigationView.setBackground(null);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted) {
            showPermissionExplanationDialog();
        }
    }

    private void showPermissionExplanationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Required");
        builder.setMessage("The application needs permission to access the microphone. Please grant the permission before proceeding.");
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(HomeActivity.this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(HomeActivity.this,
                        "Permission Denied. The recording feature will not be available. You can change this in the settings",
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
//    private void startRecording() {
//        if (recorder == null) {
//            recorder = new MediaRecorder();
//            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            recorder.setOutputFile(fileName);
//            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//            try {
//                recorder.prepare();
//                recorder.start();
//            } catch (IOException | IllegalStateException e) {
//                Log.e(LOG_TAG, "prepare() failed");
//                Toast.makeText(HomeActivity.this, "Failed to start recording", Toast.LENGTH_LONG).show();
//            }
//            Toast.makeText(HomeActivity.this, fileName, Toast.LENGTH_LONG).show();
//            Log.e(LOG_TAG, fileName);
//        }
//    }
//
//    private void stopRecording() {
//        if (recorder != null) {
//            recorder.stop();
//            recorder.release();
//            recorder = null;
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (recorder != null) {
//            recorder.release();
//            recorder = null;
//        }
//    }
//}
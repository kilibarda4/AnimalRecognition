package com.example.animalrecognition;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AudioClassifierHelper {
    AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private TimerTask timerTask;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private AudioViewModel audioViewModel;

    private TextView result;
    float probabilityThreshold = 0.3f;

    public AudioClassifierHelper(TextView result) {
        this.result = result;
    }

    private void startAudioClassification(Context context, String modelPath, AudioClassifier.AudioClassifierOptions options) {
        try {
            classifier = AudioClassifier.createFromFileAndOptions(context, modelPath, options);
            record = classifier.createAudioRecord();
            tensor = classifier.createInputTensorAudio();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Could not load ML model, please restart the application.", Toast.LENGTH_SHORT).show();
        }

        if (record != null && tensor != null) {
            audioViewModel = AudioViewModel.getInstance((Application) context.getApplicationContext());
            record.startRecording();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    tensor.load(record);
                    List<Classifications> output = classifier.classify(tensor);
//                    HashMap<String, Integer> pieChartResults = new HashMap<>();
                    List<Category> finalOutput = new ArrayList<>();
                    for (Classifications classifications : output) {
                        for (Category category : classifications.getCategories()) {
                            if (category.getScore() > probabilityThreshold
                                    //classes 69 - 131 pertain to animals
                                    && category.getIndex() >= 69
                                    && category.getIndex() <= 131) {
                                finalOutput.add(category);
//                                pieChartResults.put(category.getLabel(), pieChartResults.getOrDefault(category.getLabel(), 0) + 1);
                                audioViewModel.addOrUpdateLabel(category.getLabel());
                            }
                        }
                    }
                    Collections.sort(finalOutput, (o1, o2) -> (int) (o1.getScore() - o2.getScore()));

                    StringBuilder outputStr = new StringBuilder();
                    for (Category category : finalOutput) {
                        outputStr.append(category.getLabel()).append(": ")
                                .append(category.getScore()).append("\n");
                    }
                    mainHandler.post(() -> {
                        if (!finalOutput.isEmpty()) {
                            result.setText(outputStr.toString());
                        } else {
                            result.setText(R.string.result);
                        }
                    });
                }
            };
            new Timer().scheduleAtFixedRate(timerTask, 1, 500);
        }
    }
    public void stopRecording() {
        try {
            timerTask.cancel();
            record.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void checkPermissionAndRecord(Context context, Activity activity, String permission, int requestCode,
                                         String modelPath, AudioClassifier.AudioClassifierOptions options) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(activity, new String[] { permission }, requestCode);
        }
        else {
            startAudioClassification(context, modelPath, options);
        }
    }
    public void clearTextView() {
        this.result = null;
    }
}


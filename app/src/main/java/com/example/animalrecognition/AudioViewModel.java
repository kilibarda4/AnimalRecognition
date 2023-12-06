package com.example.animalrecognition;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import android.app.Application;
import java.util.HashMap;

public class AudioViewModel extends AndroidViewModel {
    private static AudioViewModel instance;
    private HashMap<String, Integer> audioStats = new HashMap<>();
    public AudioViewModel(@NonNull Application application) {
        super(application);
    }
    public static synchronized AudioViewModel getInstance(Application application) {
        if (instance == null) {
            instance = new AudioViewModel(application);
        }
        return instance;
    }
    public void addOrUpdateLabel(String label) {
        audioStats.put(label, audioStats.getOrDefault(label, 0) + 1);
    }
    public HashMap<String, Integer> getAudioStats() {
        return audioStats;
    }
}

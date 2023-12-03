package com.example.animalrecognition;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;

public class AudioViewModel extends ViewModel {
    public HashMap<String, Integer> audioStats = new HashMap<>();

    public void addOrUpdateLabel(String label) {
        // If the label exists, increment the count; otherwise, add the label with count 1
        audioStats.put(label, audioStats.getOrDefault(label, 0) + 1);
    }
}

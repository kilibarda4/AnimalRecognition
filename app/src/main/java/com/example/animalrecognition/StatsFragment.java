package com.example.animalrecognition;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StatsFragment extends Fragment {

    private AudioViewModel audioViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        audioViewModel = AudioViewModel.getInstance(requireActivity().getApplication());

        HashMap<String, Integer> audioStats = audioViewModel.getAudioStats();
        List<DataEntry> data = new ArrayList<>();

        if (audioStats != null && !audioStats.isEmpty()) {
            for (String label : audioStats.keySet()) {
                Integer count = audioStats.get(label);
                data.add(new ValueDataEntry(label, count));
            }
        } else {
            // If no recordings, add a placeholder entry with 0 count
            data.add(new ValueDataEntry("No Recordings", 0));
        }

        Pie pie = AnyChart.pie();
        pie.title("Top Classifications by Frequency of Occurrence");
        pie.title().margin(20);
        pie.background(String.valueOf(true));
        pie.background("#ffd700");

        pie.data(data);
        AnyChartView anyChartView = view.findViewById(R.id.any_chart_view);
        anyChartView.setChart(pie);

        return view;
    }
}


/*
        69 - 75 & 117 -> dog
        76 - 80 -> cat
        82 - 84 -> horse
        86 - 87 -> cow
        88 - 89 -> pig
        90 - 92 -> goat/sheep
        93 - 102 -> duck/chicken/fowl
        103 - 105 -> wild cats
        106 - 116 -> bird
        118 - 119 -> rodents
        121 - 126 -> flying insects
        127 - 128 -> frog
        129 - 130 -> snake
        131 -> whale

        public String getLabel(int index) {
            if index >= 69 && <= 75 || index == 117 return dog
            if index >= 76 && <= 80 return cat;
            if index >= 82 && <= 84 return horse
        }
        */
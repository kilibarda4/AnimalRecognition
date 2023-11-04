package com.example.animalrecognition;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.animalrecognition.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;

public class HomeActivity extends AppCompatActivity {

    Button startRecordingBtn, stopRecordingBtn;
    TextView result;
    BottomNavigationView bottomNavigationView;
    ActivityHomeBinding binding;
    private FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //initialize firebase for the project
        FirebaseApp.initializeApp(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        //initialize buttons
        startRecordingBtn = findViewById(R.id.startRecordingBtn);
        stopRecordingBtn = findViewById(R.id.stopRecordingBtn);
        result   = findViewById(R.id.result);

        //navigate using the nav bar
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.navigation_home) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
            else if(id == R.id.navigation_profile) {
                replaceFragment(new ProfileFragment());
            }
            else if(id == R.id.navigation_stats) {
                replaceFragment(new StatsFragment());
            }
            else if(id == R.id.navigation_info) {
                replaceFragment(new InfoFragment());
            }
            return true;
        });

        Bundle params = new Bundle();
        params.putString("startRCD", "startRecordingButton");
        startRecordingBtn.setOnClickListener(view -> mFirebaseAnalytics.logEvent("record_button_click", params));

        binding.bottomNavigationView.setBackground(null);
    }
    private void replaceFragment (Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
package com.example.animalrecognition;
import android.widget.Button;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Arrays;
import java.util.List;

public class InfoFragment extends Fragment {

    // ... (your existing code)

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        List<Integer> images = Arrays.asList(
                R.drawable.image1,
                R.drawable.image2,
                R.drawable.image3
                // Add more images as needed
        );

        ImageAdapter imageAdapter = new ImageAdapter(images);
        viewPager.setAdapter(imageAdapter);

        // Find the Button by its ID
        Button learnMoreButton = view.findViewById(R.id.learnMoreButton);

        // Set OnClickListener
        learnMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage();
            }
        });

        return view;
    }

    // ... (your existing code)

    private void openWebPage() {
        // Handle the click event, e.g., open a web browser with the URL
        String url = getString(R.string.model_webpage); // Get the URL from resources
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}

package com.example.animalrecognition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;
import java.util.List;

public class InfoFragment extends Fragment {

    public InfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_info, container, false);

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

        return view;
    }

    public void openWebPage(View view) {
        // Handle the click event, e.g., open a web browser with the URL
        String url = getString(R.string.model_webpage); // Get the URL from resources
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
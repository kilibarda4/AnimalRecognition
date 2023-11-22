package com.example.animalrecognition;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class InfoFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);


        ViewPager viewPager = view.findViewById(R.id.imageViewPager);
        TabLayout tabLayout = view.findViewById(R.id.tabLayout);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ImageSlideFragment.newInstance(R.drawable.image1));
        fragments.add(ImageSlideFragment.newInstance(R.drawable.image2));
        fragments.add(ImageSlideFragment.newInstance(R.drawable.image3));


        SlideshowPagerAdapter adapter = new SlideshowPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(adapter);


        viewPager.setPageTransformer(true, new CubeOutPageTransformer());


        tabLayout.setupWithViewPager(viewPager);

        Button websiteButton = view.findViewById(R.id.websiteButton);

        websiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String websiteUrl = "https://www.google.com"; // Replace with  actual website URL


                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));


                startActivity(intent);
            }
        });

        return view;
    }
}

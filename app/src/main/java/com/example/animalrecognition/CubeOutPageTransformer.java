package com.example.animalrecognition;

import android.os.Build;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;

@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class CubeOutPageTransformer implements ViewPager.PageTransformer {

    private static final float MAX_ROTATION = 35.0f;

    @Override
    public void transformPage(View page, float position) {
        // Normalize position to be between -1 and 1
        position = Math.max(-1, Math.min(1, position));

        // Calculate the rotation angle
        float rotation = position * MAX_ROTATION;

        // Set pivot point for rotation
        page.setPivotX(page.getWidth() * 0.5f);
        page.setPivotY(page.getHeight() * 0.5f);

        // Set the rotation
        page.setRotationY(rotation);
    }
}

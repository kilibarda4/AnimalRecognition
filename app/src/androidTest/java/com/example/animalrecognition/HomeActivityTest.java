package com.example.animalrecognition;

import static org.junit.Assert.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    private ActivityScenario<HomeActivity> scenario;

    @Before
    public void setUp() {
        scenario = ActivityScenario.launch(HomeActivity.class);
    }

    @Test
    public void onCreate() {
        scenario.onActivity(activity -> {
            assertNotNull(activity.findViewById(R.id.btnStartRecording));
            assertNotNull(activity.findViewById(R.id.btnStopRecording));
            assertNotNull(activity.findViewById(R.id.result));
        });
    }
    @Test
    public void navigation_selectProfile_navigatesToProfileFragment() {
        Espresso.onView(ViewMatchers.withId(R.id.navigation_profile))
                .perform(ViewActions.click());
        scenario.onActivity(activity -> {
            Fragment visibleFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            assertTrue(visibleFragment instanceof ProfileFragment);
        });
    }
    @Test
    public void navigation_selectStats_navigatesToStatsFragment() {
        Espresso.onView(ViewMatchers.withId(R.id.navigation_stats))
                .perform(ViewActions.click());
        scenario.onActivity(activity -> {
            Fragment visibleFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            assertTrue(visibleFragment instanceof StatsFragment);
        });
    }
    @Test
    public void navigation_selectInfo_navigatesToInfoFragment() {
        Espresso.onView(ViewMatchers.withId(R.id.navigation_info))
                .perform(ViewActions.click());
        scenario.onActivity(activity -> {
           Fragment visibleFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frame_layout);
           assertTrue(visibleFragment instanceof InfoFragment);
        });
    }

}
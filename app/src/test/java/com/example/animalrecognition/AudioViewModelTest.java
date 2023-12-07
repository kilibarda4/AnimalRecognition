package com.example.animalrecognition;

import static org.junit.Assert.*;

import android.app.Application;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class AudioViewModelTest {

    private Application application;
    private AudioViewModel audioViewModel;
    @Before
    public void setUp() {
        application = Mockito.mock(Application.class);
        //get a new instance before each test
        AudioViewModel.destroyInstance();
        audioViewModel = AudioViewModel.getInstance(application);
    }

    @After
    public void tearDown() {
        AudioViewModel.destroyInstance();
    }

    //ensure the Singleton method works and properly returns
    // the same instance
    @Test
    public void getInstance_returnSameInstance() {
        AudioViewModel instance1 = AudioViewModel.getInstance(application);
        AudioViewModel instance2 = AudioViewModel.getInstance(application);
        assertSame(instance1, instance2);
    }

    //create instance, check that the key is present, then destroy and recreate
    //to ensure that the initial instance was destroyed
    @Test
    public void destroyInstance() {
        String testLabel = "testLabel";
        audioViewModel.addOrUpdateLabel(testLabel);
        AudioViewModel.destroyInstance();
        assertTrue(audioViewModel.getAudioStats().containsKey(testLabel));
        audioViewModel = AudioViewModel.getInstance(application);
        assertFalse(audioViewModel.getAudioStats().containsKey(testLabel));
    }

    //assert adding properly and adding additional values with same label
    @Test
    public void addOrUpdateLabel_updatesHashMap() {
        String testLabel = "testLabel";
        audioViewModel.addOrUpdateLabel(testLabel);
        assertTrue(audioViewModel.getAudioStats().containsKey(testLabel));
        assertEquals(1, (int) audioViewModel.getAudioStats().get(testLabel));
        audioViewModel.addOrUpdateLabel(testLabel);
        assertEquals(2, (int) audioViewModel.getAudioStats().get(testLabel));
    }

    //assert retrieval of hashmap works as intended
    @Test
    public void getAudioStats() {
        String testLabel = "testLabel";
        audioViewModel.addOrUpdateLabel(testLabel);
        HashMap<String, Integer> audioStats = audioViewModel.getAudioStats();
        assertTrue(audioStats.containsKey(testLabel));
        assertEquals(1, (int) audioStats.get(testLabel));
    }
}
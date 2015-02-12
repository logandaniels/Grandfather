package com.inglesoft.grandfather;


import android.os.Bundle;
import android.preference.PreferenceFragment;


public class SettingsFragment extends PreferenceFragment {

    public static final String PREF_KEY_TTS_VOLUME = "pref_key_tts_volume";
    public static final String PREF_KEY_TTS_DURATION = "pref_key_tts_duration";

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }


}

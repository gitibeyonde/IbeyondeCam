package com.ibeyonde.cam.ui.setting;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.ibeyonde.cam.R;

public class SettingFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}
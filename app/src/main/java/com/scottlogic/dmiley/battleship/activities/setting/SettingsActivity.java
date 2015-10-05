package com.scottlogic.dmiley.battleship.activities.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import com.scottlogic.dmiley.battleship.R;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}

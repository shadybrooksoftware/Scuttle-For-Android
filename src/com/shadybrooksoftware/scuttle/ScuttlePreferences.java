package com.shadybrooksoftware.scuttle;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class ScuttlePreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.preferences);
	}
}

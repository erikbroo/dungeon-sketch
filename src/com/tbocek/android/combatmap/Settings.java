package com.tbocek.android.combatmap;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Preferences activity for Dungeon Sketch.
 * @author Tim Bocek
 *
 */
public final class Settings extends PreferenceActivity {
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.settings);
	}
}

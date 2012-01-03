package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

/**
 * Preferences activity for Dungeon Sketch.
 * @author Tim Bocek
 *
 */
public final class Settings extends PreferenceActivity {
	public static final int DIALOG_ID_ABOUT = 0;
	
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.settings);
        
        // Hook up the about preference
        Preference dialogPref = (Preference) findPreference("about");
        dialogPref.setOnPreferenceClickListener(
        	new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					showDialog(DIALOG_ID_ABOUT);
					return true;
				}
        		
        	});
    }
    
    @Override
    public Dialog onCreateDialog(final int id) {
    	switch(id) {
    	case DIALOG_ID_ABOUT:
    		return new AboutDialog(this);
    	default:
    		return null;
    	}
    }
}

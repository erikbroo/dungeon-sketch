package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

/**
 * Preferences activity for Dungeon Sketch.
 * @author Tim Bocek
 *
 */
public final class Settings extends PreferenceActivity {
	public static final int DIALOG_ID_ABOUT = 0;
	public static final int DIALOG_ID_ART_CREDITS = 1;
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
        
        Preference artCreditPref = (Preference) findPreference("artcredits");
        artCreditPref.setOnPreferenceClickListener(
        	new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivity(new Intent(Settings.this, ArtCredits.class));
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(this, CombatMap.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

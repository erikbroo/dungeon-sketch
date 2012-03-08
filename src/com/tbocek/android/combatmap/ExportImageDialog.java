package com.tbocek.android.combatmap;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

/**
 * Provides a dialog for the user to export an image.
 * @author Tim
 *
 */
public class ExportImageDialog extends Dialog {
	
	RadioButton mRadioExportFullMap;
	RadioButton mRadioExportCurrentView;
	CheckBox mCheckGridLines;
	CheckBox mCheckGmNotes;
	CheckBox mCheckTokens;
	CheckBox mCheckAnnotations;
	CheckBox mCheckFogOfWar;
	
	private class SetBooleanPreferenceHandler
		implements CompoundButton.OnCheckedChangeListener{
		String mPreference;
		public SetBooleanPreferenceHandler(String preference) {
			mPreference = preference;
		}
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			SharedPreferences sharedPreferences = 
					PreferenceManager.getDefaultSharedPreferences(
							getContext());
			Editor editor = sharedPreferences.edit();
			editor.putBoolean(mPreference, isChecked);
			editor.commit();
		}
	}
	
	/**
	 * Constructor.
	 * @param context Application context to use.
	 */
	public ExportImageDialog(Context context) {
		super(context);
		this.setTitle("Export Image");
		this.setContentView(R.layout.export_dialog);
		
		mRadioExportFullMap = (RadioButton) this.findViewById(R.id.radio_export_full_map);
		mRadioExportCurrentView = (RadioButton) this.findViewById(R.id.radio_export_full_map);
	    mCheckGridLines = (CheckBox) this.findViewById(R.id.checkbox_export_grid_lines);
		mCheckGmNotes = (CheckBox) this.findViewById(R.id.checkbox_export_gm_notes); 
		mCheckTokens = (CheckBox) this.findViewById(R.id.checkbox_export_tokens);
		mCheckAnnotations = (CheckBox) this.findViewById(R.id.checkbox_export_annotations);
		mCheckFogOfWar = (CheckBox) this.findViewById(R.id.checkbox_export_fog_of_war);
		
		associateControl(mRadioExportFullMap, "export_full_map", true);
		associateControl(mRadioExportCurrentView, "export_current_view", false);
		associateControl(mCheckGridLines, "export_grid_lines", true);
		associateControl(mCheckGmNotes, "export_gm_notes", false);
		associateControl(mCheckTokens, "export_tokens", true);
		associateControl(mCheckAnnotations, "export_annotations", false);
		associateControl(mCheckFogOfWar, "export_fog_of_war", false);
	}

	private void associateControl(CompoundButton b, String pref, boolean defaultValue) {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getContext());
		b.setChecked(prefs.getBoolean(pref, defaultValue));
		b.setOnCheckedChangeListener(new SetBooleanPreferenceHandler(pref));
	}
	
}

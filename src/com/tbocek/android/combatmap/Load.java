package com.tbocek.android.combatmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
 
public class Load extends ListActivity {
	private List<String> mSavedFiles;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		List<Map<String, String>> filenameMapList = new ArrayList<Map<String, String>>();
		
		DataManager dataMgr = new DataManager(this.getApplicationContext());
		
		mSavedFiles = dataMgr.savedFiles();
		for (String mapName : mSavedFiles) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("mapName", mapName);
			filenameMapList.add(data);
		}
		
		ListAdapter adapter = new SimpleAdapter(
			this.getApplicationContext(),
			filenameMapList,
			R.layout.saved_map_file,
			new String[] {"mapName"},
			new int[] {R.id.saved_map_file_name}	
		);
		
		setListAdapter(adapter);
	}
	
	protected void onListItemClick(ListView i, View v, int position, long id) {
		setFilenamePreference(mSavedFiles.get(position));
		finish();
	}
	
	private void setFilenamePreference(String newFilename) {
		//TODO(tim.bocek): De-dupe from method in CombatMap
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    	// Persist the filename that we saved to so that we can load from that file again.
    	Editor editor = sharedPreferences.edit();
    	editor.putString("filename", newFilename);
    	editor.commit();
    }
}

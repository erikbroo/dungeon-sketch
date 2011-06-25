package com.tbocek.android.combatmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tbocek.android.combatmap.view.SaveFileButton;
import com.tbocek.android.combatmap.view.TileView;

import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
 
public class Load extends Activity {
	private List<String> mSavedFiles;
	private DataManager dataMgr;
	TileView scrollingView;
	
	private static final int FILE_VIEW_WIDTH = 200;
	private static final int FILE_VIEW_HEIGHT = 200;
	private static final int FILE_VIEW_PADDING = 16;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		List<Map<String, String>> filenameMapList = new ArrayList<Map<String, String>>();
		
		dataMgr = new DataManager(this.getApplicationContext());
		
		setup();
		
	}

	private void setup() {
		mSavedFiles = dataMgr.savedFiles();
		
		List<View> fileViews = new ArrayList<View>();
		
		for (String saveFile: mSavedFiles) {
			SaveFileButton b = createSaveFileButton(saveFile);
			fileViews.add(b);
		}
		View layout = createLayout(fileViews);
		ScrollView scroller = new ScrollView(this);
		scroller.addView(layout);
		this.setContentView(scroller);
	}
	
	private View createLayout(List<View> views) {
		TableLayout layout = new TableLayout(this);
		TableRow currentRow = null;
		int viewsPerRow = getWindowManager().getDefaultDisplay().getWidth() / (FILE_VIEW_WIDTH + 2 * FILE_VIEW_PADDING); //TODO: how to compute this
		int i = 0;
		for (View v: views) {
			if (i%viewsPerRow == 0) {
				currentRow = new TableRow(this);
				layout.addView(currentRow);
			}
			currentRow.addView(v);
			++i;
		}
		return layout;
	}

	private SaveFileButton createSaveFileButton(String saveFile) {
		SaveFileButton b = new SaveFileButton(this);
		b.setFileName(saveFile);
		try {
			b.setPreviewImage(dataMgr.loadImage(saveFile + ".preview.jpg")); //TODO: make this code not suck (preview filename creation logic should be in DataManager)
		} catch (IOException e) {
			//no-op
		}
		b.setPadding(FILE_VIEW_PADDING, FILE_VIEW_PADDING, FILE_VIEW_PADDING, FILE_VIEW_PADDING);
		b.setMinimumWidth(FILE_VIEW_WIDTH);
		b.setMinimumHeight(FILE_VIEW_HEIGHT);
		b.setOnClickListener(new SaveFileButtonClickListener(saveFile));
		registerForContextMenu(b);
		return b;
	}
	
	private void setFilenamePreference(String newFilename) {
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    	// Persist the filename that we saved to so that we can load from that file again.
    	Editor editor = sharedPreferences.edit();
    	editor.putString("filename", newFilename);
    	editor.commit();
    }
	
	public class SaveFileButtonClickListener implements View.OnClickListener{
		private String filename;
		public SaveFileButtonClickListener(String filename) {
			this.filename = filename;
		}
		
		@Override
		public void onClick(View arg0) {
			setFilenamePreference(filename);
			finish();
		}
	}
	
	SaveFileButton contextMenuTrigger = null;
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		contextMenuTrigger = (SaveFileButton) v;
		getMenuInflater().inflate(R.menu.save_file_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	  AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	  switch (item.getItemId()) {
	  case R.id.delete_save_file:
		  dataMgr.deleteSaveFile(contextMenuTrigger.getFileName());
		  setup();
		  return true;
	  default:
		  return super.onContextItemSelected(item);
	  }
	}
}

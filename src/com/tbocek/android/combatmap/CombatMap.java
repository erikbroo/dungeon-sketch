package com.tbocek.android.combatmap;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.Grid;
import com.tbocek.android.combatmap.graphicscore.GridColorScheme;
import com.tbocek.android.combatmap.graphicscore.MapData;
import com.tbocek.android.combatmap.graphicscore.SolidColorToken;
import com.tbocek.android.combatmap.view.CombatView;
import com.tbocek.android.combatmap.view.DrawOptionsView;
import com.tbocek.android.combatmap.view.TokenSelectorView;

public class CombatMap extends Activity {
	
	private static final String TOKEN_IMAGE_DIRECTORY = "/sdcard/dungeon_sketch_tokens";
	
	private CombatView mCombatView;
	private TokenSelectorView mTokenSelector;
	private FrameLayout mBottomControlFrame;
	private DrawOptionsView mDrawOptionsView;
	private static MapData mData = new MapData();
	
	private TokenSelectorView.OnTokenSelectedListener mOnTokenSelectedListener = new TokenSelectorView.OnTokenSelectedListener() {
		@Override
		public void onTokenSelected(BaseToken t) {
			mCombatView.placeToken(t);
		}
	};
	
	private DrawOptionsView.OnChangeDrawToolListener mOnChangeDrawToolListener = new DrawOptionsView.OnChangeDrawToolListener() {
		
		@Override
		public void onChooseEraser() {
			mCombatView.setEraseMode();
		}
		
		@Override
		public void onChooseColoredPen(int color) {
			// TODO Auto-generated method stub
			mCombatView.setDrawMode();
			mCombatView.newLineColor = color;
		}

		@Override
		public void onChoosePanTool() {
			// TODO Auto-generated method stub
			mCombatView.setZoomPanMode();
		}

		@Override
		public void onChooseStrokeWidth(int width) {
			mCombatView.newLineStrokeWidth = width;
			
		}
	};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.combat_map_layout);
        
        mCombatView = new CombatView(this, mData);
        this.registerForContextMenu(mCombatView);
        
        mTokenSelector = new TokenSelectorView(this.getApplicationContext());
        mTokenSelector.setOnTokenSelectedListener(mOnTokenSelectedListener);
        
        mDrawOptionsView = new DrawOptionsView(this.getApplicationContext());
        mDrawOptionsView.setOnChangeDrawToolListener(mOnChangeDrawToolListener);
        
        FrameLayout mainContentFrame = (FrameLayout) this.findViewById(R.id.mainContentFrame);
        mBottomControlFrame = (FrameLayout) this.findViewById(R.id.bottomControlAreaFrame);
        
        mainContentFrame.addView(mCombatView);
        mBottomControlFrame.addView(mTokenSelector);
        
        mCombatView.requestFocus();
    }
    
    @Override 
    public void onResume() {
    	super.onResume();
    	//Reload preferences
    	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
    	String colorScheme = sharedPreferences.getString("theme", "graphpaper");
    	String gridType = sharedPreferences.getString("gridtype", "rect");
    	mData.grid = Grid.createGrid(gridType, colorScheme, mData.grid.gridSpaceToWorldSpaceTransformer());
    	mCombatView.invalidate();
    }
    
    MenuItem backgroundLayerItem;
    MenuItem annotationLayerItem;
    MenuItem combatItem;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.combat_map_menu, menu);
    	backgroundLayerItem = menu.findItem(R.id.edit_background);
    	annotationLayerItem = menu.findItem(R.id.edit_annotations);
    	combatItem = menu.findItem(R.id.combat_on);
    	disableCurrentMode(combatItem); //Starts out in combat mode.
    	return true;
    }
    
    /**
     * If the selected menu item is a drawing mode, disable it.
     * @param modeItem
     */
    private void disableCurrentMode(MenuItem modeItem) {
    	backgroundLayerItem.setEnabled(modeItem != backgroundLayerItem);
    	annotationLayerItem.setEnabled(modeItem != annotationLayerItem);
    	combatItem.setEnabled(modeItem != combatItem);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.edit_background:
        	mCombatView.setDrawMode();
        	mCombatView.useBackgroundLayer();
        	mBottomControlFrame.removeAllViews();
        	mBottomControlFrame.addView(this.mDrawOptionsView);
        	disableCurrentMode(item);
        	return true;
        case R.id.edit_annotations:
        	mCombatView.setDrawMode();
        	mCombatView.useAnnotationLayer();
        	mBottomControlFrame.removeAllViews();
        	mBottomControlFrame.addView(this.mDrawOptionsView);
        	disableCurrentMode(item);
        	return true;
        case R.id.combat_on:
        	mCombatView.setTokenManipulationMode();
        	mBottomControlFrame.removeAllViews();
        	mBottomControlFrame.addView(mTokenSelector);
        	disableCurrentMode(item);
        	return true;
        case R.id.clear_all:
        	mCombatView.clearAll();
        	return true;
        case R.id.snap_to_grid:
        	item.setChecked(!item.isChecked());
        	mCombatView.shouldSnapToGrid = item.isChecked();
        	return true;
        case R.id.settings:
        	startActivity(new Intent(this, Settings.class));
        	return true;
        case R.id.resize_grid:
        	mCombatView.setResizeGridMode();
        	mBottomControlFrame.removeAllViews();
        	disableCurrentMode(item);
        }

        return false;
    }
    
    private void loadImages() {
    	//TODO: Use a worker thread here.
    	
    	// Make sure we can read the internal storage
    	if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    		return;
    	}
    	
    	// Make sure the TOKEN_IMAGE_DIRECTORY exists, creating it if necessary
    	File tokenImageDirectory = new File(TOKEN_IMAGE_DIRECTORY);
    	tokenImageDirectory.mkdirs();

    	for (File file: tokenImageDirectory.listFiles()) {
    		
    	}
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);

    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	return mCombatView.onContextItemSelected(item);
    }

    
}
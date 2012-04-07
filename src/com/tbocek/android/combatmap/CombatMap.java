package com.tbocek.android.combatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
import com.tbocek.android.combatmap.model.Grid;
import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.MapDrawer.FogOfWarMode;
import com.tbocek.android.combatmap.model.MultiSelectManager;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Text;
import com.tbocek.android.combatmap.model.primitives.Util;
import com.tbocek.android.combatmap.tokenmanager.TokenManager;
import com.tbocek.android.combatmap.view.CombatView;
import com.tbocek.android.combatmap.view.DrawOptionsView;
import com.tbocek.android.combatmap.view.TagListView;
import com.tbocek.android.combatmap.view.TokenSelectorView;

/**
 * This is the main activity that allows the user to sketch a map, and place
 * and manipulate tokens.  Most of the application logic that does not relate
 * to token management occurs in this activity or one of its views.
 *
 * @author Tim Bocek
 */
public final class CombatMap extends SherlockActivity {
	/**
	 * Identifier for the draw background mode.
	 */
	private static final int MODE_DRAW_BACKGROUND = 1;

	/**
	 * Identifier for the manipulate tokens mode.
	 */
	private static final int MODE_TOKENS = 2;

	/**
	 * Identifier for the draw annotations mode.
	 */
	private static final int MODE_DRAW_ANNOTATIONS = 3;

	/**
	 * Identifier for the draw GM notes mode.
	 */
	private static final int MODE_DRAW_GM_NOTES = 4;

	/**
	 * Text size to use in the list of tags.
	 */
	private static final int TAG_LIST_TEXT_SIZE = 20;

    /**
     * Dialog ID to use for the save file dialog.
     */
    private static final int DIALOG_ID_SAVE = 0;
    
    /**
     * Dialog ID to use for the draw text dialog.
     */
    private static final int DIALOG_ID_DRAW_TEXT = 1;
    
    /**
     * Dialog ID to use when confirming a save file name, in case the name would
     * overwrite a different map file.
     */
    private static final int DIALOG_ID_SAVE_NAME_CONFIRM = 2;
    
	private static final int DIALOG_ID_GRID_PROPERTIES = 3;
	
	private static final int DIALOG_ID_EXPORT = 4;
    
    /**
     * Maximum height of the popup tag selector.  Must be scaled.
     */
    private static final int POPUP_AREA_HEIGHT = 200;
    
    /**
     * The current map.
     */
    private static MapData mData;
    
    /**
     * The view that manages the main canvas for drawing and tokens.
     */
    private CombatView mCombatView;

    /**
     * This frame renders on the bottom of the screen to provide controls
     * related to the current interaction mode, i.e. the token list or drawing
     * tools.
     */
    private FrameLayout mBottomControlFrame;

    /**
     * This view provides an area to render controls in a region that draws
     * over the main canvas and can be displayed or hidden as needed.  Currently
     * used to draw the token category selector.
     */
    private FrameLayout mPopupFrame;

    /**
     * The view that allows the user to select a token for the map.
     */
    private TokenSelectorView mTokenSelector;

    /**
     * The view that allows the user to select a drawing tool or color.
     */
    private DrawOptionsView mDrawOptionsView;

    /**
     * The view that allows the user to select a token category to display in
     * the token selector.
     */
    private TagListView mTokenCategorySelector;


    /**
     * Database of available combat tokens.
     */
    private TokenDatabase mTokenDatabase;

    /**
     * Whether the control tray on the bottom of the screen is expanded.
     */
    private boolean mIsControlTrayExpanded = true;
    
    /**
     * The menu item that controls whether drawing/tokens snap to the grid.
     * Saved because we need to listen for these events.
     */
    private MenuItem mSnapToGridMenuItem;
    
    /**
     * The saved menu item that performs the undo operation.
     */
    private MenuItem mUndoMenuItem;
    
    /**
     * The saved menu item that performs the redo operation.
     */
    private MenuItem mRedoMenuItem;
    
    /**
     * Whether the tag selector is visible.
     */
    private boolean mTagSelectorVisible;
    
	/**
	 * The action mode that was started to manage the selection.
	 */
	private ActionMode mMultiSelectActionMode;

    /**
     * Listener that fires when a token has been selected in the token selector
     * view.
     */
    private TokenSelectorView.OnTokenSelectedListener mOnTokenSelectedListener =
        new TokenSelectorView.OnTokenSelectedListener() {
        @Override
        public void onTokenSelected(final BaseToken t) {
            mCombatView.placeToken(t);
        }
    };

    /**
     * Listener that fires when a new draw tool or color has been selected.
     */
    private DrawOptionsView.OnChangeDrawToolListener mOnChangeDrawToolListener =
        new DrawOptionsView.OnChangeDrawToolListener() {

        @Override
        public void onChooseEraser() {
            mCombatView.setEraseMode();
        }

        @Override
        public void onChooseColoredPen(final int color) {
            mCombatView.setNewLineColor(color);
        }

        @Override
        public void onChoosePanTool() {
            mCombatView.setZoomPanMode();
        }

        @Override
        public void onChooseStrokeWidth(final float width) {
            mCombatView.setNewLineStrokeWidth(width);
        }

		@Override
		public void onChooseMaskTool() {
            mCombatView.setFogOfWarDrawMode();
            mCombatView.setNewLineStyle(CombatView.NewLineStyle.FREEHAND);
		}

		@Override
		public void onChooseFreeHandTool() {
            mCombatView.setDrawMode();
			mCombatView.setNewLineStyle(CombatView.NewLineStyle.FREEHAND);
		}

		@Override
		public void onChooseStraightLineTool() {
            mCombatView.setDrawMode();
			mCombatView.setNewLineStyle(CombatView.NewLineStyle.STRAIGHT);
		}

		@Override
		public void onChooseCircleTool() {
			mCombatView.setDrawMode();
			mCombatView.setNewLineStyle(CombatView.NewLineStyle.CIRCLE);
		}

		@Override
		public void onChooseTextTool() {
			mCombatView.setTextMode();

		}

		@Override
		public void onChooseRectangleTool() {
			mCombatView.setDrawMode();
			mCombatView.setNewLineStyle(CombatView.NewLineStyle.RECTANGLE);
		}

		@Override
		public void onChooseImageTool() {
			mCombatView.setBackgroundImageMode();
		}
    };


    /**
     * Callback that loads the correct interaction mode when a new tab is
     * selected.
     */
    private TabManager.TabSelectedListener mTabSelectedListener =
    		new TabManager.TabSelectedListener() {
		@Override
		public void onTabSelected(int tab) {
			if (mData != null) {
				setManipulationMode(tab);
			}
		}
	};

    /**
     * Listener that fires when a new token category is selected.
     */
    private TagListView.OnTagListActionListener mOnTagListActionListener =
    	new TagListView.OnTagListActionListener() {

			@Override
			public void onDragTokensToTag(
					final Collection<BaseToken> tokens, final String tag) {
			}

			@Override
			public void onChangeSelectedTag(final String newTag) {
				mTokenSelector.setSelectedTag(newTag, mCombatView);
			}
		};
		

		/**
		 * Callback to listen for text edit/creation requests and load the
		 * required dialog, since dialogs need to be managed at the activity
		 * level.
		 */
		private CombatView.NewTextEntryListener mOnNewTextEntryListener = 
				new CombatView.NewTextEntryListener() {

			@Override
			public void requestNewTextEntry(PointF newTextLocationWorldSpace) {
				mEditedTextObject = null;
				mNewTextLocationWorldSpace = newTextLocationWorldSpace;
				showDialog(DIALOG_ID_DRAW_TEXT);
			}

			@Override
			public void requestEditTextObject(Text t) {
				mEditedTextObject = t;
				showDialog(DIALOG_ID_DRAW_TEXT);
			}
		
	};
	
	/**
	 * Location at which to place text, in world space, should the new text
	 * dialog be accepted.
	 */
	private PointF mNewTextLocationWorldSpace;

	/**
	 * The text object that the edit dialog is currently editing, or null if
	 * a new text object is being created.
	 */
	private Text mEditedTextObject;

	/**
	 * Object to manage which mode is changed to when a new tab is selected.
	 */
	private TabManager mTabManager;
	
	/**
	 * The attempted save name used when an extra saved prompt is needed (i.e.
	 * when saving over a different map).
	 */
	private String mAttemptedMapName;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
    	DeveloperMode.strictMode();
    	// android.os.Debug.startMethodTracing("main_activity_load");
        super.onCreate(savedInstanceState);

        BuiltInImageToken.registerResources(
                this.getApplicationContext().getResources());
        
        PreferenceManager.setDefaultValues(this, R.layout.settings, false);

        // Set up the tabs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setContentView(R.layout.combat_map_layout);
        	ActionBar actionBar = getSupportActionBar();
        	mTabManager = new ActionBarTabManager(actionBar);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // Clear the title on the action bar, since we want to leave more 
            // space for the tabs.
            actionBar.setTitle("");
        } else {
        	this.setContentView(R.layout.combat_map_layout);
        	View legacyActionBar = this.findViewById(R.id.legacyActionBar);
        	legacyActionBar.setVisibility(View.VISIBLE);
        	mTabManager = new LegacyTabManager(
        			(LinearLayout) this.findViewById(
        					R.id.legacyActionBarLayout));
        }


        mCombatView = new CombatView(this);
        this.registerForContextMenu(mCombatView);
        mCombatView.setNewTextEntryListener(this.mOnNewTextEntryListener);

        mTokenSelector = new TokenSelectorView(this.getApplicationContext());

        // Set up listeners for the token selector's category and manager
        // buttons.
        mTokenSelector.setOnTokenSelectedListener(mOnTokenSelectedListener);
        mTokenSelector.setOnClickTokenManagerListener(
                new View.OnClickListener() {
            @Override
            public void onClick(final View arg0) {
                Debug.startMethodTracing("tokenmanager");
                startActivity(new Intent(CombatMap.this, TokenManager.class));
            }
        });

        mTokenSelector.setOnClickGroupSelectorListener(
                new View.OnClickListener() {

            
            @Override
            public void onClick(final View arg0) {
            	setTagSelectorVisibility(!mTagSelectorVisible);
            }
        });

        mDrawOptionsView = new DrawOptionsView(this.getApplicationContext());
        mDrawOptionsView.setOnChangeDrawToolListener(mOnChangeDrawToolListener);

        FrameLayout mainContentFrame =
            (FrameLayout) this.findViewById(R.id.mainContentFrame);
        mBottomControlFrame =
            (FrameLayout) this.findViewById(R.id.bottomControlAreaFrame);
        mPopupFrame =
            (FrameLayout) this.findViewById(R.id.popupControlAreaFrame);

        mTokenCategorySelector = new TagListView(this);
        mTokenCategorySelector.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mTokenCategorySelector.setOnTagListActionListener(
        		mOnTagListActionListener);
        mTokenCategorySelector.setTextSize(TAG_LIST_TEXT_SIZE);

        mPopupFrame.addView(mTokenCategorySelector);

        mainContentFrame.addView(mCombatView);
        mBottomControlFrame.addView(mTokenSelector);

        final ImageButton collapseButton =
        	(ImageButton) this.findViewById(R.id.bottomControlAreaExpandButton);
        collapseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				mIsControlTrayExpanded = !mIsControlTrayExpanded;
				if (mIsControlTrayExpanded) {
					mBottomControlFrame.getLayoutParams().height =
						RelativeLayout.LayoutParams.WRAP_CONTENT;
					collapseButton.setImageResource(
							R.drawable.vertical_contract);
				} else {
					mBottomControlFrame.getLayoutParams().height = 0;
					collapseButton.setImageResource(
							R.drawable.vertical_expand);
				}
				findViewById(R.id.combatMapMainLayout).requestLayout();
			}
        });

        loadOrCreateMap();

        if (mTabManager != null) {
        	mTabManager.addTab(getString(R.string.background), MODE_DRAW_BACKGROUND);
            mTabManager.addTab(getString(R.string.gm_notes), MODE_DRAW_GM_NOTES);
            mTabManager.addTab(getString(R.string.combat), MODE_TOKENS);
            mTabManager.addTab(getString(R.string.annotations), MODE_DRAW_ANNOTATIONS);
            mTabManager.setTabSelectedListener(mTabSelectedListener);
        }
        
        reloadPreferences();
        
        mCombatView.setOnRefreshListener(new CombatView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				// When the map is refreshed, update the undo/redo status as
				// well.
				setUndoRedoEnabled();
			}
		});
        
        mCombatView.getMultiSelect().setSelectionChangedListener(new SelectionChangedListener());
        
        mCombatView.refreshMap();
        mCombatView.requestFocus();
    }
    
    /**
     * Queries the undo/redo state and sets the enabled state for the menu
     * items.
     */
    private void setUndoRedoEnabled() {
    	if (mCombatView == null || mCombatView.getUndoRedoTarget() == null) {
    		return;
    	}
    	
		if (mUndoMenuItem != null) {
			mUndoMenuItem.setEnabled(
					mCombatView.getUndoRedoTarget().canUndo());
			mUndoMenuItem.setIcon(
					mUndoMenuItem.isEnabled() 
						? R.drawable.undo 
						: R.drawable.undo_greyscale);
		}
		if (mRedoMenuItem != null) {
			mRedoMenuItem.setEnabled(
					mCombatView.getUndoRedoTarget().canRedo());
			mRedoMenuItem.setIcon(
					mRedoMenuItem.isEnabled() 
						? R.drawable.redo 
						: R.drawable.redo_greyscale);
		}    	
    }
    
    /**
     * Sets the visibility of the tag selector.
     * @param visible The new visibility.
     */
    private void setTagSelectorVisibility(boolean visible) {
    	mPopupFrame.getLayoutParams().width = visible 
    			? (int) (getResources().getDisplayMetrics().density 
    					* POPUP_AREA_HEIGHT)
    			: 0;
    	findViewById(R.id.combatMapMainLayout).requestLayout();
    	this.mTagSelectorVisible = visible;
    }


	/**
	 * Attempts to load map data, or creates a new map if this fails.
	 */
	private void loadOrCreateMap() {
		if (MapData.hasValidInstance()) {
			mData = MapData.getInstance();
			mCombatView.setData(mData);
		} else {
	        loadMap(DataManager.TEMP_MAP_NAME);		
		}
		setUndoRedoEnabled();

	}
    

    @Override
    public void onResume() {
        super.onResume();
        loadOrCreateMap();
        
        reloadPreferences();
        
        mCombatView.refreshMap();
        new TokenDatabaseLoadTask().execute();
        // android.os.Debug.stopMethodTracing();
    }


	/**
     * Modifies the current map data according to any preferences the user has
     * set.
     */
    private void reloadPreferences() {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());

        this.mTokenSelector.setShouldDrawDark(mData.getGrid().isDark());

        if (mTabManager != null) {
	        mTabManager.pickTab(sharedPreferences.getInt(
	        		"manipulation_mode", MODE_DRAW_BACKGROUND));
        }
        
        // We defer loading the manipulation mode until now, so that the correct
        // item is disabled after the menu is loaded.
        loadModePreference();

    }
    
    /**
     * Loads the snap preference associated with the current combat map mode.
     */
    private void loadModeSpecificSnapPreference() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                        this.getApplicationContext());
    	
    	int manipulationMode = sharedPreferences.getInt(
    			"manipulation_mode", MODE_TOKENS);
    	
        boolean shouldSnap = sharedPreferences.getBoolean(
        		getModeSpecificSnapPreferenceName(manipulationMode), true);
        
    	mCombatView.setShouldSnapToGrid(shouldSnap);
    	mCombatView.setTokensSnapToIntersections(
    			sharedPreferences.getBoolean("tokenssnaptogridlines", false));
    	
    	if (mSnapToGridMenuItem != null) {
    		mSnapToGridMenuItem.setChecked(shouldSnap);
    	}
    }
    
    /**
     * Sets the snap preference associated with the current combat map mode.
     * @param shouldSnap True if should snap, false otherwise.
     */
    private void setModeSpecificSnapPreference(final boolean shouldSnap) {
    	SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                        this.getApplicationContext());
    	
    	int manipulationMode = sharedPreferences.getInt(
    			"manipulation_mode", MODE_TOKENS);
    	
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(
        		getModeSpecificSnapPreferenceName(manipulationMode),
        		mSnapToGridMenuItem.isChecked());
        editor.commit();
        
    	mCombatView.setShouldSnapToGrid(shouldSnap);
    }
    
    /**
     * Given a combat mode, returns the snap to grid preference name associated
     * with that combat mode.
     * @param mode The combat mode to check.
     * @return Name of the snap preference associated with that combat mode.
     */
    private String getModeSpecificSnapPreferenceName(final int mode) {
    	return mode == MODE_TOKENS ? "snaptokens" : "snapdrawing";
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());

        Editor editor = sharedPreferences.edit();
        editor.commit();
        String filename = sharedPreferences.getString("filename", null);
        if (filename == null 
        		|| !sharedPreferences.getBoolean("autosave", true)) {
        	filename = DataManager.TEMP_MAP_NAME;
        }

        new MapSaver(filename, this.getApplicationContext()).run();
    }

    /**
     * Loads the map with the given name (no extension), and replaces the
     * currently loaded map with it.
     * @param name Name of the map to load.
     */
    public void loadMap(final String name) {
        try {
            new DataManager(getApplicationContext()).loadMapName(name);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "Could not load file.  Reason: " + e.toString(),
                    Toast.LENGTH_LONG);
            toast.show();

            MapData.clear();
            setFilenamePreference(null);
        }
        mData = MapData.getInstance();
        mCombatView.setData(mData);
    }

    /**
     * Sets the preference that will persist the name of the active file
     * between sessions.
     * @param newFilename The filename to set.
     */
    private void setFilenamePreference(final String newFilename) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());
        // Persist the filename that we saved to so that we can load from that
        // file again.
        Editor editor = sharedPreferences.edit();
        editor.putString("filename", newFilename);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.combat_map_menu, menu);

        mSnapToGridMenuItem = menu.findItem(R.id.menu_snap_to_grid);
        loadModeSpecificSnapPreference();
        
        mUndoMenuItem = menu.findItem(R.id.menu_undo);
        mRedoMenuItem = menu.findItem(R.id.menu_redo);
        setUndoRedoEnabled();
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                        this.getApplicationContext());
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_clear_all:
        	// Save the current map, if autosave was requested.
        	if (sharedPreferences.getBoolean("autosave", true)) {
        		new MapSaver(
        				sharedPreferences.getString("filename", ""), 
        				this.getApplicationContext())
        		.run();
        	}
        	
        	Grid g = mData.getGrid();
        	
            MapData.clear();
            setFilenamePreference(null);
            mData = MapData.getInstance();
            // Make sure the new map data has the same grid.
            mData.setGrid(g);
            mCombatView.setData(mData);
            reloadPreferences();
            return true;
        case R.id.menu_settings:
            startActivity(new Intent(this, Settings.class));
            return true;
        case R.id.menu_resize_grid:
            mCombatView.setResizeGridMode();
            mBottomControlFrame.removeAllViews();
            return true;
        case R.id.menu_snap_to_grid:
        	mSnapToGridMenuItem.setChecked(!mSnapToGridMenuItem.isChecked());
            this.setModeSpecificSnapPreference(mSnapToGridMenuItem.isChecked());
        	return true;
        case R.id.menu_save:
            showDialog(DIALOG_ID_SAVE);
            return true;
        case R.id.menu_load:
            startActivity(new Intent(this, Load.class));
            return true;
        case R.id.menu_undo:
        	mCombatView.getUndoRedoTarget().undo();
        	mCombatView.refreshMap();
        	return true;
        case R.id.menu_redo:
        	mCombatView.getUndoRedoTarget().redo();
        	mCombatView.refreshMap();
        	return true;
        case R.id.menu_grid_properties:
        	showDialog(DIALOG_ID_GRID_PROPERTIES);
        	return true;
        case R.id.menu_export:
        	showDialog(DIALOG_ID_EXPORT);
        	return true;
        default:
        	return false;
        }
    }

    /**
     * Sets the manipulation mode to the given mode.
     * @param manipulationMode The mode to set to; should be a MODE_ constant
     * 		declared in this class.
     */
    private void setManipulationMode(final int manipulationMode) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());

        Editor editor = sharedPreferences.edit();
        editor.putInt("manipulation_mode", manipulationMode);
        editor.commit();

		switch (manipulationMode) {
		case MODE_DRAW_BACKGROUND:
			mCombatView.getMultiSelect().selectNone();
            mCombatView.setAreTokensManipulatable(false);
            mCombatView.useBackgroundLayer();
            mCombatView.setFogOfWarMode(FogOfWarMode.DRAW);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
            mDrawOptionsView.setMaskToolVisibility(true);
            mDrawOptionsView.setBackgroundImageButtonVisibility(
            		BuildConfig.DEBUG);
            setTagSelectorVisibility(false);
            loadModeSpecificSnapPreference();
			return;
		case MODE_DRAW_ANNOTATIONS:
			mCombatView.getMultiSelect().selectNone();
            mCombatView.setAreTokensManipulatable(false);
            mCombatView.useAnnotationLayer();
            mCombatView.setFogOfWarMode(FogOfWarMode.CLIP);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
            mDrawOptionsView.setMaskToolVisibility(false);
            mDrawOptionsView.setBackgroundImageButtonVisibility(false);
            setTagSelectorVisibility(false);
            loadModeSpecificSnapPreference();
			return;
		case MODE_DRAW_GM_NOTES:
			mCombatView.getMultiSelect().selectNone();
            mCombatView.setAreTokensManipulatable(false);
            mCombatView.useGmNotesLayer();
            mCombatView.setFogOfWarMode(FogOfWarMode.NOTHING);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
            mDrawOptionsView.setMaskToolVisibility(true);
            mDrawOptionsView.setBackgroundImageButtonVisibility(false);
            setTagSelectorVisibility(false);
            loadModeSpecificSnapPreference();
			return;
		case MODE_TOKENS:
            mCombatView.setAreTokensManipulatable(true);
            mCombatView.setTokenManipulationMode();
            mCombatView.setFogOfWarMode(
            		sharedPreferences.getBoolean("fogofwar", true)
            				? FogOfWarMode.CLIP
            				: FogOfWarMode.NOTHING);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(mTokenSelector);
            setModePreference(manipulationMode);
            loadModeSpecificSnapPreference();
			return;
		default:
			throw new IllegalArgumentException(
					"Invalid manipulation mode: "
					+ Integer.toString(manipulationMode));
		}
	}

    /**
     * Sets the preference that will persist the active mode between sessions.
     * @param mode The mode to set
     */
    private void setModePreference(final int mode) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());
        // Persist the filename that we saved to so that we can load from that
        // file again.
        Editor editor = sharedPreferences.edit();
        editor.putInt("manipulation_mode", mode);
        editor.commit();
    }

    /**
     * Loads the preference that controls what the current manipulation mode
     * is.
     */
	private void loadModePreference() {
		SharedPreferences sharedPreferences =
	            PreferenceManager.getDefaultSharedPreferences(
	                    this.getApplicationContext());
		// Set the current mode to the selected mode.
        setManipulationMode(
        		sharedPreferences.getInt(
        				"manipulation_mode", MODE_DRAW_BACKGROUND));
	}


    @Override
    public Dialog onCreateDialog(final int id) {
        switch(id) {
        case DIALOG_ID_SAVE:
             return new TextPromptDialog(this,
                     new TextPromptDialog.OnTextConfirmedListener() {
                public void onTextConfirmed(final String text) {
           		 	SharedPreferences sharedPreferences =
           		 			PreferenceManager.getDefaultSharedPreferences(
           		 					getApplicationContext());
           		    
           		 	// If the save file name exists and is not the current file,
           		 	// warn about overwriting.
           		    if (!text.equals(
           		    			sharedPreferences.getString("filename", "")) 
           		    		&& new DataManager(getApplicationContext())
           		    				.saveFileExists(text)) {
           		    	mAttemptedMapName = text;
           		    	showDialog(CombatMap.DIALOG_ID_SAVE_NAME_CONFIRM);
           		    } else {
	                	setFilenamePreference(text);
	                	new MapSaver(text, getApplicationContext()).run();
           		    }
                }
            }, getString(R.string.save_map), getString(R.string.save));
        case DIALOG_ID_DRAW_TEXT:
        	
            FontDialog d = new FontDialog(this,
                    new FontDialog.OnTextConfirmedListener() {
               public void onTextConfirmed(
            		   final String text, final float size) {
            	   if (mEditedTextObject == null) {
               			mCombatView.createNewText(
               					mNewTextLocationWorldSpace, text, size);
            	   } else {
            		   mCombatView.getActiveLines().editText(
            				   mEditedTextObject, text, size, 
            				   mCombatView.getWorldSpaceTransformer());
            		   mCombatView.refreshMap();
            	   }
               }
           });  

            return d;
        case DIALOG_ID_SAVE_NAME_CONFIRM:
        	return new AlertDialog.Builder(CombatMap.this)
        		.setMessage("Map already exists.  Save over it?")
        		.setCancelable(false)
        		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int id) {
	                	setFilenamePreference(mAttemptedMapName);
	                	new MapSaver(mAttemptedMapName, getApplicationContext()).run();
        			}
        		})
        		.setNegativeButton("No", new DialogInterface.OnClickListener() {
        			public void onClick(DialogInterface dialog, int id) {
        				mAttemptedMapName = null;
        			}
        		})
        		.create();
        case DIALOG_ID_GRID_PROPERTIES:
        	GridPropertiesDialog gpd = new GridPropertiesDialog(this);
        	gpd.setOnPropertiesChangedListener(new GridPropertiesDialog.PropertiesChangedListener() {
				
				@Override
				public void onPropertiesChanged() {
					mCombatView.refreshMap();
				}
			});
        	return gpd;
        case DIALOG_ID_EXPORT:
        	return new ExportImageDialog(this);
        default:
        	return null;
        }
    }
    
    @Override
    protected void onPrepareDialog(final int id, final Dialog dialog) {
    	switch(id) {
    	 case DIALOG_ID_SAVE:
    		 SharedPreferences sharedPreferences =
             	PreferenceManager.getDefaultSharedPreferences(
             			this.getApplicationContext());

    		 // Attempt to load map data.  If we can't load map data, create a 
    		 // new map.
    		 String filename = sharedPreferences.getString("filename", "");
    		 if (filename == null || filename.equals(
    				 DataManager.TEMP_MAP_NAME)) {
    			 filename = "";
    		 }
    		 TextPromptDialog d = (TextPromptDialog) dialog;
    		 d.fillText(filename);
    		 break;
         case DIALOG_ID_DRAW_TEXT:
         	 FontDialog fd = (FontDialog) dialog;
             if (mEditedTextObject != null) {
             	fd.populateFields(
             			mEditedTextObject.getText(), 
             			mEditedTextObject.getTextSize());
             } else {
            	fd.clearText();
             }
             break;
         case DIALOG_ID_SAVE_NAME_CONFIRM:
        	 AlertDialog ad = (AlertDialog) dialog;
        	 ad.setMessage(
        			 "There is already a map named \"" 
        			 + mAttemptedMapName + "\".  Save over it?");
        	 break;
         case DIALOG_ID_GRID_PROPERTIES:
        	 GridPropertiesDialog gpd = (GridPropertiesDialog) dialog;
        	 gpd.setMapData(this.mData);
        	 break;
         case DIALOG_ID_EXPORT:
    		 sharedPreferences =
          		PreferenceManager.getDefaultSharedPreferences(
          			this.getApplicationContext());

    		 // Attempt to load map data.  If we can't load map data, create a 
    		 // new map.
    		 filename = sharedPreferences.getString("filename", "");
    		 if (filename == null || filename.equals(
 				 DataManager.TEMP_MAP_NAME)) {
    			 filename = "";
    		 }
         	ExportImageDialog ed = (ExportImageDialog) dialog;
         	ed.prepare(filename, mData, mCombatView.getWidth(), 
         			mCombatView.getHeight());
         default:
        	 super.onPrepareDialog(id, dialog);
         }
    }

    /**
     * This helper class allows a map to be saved asynchronously.
     * @author Tim Bocek
     *
     */
    private class MapSaver implements Runnable {
    	/**
    	 * Filename to save to.
    	 */
        private String mFilename;

        /**
         * Context to use while saving.
         */
        private Context mContext;

        /**
         * Constructor.
         * @param filename Filename to save to.
         * @param context Context to use while saving.
         */
        public MapSaver(final String filename, final Context context) {
            this.mFilename = filename;
            this.mContext = context;
        }

        @Override
        public void run() {
            try {
                DataManager dm = new DataManager(mContext);
	            dm.saveMapName(mFilename);
	            // Only save preview if not saving to temp file.
		        if (mFilename != DataManager.TEMP_MAP_NAME) {
		            Bitmap preview = mCombatView.getPreview();
		            if (preview != null) {
		            	dm.savePreviewImage(mFilename, preview);
		            }
		        }
            } catch (Exception e) {
                MapData.clear();
                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(mContext);

                // Persist the filename that we saved to so that we can load
                // from that file again.
                Editor editor = sharedPreferences.edit();
                editor.putString("filename", null);
                editor.commit();

                // Log the error in a toast
                e.printStackTrace();
                Toast toast = Toast.makeText(mContext,
                        "Could not save file.  Reason: " + e.toString(),
                        Toast.LENGTH_LONG);
                toast.show();

                if (DeveloperMode.DEVELOPER_MODE) {
                	throw new RuntimeException(e);
                }

            }
        }
    }
    
    /**
     * Callback defining an action mode for selecting multiple tokens.
     * @author Tim
     *
     */
    private class TokenSelectionActionModeCallback 
    		implements ActionMode.Callback {

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// Get a *list* of the selected tokens.
			List<BaseToken> tokens = new ArrayList<BaseToken>(
					mCombatView.getMultiSelect().getSelectedTokens());
			
			switch (item.getItemId()) {
			case R.id.token_action_mode_bloodied:
				item.setChecked(!item.isChecked());
				mData.getTokens().checkpointTokens(tokens);
				for (BaseToken t: tokens) {
					t.setBloodied(item.isChecked());
				}
				mData.getTokens().createCommandHistory();
				break;
			case R.id.token_action_mode_border_color_none:
				item.setChecked(true);
				mData.getTokens().checkpointTokens(tokens);
				for (BaseToken t: tokens) {
					t.clearCustomBorderColor();
				}
				mData.getTokens().createCommandHistory();
				break;
			case R.id.token_action_mode_border_color_white:
				item.setChecked(true);
				setTokenBorderColor(tokens, Color.WHITE);
				break;
			case R.id.token_action_mode_border_color_blue:
				item.setChecked(true);
				setTokenBorderColor(tokens, Color.BLUE);
				break;
			case R.id.token_action_mode_border_color_black:
				item.setChecked(true);
				setTokenBorderColor(tokens, Color.BLACK);
				break;
			case R.id.token_action_mode_border_color_red:
				item.setChecked(true);
				setTokenBorderColor(tokens, Color.RED);
				break;
			case R.id.token_action_mode_border_color_green:
				item.setChecked(true);
				setTokenBorderColor(tokens, Color.GREEN);
				break;
			case R.id.token_action_mode_border_color_yellow:
				item.setChecked(true);
				setTokenBorderColor(tokens, Color.YELLOW);
				break;
			case R.id.token_action_mode_size_tenth:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 0.1f);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_quarter:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 0.25f);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_half:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 0.5f);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_1:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 1);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_2:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 2);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_3:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 3);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_4:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 4);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_5:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 5);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_size_6:
				//CHECKSTYLE:OFF
				item.setChecked(true);
				setTokenSize(tokens, 6);
				//CHECKSTYLE:ON
				break;
			case R.id.token_action_mode_delete:
				mData.getTokens().removeAll(tokens);
				// We just deleted all the tokens, select none.
				mCombatView.getMultiSelect().selectNone();
			default:
				break;
			}
			mCombatView.refreshMap();
			return true;
		}
		
		/**
		 * Sets the size of all tokens in the given list, properly checkpointing
		 * for undo/redo.
		 * @param tokens The list of tokens to change.
		 * @param size The new token size.
		 */
		private void setTokenSize(List<BaseToken> tokens, float size) {
			mData.getTokens().checkpointTokens(tokens);
			for (BaseToken t: tokens) {
				t.setSize(size);
			}
			mData.getTokens().createCommandHistory();
		}
		
		/**
		 * Sets the border color for all tokens in the given list, properly
		 * checkpointing for undo/redo.
		 * @param tokens The list of tokens to change.
		 * @param color Color of the border to apply.
		 */
		private void setTokenBorderColor(List<BaseToken> tokens, int color) {
			mData.getTokens().checkpointTokens(tokens);
			for (BaseToken t: tokens) {
				t.setCustomBorder(color);
			}
			mData.getTokens().createCommandHistory();
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.token_action_mode_menu, menu);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mCombatView.getMultiSelect().selectNone();
			mCombatView.refreshMap();
			// Return to token manipulation mode.
            mCombatView.setTokenManipulationMode();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			mCombatView.setMultiTokenMode();
			return true;
		}
    }
    
    /**
     * Listener for actions to take when the multi-token select managed by this
     * activity's main view changes.
     * @author Tim
     *
     */
    private class SelectionChangedListener 
    		implements MultiSelectManager.SelectionChangedListener {
		
		@Override
		public void selectionStarted() {
			// TODO Auto-generated method stub
			mMultiSelectActionMode = startActionMode(
					new TokenSelectionActionModeCallback());
		}
		
		@Override
		public void selectionEnded() {
			if (mMultiSelectActionMode != null) {
				ActionMode m = mMultiSelectActionMode;
				mMultiSelectActionMode = null;
				m.finish();
			}	
		}
		
		@Override
		public void selectionChanged() {
			Collection<BaseToken> selected = mCombatView.getMultiSelect()
					.getSelectedTokens();
			BaseToken[] selectedArr = selected.toArray(new BaseToken[0]);
			int numTokens = selected.size();
			if (mMultiSelectActionMode != null && selected.size() > 0) {
				Menu m = mMultiSelectActionMode.getMenu();
				mMultiSelectActionMode.setTitle(
						Integer.toString(numTokens) 
						+ (numTokens == 1 ? " Token " : " Tokens ")
						+ "Selected.");
			
			
				// Modify the currently checked menu items based on the property
				// of the tokens selected.
				m.findItem(R.id.token_action_mode_bloodied).setChecked(BaseToken.allBloodied(selected));
			
				// Modify the currently checked border color.
				if (BaseToken.areTokenBordersSame(selected)) {
					if (selectedArr[0].hasCustomBorder()) {
						switch(selectedArr[0].getCustomBorderColor()) {
						case Color.WHITE:
							m.findItem(R.id.token_action_mode_border_color_white).setChecked(true);
							break;
						case Color.BLUE:
							m.findItem(R.id.token_action_mode_border_color_blue).setChecked(true);
							break;
						case Color.BLACK:
							m.findItem(R.id.token_action_mode_border_color_black).setChecked(true);
							break;
						case Color.RED:
							m.findItem(R.id.token_action_mode_border_color_red).setChecked(true);
							break;
						case Color.GREEN:
							m.findItem(R.id.token_action_mode_border_color_green).setChecked(true);
							break;
						case Color.YELLOW:
							m.findItem(R.id.token_action_mode_border_color_yellow).setChecked(true);
							break;
						default:
							break;
						}
						
					} else {
						m.findItem(R.id.token_action_mode_border_color_none).setChecked(true);
					}
				}
				
				if (BaseToken.areTokenSizesSame(selected)) {
					float size = selectedArr[0].getSize();
					// CHECKSTYLE:OFF
					if (Math.abs(size - .1) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_tenth)
							.setChecked(true);
					} else if (Math.abs(size - .25) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_quarter)
							.setChecked(true);
					} else if (Math.abs(size - .5) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_half)
							.setChecked(true);
					} else if (Math.abs(size - 1) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_1)
							.setChecked(true);
					} else if (Math.abs(size - 2) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_2)
							.setChecked(true);
					} else if (Math.abs(size - 3) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_3)
							.setChecked(true);
					} else if (Math.abs(size - 4) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_4)
							.setChecked(true);
					} else if (Math.abs(size - 5) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_5)
							.setChecked(true);
					} else if (Math.abs(size - 6) < Util.FP_COMPARE_ERROR) {
						m.findItem(R.id.token_action_mode_size_6)
							.setChecked(true);
					}
					//CHECKSTYLE:ON
				}
			}
		}
    }
    
    /**
     * Task that loads the token database off the UI thread, and populates
     * everything that needs the database when on the UI thread again.
     * @author Tim
     *
     */
    class TokenDatabaseLoadTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			TokenDatabase.getInstance(getApplicationContext());
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mTokenDatabase = TokenDatabase.getInstance(
	        		getApplicationContext());
			MapData d = MapData.getInstance();
			d.getTokens().deplaceholderize(mTokenDatabase);
			mTokenSelector.setTokenDatabase(mTokenDatabase, mCombatView);
			mTokenCategorySelector.setTagList(mTokenDatabase.getTags());
		}
			
    	
    }
}

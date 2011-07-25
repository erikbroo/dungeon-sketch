package com.tbocek.android.combatmap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.tbocek.android.combatmap.graphicscore.BaseToken;
import com.tbocek.android.combatmap.graphicscore.BuiltInImageToken;
import com.tbocek.android.combatmap.graphicscore.Grid;
import com.tbocek.android.combatmap.graphicscore.MapData;
import com.tbocek.android.combatmap.tokenmanager.TokenManager;
import com.tbocek.android.combatmap.view.CombatView;
import com.tbocek.android.combatmap.view.DrawOptionsView;
import com.tbocek.android.combatmap.view.TokenCategorySelector;
import com.tbocek.android.combatmap.view.TokenSelectorView;

/**
 * This is the main activity that allows the user to sketch a map, and place
 * and manipulate tokens.  Most of the application logic that does not relate
 * to token management occurs in this activity or one of its views.
 *
 * @author Tim Bocek
 */
public final class CombatMap extends Activity {
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
    private TokenCategorySelector mTokenCategorySelector;

    /**
     * The current map.
     */
    private static MapData mData;

    /**
     * Database of available combat tokens.
     */
    private TokenDatabase tokenDatabase;

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
            // TODO Auto-generated method stub
            mCombatView.setDrawMode();
            mCombatView.setNewLineColor(color);
        }

        @Override
        public void onChoosePanTool() {
            // TODO Auto-generated method stub
            mCombatView.setZoomPanMode();
        }

        @Override
        public void onChooseStrokeWidth(final int width) {
            mCombatView.setDrawMode();
            mCombatView.setNewLineStrokeWidth(width);

        }
    };

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BuiltInImageToken.registerResources(
                this.getApplicationContext().getResources());

        setContentView(R.layout.combat_map_layout);

        mCombatView = new CombatView(this);
        this.registerForContextMenu(mCombatView);

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
                if (mPopupFrame.getVisibility() == View.VISIBLE) {
                    mPopupFrame.setVisibility(View.INVISIBLE);
                } else {
                    mPopupFrame.setVisibility(View.VISIBLE);
                }
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

        mTokenCategorySelector = new TokenCategorySelector(this);
        mTokenCategorySelector.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        mTokenCategorySelector.setOnCheckedListChangedListener(
                new TokenCategorySelector.OnTagSelectedListener() {
            @Override
            public void onTagSelected(final String selectedTag) {
                Debug.startMethodTracing("setSelectedTags");

                mTokenSelector.setSelectedTag(selectedTag);
                Debug.stopMethodTracing();
            }
        });

        mPopupFrame.addView(mTokenCategorySelector);

        mainContentFrame.addView(mCombatView);
        mBottomControlFrame.addView(mTokenSelector);

        mCombatView.setTokenManipulationMode();
        mCombatView.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                this.getApplicationContext());

        // Attempt to load map data.  If we can't load map data, create a new
        // map.
        String filename = sharedPreferences.getString("filename", null);
        if (filename == null) {
            MapData.clear();
            mData = MapData.getInstance();
            mCombatView.setData(mData);
        } else {
            loadMap(filename);
        }


        reloadPreferences();

        mCombatView.invalidate();

        tokenDatabase = TokenDatabase.getInstance(this);
        mTokenCategorySelector.setTokenDatabase(tokenDatabase);
        mTokenSelector.setTokenDatabase(tokenDatabase);
    }


	/**
     * Modifies the current map data according to any preferences the user has
     * set.
     */
    private void reloadPreferences() {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());
        String colorScheme = sharedPreferences.getString("theme", "graphpaper");
        String gridType = sharedPreferences.getString("gridtype", "rect");
        mCombatView.setShouldSnapToGrid(
        		sharedPreferences.getBoolean("snaptogrid", true));
        mData.setGrid(Grid.createGrid(
                gridType, colorScheme,
                mData.getGrid().gridSpaceToWorldSpaceTransformer()));
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());

        String filename = sharedPreferences.getString("filename", null);
        if (filename == null) {
            setFilenamePreference("tmp");
            filename = "tmp";
        }

        Thread saveThread = new Thread(
                new MapSaver(filename, this.getApplicationContext()));
        saveThread.setPriority(1);
        saveThread.start();
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

    /**
     * The menu item that will cause the background to be actively drawn.
     * This is cached here so it can be disabled when this is the active menu
     * item.
     */
    private MenuItem backgroundLayerItem;

    /**
     * The menu item that will cause the annotations to be actively drawn.
     * This is cached here so it can be disabled when this is the active menu
     * item.
     */
    private MenuItem annotationLayerItem;

    /**
     * The menu item that will cause the combat tokens to be manipulated.
     * This is cached here so it can be disabled when this is the active menu
     * item.
     */
    private MenuItem combatItem;

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.combat_map_menu, menu);
        backgroundLayerItem = menu.findItem(R.id.edit_background);
        annotationLayerItem = menu.findItem(R.id.edit_annotations);
        combatItem = menu.findItem(R.id.combat_on);

        // We defer loading the manipulation mode until now, so that the correct
        // item is disabled after the menu is loaded.
        loadModePreference();
        return true;
    }

    /**
     * If the selected menu item is a drawing mode, disable it.
     * @param modeItem The menu item to check.
     */
    private void disableCurrentMode(final MenuItem modeItem) {
    	if (modeItem == null) {
    		return;
    	}

        backgroundLayerItem.setEnabled(modeItem != backgroundLayerItem);
        annotationLayerItem.setEnabled(modeItem != annotationLayerItem);
        combatItem.setEnabled(modeItem != combatItem);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.edit_background:
        	setManipulationMode(MODE_DRAW_BACKGROUND);
            return true;
        case R.id.edit_annotations:
        	setManipulationMode(MODE_DRAW_ANNOTATIONS);
            return true;
        case R.id.combat_on:
        	setManipulationMode(MODE_TOKENS);
            return true;
        case R.id.zoom_to_fit:
            mData.zoomToFit(mCombatView.getWidth(), mCombatView.getHeight());
            return true;
        case R.id.clear_all:
            MapData.clear();
            setFilenamePreference(null);
            mData = MapData.getInstance();
            reloadPreferences();
            mCombatView.setData(mData);
            return true;
        case R.id.settings:
            startActivity(new Intent(this, Settings.class));
            return true;
        case R.id.resize_grid:
            mCombatView.setResizeGridMode();
            mBottomControlFrame.removeAllViews();
            disableCurrentMode(item);
            return true;
        case R.id.save:
            showDialog(DIALOG_ID_SAVE);
            return true;
        case R.id.load:
            startActivity(new Intent(this, Load.class));
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
		switch (manipulationMode) {
		case MODE_DRAW_BACKGROUND:
            mCombatView.setDrawMode();
            mCombatView.useBackgroundLayer();
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            disableCurrentMode(this.backgroundLayerItem);
            mPopupFrame.setVisibility(View.INVISIBLE);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
			return;
		case MODE_DRAW_ANNOTATIONS:
            mCombatView.setDrawMode();
            mCombatView.useAnnotationLayer();
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            disableCurrentMode(this.annotationLayerItem);
            mPopupFrame.setVisibility(View.INVISIBLE);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
			return;
		case MODE_TOKENS:
            mCombatView.setTokenManipulationMode();
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(mTokenSelector);
            disableCurrentMode(this.combatItem);
            setModePreference(manipulationMode);
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

    /**
     * Dialog ID to use for the save file dialog.
     */
    private static final int DIALOG_ID_SAVE = 0;


    @Override
    public Dialog onCreateDialog(final int id) {
        switch(id) {
        case DIALOG_ID_SAVE:
             return new TextPromptDialog(this,
                     new TextPromptDialog.OnTextConfirmedListener() {
                public void onTextConfirmed(final String text) {
                	new MapSaver(text, getApplicationContext(), false).run();
                }
            }, "Save Map", "Save");
        default:
        	return null;
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        return mCombatView.onContextItemSelected(item);
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
         * Whether this process is running as a thread or not.
         */
        private boolean mRunAsThread;

        /**
         * Amount to sleep before actually saving.  When running as a thread,
         * this stops the thread from blocking other, more important threads.
         */
        private static final int SLEEP_TIME = 1000;

        /**
         * Constructor.
         * @param filename Filename to save to.
         * @param context Context to use while saving.
         */
        public MapSaver(final String filename, final Context context) {
            this.mFilename = filename;
            this.mContext = context;
        }


        /**
         * Constructor.
         * @param filename Filename to save to.
         * @param context Context to use while saving.
         * @param runAsThread Whether this process is running as a seperate
         * 		thread.
         */
        public MapSaver(
        		final String filename, final Context context,
        		final boolean runAsThread) {
            this(filename, context);
            this.mRunAsThread = runAsThread;
        }

        @Override
        public void run() {
            // Force a sleep to allow the UI to remain responsive
            if (this.mRunAsThread) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                DataManager dm = new DataManager(mContext);
                dm.saveMapName(mFilename);
                dm.savePreviewImage(mFilename, mCombatView.getPreview());
            } catch (Exception e) {
                MapData.clear();
                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(mContext);
                // Persist the filename that we saved to so that we can load
                // from that file again.
                Editor editor = sharedPreferences.edit();
                editor.putString("filename", null);
                editor.commit();
                // TODO: open a toast
            }
        }
    }
}
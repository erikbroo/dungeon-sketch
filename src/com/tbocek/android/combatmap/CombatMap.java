package com.tbocek.android.combatmap;

import java.util.Collection;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tbocek.android.combatmap.model.Grid;
import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.primitives.BaseToken;
import com.tbocek.android.combatmap.model.primitives.BuiltInImageToken;
import com.tbocek.android.combatmap.model.primitives.PointF;
import com.tbocek.android.combatmap.model.primitives.Text;
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
        	mCombatView.setDrawMode();
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
				mTokenSelector.setSelectedTag(newTag);
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

    @Override
    public void onCreate(final Bundle savedInstanceState) {
    	DeveloperMode.strictMode();

        super.onCreate(savedInstanceState);

        BuiltInImageToken.registerResources(
                this.getApplicationContext().getResources());



        // Set up the tabs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setContentView(R.layout.combat_map_layout);
        	ActionBar actionBar = getActionBar();
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
                if (mPopupFrame.getVisibility() == View.VISIBLE) {
                    mPopupFrame.setVisibility(View.GONE);
                } else {
                    mPopupFrame.setVisibility(View.VISIBLE);
                    mPopupFrame.performClick();
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

        // Attempt to load map data.  If we can't load map data, create a new
        // map.
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());
        String filename = sharedPreferences.getString("filename", null);
        if (filename == null) {
            MapData.clear();
            mData = MapData.getInstance();
            mCombatView.setData(mData);
        } else {
            loadMap(filename);
        }

        if (mTabManager != null) {
        	mTabManager.addTab(getString(R.string.background), MODE_DRAW_BACKGROUND);
            mTabManager.addTab(getString(R.string.gm_notes), MODE_DRAW_GM_NOTES);
            mTabManager.addTab(getString(R.string.combat), MODE_TOKENS);
            mTabManager.addTab(getString(R.string.annotations), MODE_DRAW_ANNOTATIONS);
            mTabManager.setTabSelectedListener(mTabSelectedListener);
        }

        mCombatView.refreshMap();
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

        mCombatView.refreshMap();

        mTokenDatabase = TokenDatabase.getInstance(
        		this.getApplicationContext());
        mTokenSelector.setTokenDatabase(mTokenDatabase);
        mTokenCategorySelector.setTagList(mTokenDatabase.getTags());

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

        this.mTokenSelector.setShouldDrawDark(mData.getGrid().isDark());

        if (mTabManager != null) {
	        mTabManager.pickTab(sharedPreferences.getInt(
	        		"manipulationmode", MODE_DRAW_BACKGROUND));
        }
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
        if (filename == null) {
            setFilenamePreference("tmp");
            filename = "tmp";
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
            setFilenamePreference(name);
        } catch (Exception e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(this.getApplicationContext(),
                    "Could not load file.  Reason: " + e.toString(),
                    Toast.LENGTH_LONG);
            toast.show();

            MapData.clear();
            setFilenamePreference(null);
            
            if (DeveloperMode.DEVELOPER_MODE) { 
            	throw new RuntimeException(e);
            }
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.combat_map_menu, menu);

        // We defer loading the manipulation mode until now, so that the correct
        // item is disabled after the menu is loaded.
        loadModePreference();

        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());

        mSnapToGridMenuItem = menu.findItem(R.id.snap_to_grid);
        mSnapToGridMenuItem.setChecked(
        		sharedPreferences.getBoolean("snaptogrid", true));
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
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
            return true;
        case R.id.snap_to_grid:
        	mSnapToGridMenuItem.setChecked(!mSnapToGridMenuItem.isChecked());
            SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                        this.getApplicationContext());
            // Persist the filename that we saved to so that we can load from 
            // that file again.
            Editor editor = sharedPreferences.edit();
            editor.putBoolean("snaptogrid", mSnapToGridMenuItem.isChecked());
            editor.commit();
            mCombatView.setShouldSnapToGrid(mSnapToGridMenuItem.isChecked());
        	return true;
        case R.id.save:
            showDialog(DIALOG_ID_SAVE);
            return true;
        case R.id.load:
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
        editor.putInt("manipulationmode", manipulationMode);
        editor.commit();

		switch (manipulationMode) {
		case MODE_DRAW_BACKGROUND:
            mCombatView.setAreTokensManipulatable(false);
            mCombatView.useBackgroundLayer();
            mCombatView.setFogOfWarMode(CombatView.FogOfWarMode.DRAW);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            mPopupFrame.setVisibility(View.INVISIBLE);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
            mDrawOptionsView.setMaskToolVisibility(true);
			return;
		case MODE_DRAW_ANNOTATIONS:
            mCombatView.setAreTokensManipulatable(false);
            mCombatView.useAnnotationLayer();
            mCombatView.setFogOfWarMode(CombatView.FogOfWarMode.CLIP);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            mPopupFrame.setVisibility(View.INVISIBLE);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
            mDrawOptionsView.setMaskToolVisibility(false);
			return;
		case MODE_DRAW_GM_NOTES:
            mCombatView.setAreTokensManipulatable(false);
            mCombatView.useGmNotesLayer();
            mCombatView.setFogOfWarMode(CombatView.FogOfWarMode.NOTHING);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            mPopupFrame.setVisibility(View.INVISIBLE);
            setModePreference(manipulationMode);
            mDrawOptionsView.setDefault();
            mDrawOptionsView.setMaskToolVisibility(true);
			return;
		case MODE_TOKENS:
            mCombatView.setAreTokensManipulatable(true);
            mCombatView.setTokenManipulationMode();
            mCombatView.setFogOfWarMode(
            		sharedPreferences.getBoolean("fogofwar", true)
            				? CombatView.FogOfWarMode.CLIP
            				: CombatView.FogOfWarMode.NOTHING);
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(mTokenSelector);
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



    @Override
    public Dialog onCreateDialog(final int id) {
        switch(id) {
        case DIALOG_ID_SAVE:
             return new TextPromptDialog(this,
                     new TextPromptDialog.OnTextConfirmedListener() {
                public void onTextConfirmed(final String text) {
                	setFilenamePreference(text);
                	new MapSaver(text, getApplicationContext()).run();
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
    		 if (filename.equals("tmp")) {
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
         default:
        	 super.onPrepareDialog(id, dialog);
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
	            Bitmap preview = mCombatView.getPreview();
	            if (preview != null) {
	            	dm.savePreviewImage(mFilename, preview);
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
}

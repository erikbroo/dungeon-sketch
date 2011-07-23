package com.tbocek.android.combatmap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.util.Log;
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
 */
public final class CombatMap extends Activity {


    /**
     * Tag for debug messages.
     */
    private static final String TAG = "CombatMap";

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

    private TokenSelectorView.OnTokenSelectedListener mOnTokenSelectedListener =
        new TokenSelectorView.OnTokenSelectedListener() {
        @Override
        public void onTokenSelected(BaseToken t) {
            mCombatView.placeToken(t);
        }
    };

    private DrawOptionsView.OnChangeDrawToolListener mOnChangeDrawToolListener =
        new DrawOptionsView.OnChangeDrawToolListener() {

        @Override
        public void onChooseEraser() {
            mCombatView.setEraseMode();
        }

        @Override
        public void onChooseColoredPen(int color) {
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
        public void onChooseStrokeWidth(int width) {
            mCombatView.setDrawMode();
            mCombatView.setNewLineStrokeWidth(width);

        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BuiltInImageToken.registerResources(
                this.getApplicationContext().getResources());

        setContentView(R.layout.combat_map_layout);

        mCombatView = new CombatView(this);
        this.registerForContextMenu(mCombatView);

        mTokenSelector = new TokenSelectorView(this.getApplicationContext());
        mTokenSelector.setOnTokenSelectedListener(mOnTokenSelectedListener);
        mTokenSelector.setOnClickTokenManagerListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Debug.startMethodTracing("tokenmanager");
                startActivity(new Intent(CombatMap.this, TokenManager.class));
            }
        });

        mTokenSelector.setOnClickGroupSelectorListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
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
            public void onTagSelected(String checkedTag) {
                Debug.startMethodTracing("setSelectedTags");

                //TODO(tbocek): Refactor so this conversion isn't needed.
                List<String> checkedTags = new ArrayList<String>(1);
                checkedTags.add(checkedTag);

                mTokenSelector.setSelectedTags(checkedTags);
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
        mCombatView.setShouldSnapToGrid(sharedPreferences.getBoolean("snaptogrid", true));
        mData.grid = Grid.createGrid(
                gridType, colorScheme,
                mData.grid.gridSpaceToWorldSpaceTransformer());
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

    private class MapSaver implements Runnable {
        private String filename;
        private Context context;
        private boolean runAsThread;

        public MapSaver(String filename, Context context) {
            this.filename = filename;
            this.context = context;
        }

        public MapSaver(String filename, Context context, boolean runAsThread) {
            this(filename, context);
            this.runAsThread = runAsThread;
        }

        @Override
        public void run() {
            // Force a sleep to allow the UI to remain responsive
            if (this.runAsThread) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            try {
                DataManager dm = new DataManager(context);
                dm.saveMapName(filename);
                dm.savePreviewImage(filename, mCombatView.getPreview());
            } catch (Exception e) {
                MapData.clear();
                SharedPreferences sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(context);
                // Persist the filename that we saved to so that we can load
                // from that file again.
                Editor editor = sharedPreferences.edit();
                editor.putString("filename", null);
                editor.commit();
                // TODO: open a toast
            }
        }
    }

    public void loadMap(String name) {
        try {
            new DataManager(getApplicationContext()).loadMapName(name);
        } catch (Exception e) {
            reportIOException(e, "load");
            MapData.clear();
            setFilenamePreference(null);
        }
        mData = MapData.getInstance();
        mCombatView.setData(mData);
    }

    private void reportIOException(Exception e, String attemptedAction) {
        Log.e(TAG, "Could not " + attemptedAction + " file.  Reason:");
        Log.e(TAG, e.toString());
        e.printStackTrace();
        Toast toast = Toast.makeText(this.getApplicationContext(),
                "Could not " + attemptedAction + " file.  Reason:"
                + e.toString(),
                Toast.LENGTH_LONG);
        toast.show();
    }

    private void setFilenamePreference(String newFilename) {
        SharedPreferences sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(
                    this.getApplicationContext());
        // Persist the filename that we saved to so that we can load from that
        // file again.
        Editor editor = sharedPreferences.edit();
        editor.putString("filename", newFilename);
        editor.commit();
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
        disableCurrentMode(combatItem); // Starts out in combat mode.
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
            mPopupFrame.setVisibility(View.INVISIBLE);
            return true;
        case R.id.edit_annotations:
            mCombatView.setDrawMode();
            mCombatView.useAnnotationLayer();
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(this.mDrawOptionsView);
            disableCurrentMode(item);
            mPopupFrame.setVisibility(View.INVISIBLE);
            return true;
        case R.id.combat_on:
            mCombatView.setTokenManipulationMode();
            mBottomControlFrame.removeAllViews();
            mBottomControlFrame.addView(mTokenSelector);
            disableCurrentMode(item);
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
        }

        return false;
    }

    private static final int DIALOG_ID_SAVE = 0;


    @Override
    public Dialog onCreateDialog(int id) {
        switch(id) {
        case DIALOG_ID_SAVE:
             return new TextPromptDialog(this,
                     new TextPromptDialog.OnTextConfirmedListener() {
                public void onTextConfirmed(String text) {
                	new MapSaver(text, getApplicationContext(), false).run();
                }
            }, "Save Map", "Save");
        }
        return null;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return mCombatView.onContextItemSelected(item);
    }
}
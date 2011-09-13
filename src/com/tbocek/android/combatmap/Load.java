package com.tbocek.android.combatmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.tbocek.android.combatmap.view.SaveFileButton;

/**
 * This activity allows the user to select a new file to load.
 * @author Tim Bocek
 *
 */
public final class Load extends Activity {
    /**
     * List of save files available.
     */
    private List<String> mSavedFiles;

    /**
     * Data manager to facilitate save file enumeration and loading.
     */
    private DataManager dataMgr;

    /**
     * Width of a file button.
     */
    private static final int FILE_VIEW_WIDTH = 200;

    /**
     * Height of a file button.
     */
    private static final int FILE_VIEW_HEIGHT = 200;

    /**
     * Padding on each file button.
     */
    private static final int FILE_VIEW_PADDING = 16;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataMgr = new DataManager(this.getApplicationContext());

        setup();
    }

    /**
     * Loads a list of files and sets up and lays out views to represent all the
     * files.
     */
    private void setup() {
        mSavedFiles = dataMgr.savedFiles();

        if (mSavedFiles.size() > 0) {
	        List<View> fileViews = new ArrayList<View>();
	        for (String saveFile : mSavedFiles) {
	            SaveFileButton b = createSaveFileButton(saveFile);
	            fileViews.add(b);
	        }

	        View layout = createLayout(fileViews);
	        ScrollView scroller = new ScrollView(this);
	        scroller.addView(layout);
	        this.setContentView(scroller);
        } else {
        	RelativeLayout root = new RelativeLayout(this);
        	this.getLayoutInflater().inflate(R.layout.no_files_layout, root);
        	this.setContentView(root);
        }
    }

    /**
     * Lays out the given save file buttons in a grid.
     * @param views The views to lay out.
     * @return A view containing the entire layout.
     */
    private View createLayout(final List<View> views) {
        TableLayout layout = new TableLayout(this);
        TableRow currentRow = null;
        int viewsPerRow =
            getWindowManager().getDefaultDisplay().getWidth()
                / (FILE_VIEW_WIDTH + 2 * FILE_VIEW_PADDING);
        int i = 0;
        for (View v : views) {
            if (i % viewsPerRow == 0) {
                currentRow = new TableRow(this);
                layout.addView(currentRow);
            }
            currentRow.addView(v);
            ++i;
        }
        return layout;
    }

    /**
     * Creates a button that represents the given save file and will load it
     * when pressed.
     * @param saveFile Name of the save file to represent with this button.
     * @return The button.
     */
    private SaveFileButton createSaveFileButton(final String saveFile) {
        SaveFileButton b = new SaveFileButton(this);
        b.setFileName(saveFile);
        try {
            b.setPreviewImage(dataMgr.loadPreviewImage(saveFile));
        } catch (IOException e) {
            //no-op
        }
        b.setPadding(
                FILE_VIEW_PADDING, FILE_VIEW_PADDING, FILE_VIEW_PADDING,
                FILE_VIEW_PADDING);
        b.setMinimumWidth(FILE_VIEW_WIDTH);
        b.setMinimumHeight(FILE_VIEW_HEIGHT);
        b.setOnClickListener(new SaveFileButtonClickListener(saveFile));
        registerForContextMenu(b);
        b.setOnCreateContextMenuListener(contextMenuListener);
        return b;
    }

    /**
     * Sets the preference that stores the current filename, so that next time
     * an activity that loads the current file opens the newly selected file
     * will be opened.
     * @param newFilename The new filename to load.
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
     * Listener that loads a file when a button representing that file is
     * clicked.
     * @author Tim Bocek
     *
     */
    public final class SaveFileButtonClickListener
            implements View.OnClickListener {
        /**
         * Filename that this listener instance will load when it fires.
         */
        private String mFilename;

        /**
         * Constructor.
         * @param filename The filename to tie to this listener.
         */
        public SaveFileButtonClickListener(final String filename) {
            this.mFilename = filename;
        }

        @Override
        public void onClick(final View v) {
            setFilenamePreference(mFilename);
            finish();
        }
    }

    /**
     * The save file button that last triggered a context menu open.  Used to
     * determine which file to delete if a delete operation is selected.
     */
    private SaveFileButton contextMenuTrigger = null;

    /**
     * Listener that creates a menu to delete the given save file.
     */
    private View.OnCreateContextMenuListener contextMenuListener =
        new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(final ContextMenu menu,
                final View view, final ContextMenuInfo menuInfo) {
            View v = view;
            while (!(v instanceof SaveFileButton)) {
                v = (View) v.getParent();
            }
            contextMenuTrigger = (SaveFileButton) v;
            getMenuInflater().inflate(R.menu.save_file_context_menu, menu);
        }
    };

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
      switch (item.getItemId()) {
      case R.id.delete_save_file:
          dataMgr.deleteSaveFile(contextMenuTrigger.getFileName());
          SharedPreferences sharedPreferences =
              PreferenceManager.getDefaultSharedPreferences(
                      this.getApplicationContext());

          // If we deleted the currently open file, set us up to create a new
          // file when we return to the main activity.
          if (contextMenuTrigger.getFileName()
                  == sharedPreferences.getString("filename", null)) {
              setFilenamePreference(null);
          }

          // Re-run the setup to remove the deleted file.
          setup();
          return true;
      default:
          return super.onContextItemSelected(item);
      }
    }
}

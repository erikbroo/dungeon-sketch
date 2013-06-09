package com.tbocek.android.combatmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;

/**
 * Dialog that allows importing data from other installed instances of Dungeon
 * Sketch.
 * @author Tim
 *
 */
public class ImportDataDialog extends RoboActivity {

    @InjectView(tag="import_alpha") RadioButton importAlpha;
    @InjectView(tag="import_legacy") RadioButton importLegacy;
    @InjectView(tag="import_current") RadioButton importCurrent;
    @InjectView(tag="check_overwrite_tokens") CheckBox overwriteTokens;
    @InjectView(tag="check_overwrite_maps") CheckBox overwriteMaps;
    @InjectView(tag="button_import") Button buttonImport;
    @InjectView(tag="spinner_import_data") ProgressBar spinner;

    Map<File, RadioButton> mImportOptions =
            new HashMap<File, RadioButton>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.import_data);

        boolean hasOption = false;
        hasOption = hasOption || addImportOption(
                importAlpha, "com.tbocek.dungeonsketchalpha");
        hasOption = hasOption || addImportOption(
                importCurrent, "com.tbocek.dungeonsketch");
        hasOption = hasOption || addImportOption(
                importCurrent, "com.tbocek.android.combatmap");

        if (!hasOption) { finish(); }

        buttonImport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                File srcDir = getSelectedSrcDir();
                spinner.setVisibility(View.VISIBLE);
                buttonImport.setEnabled(false);
                mImportFilesTask.execute(srcDir);
            }
        });
    }

    boolean addImportOption(RadioButton importOption, String packageName) {
        if (packageName == this.getPackageName()) {
            importOption.setVisibility(View.GONE);
            return false;
        } else if (!getExternalFilesDirForPackage(packageName).exists()) {
            importOption.setVisibility(View.GONE);
            return false;
        } else {
            importOption.setVisibility(View.VISIBLE);
            mImportOptions.put(getExternalFilesDirForPackage(packageName), importOption);
            return true;
        }
    }

    File getExternalFilesDirForPackage(String packageName) {
        return new File(new File(this.getExternalFilesDir(null), ".."), packageName);
    }

    File getSelectedSrcDir() {
        for (Entry<File, RadioButton> entry: this.mImportOptions.entrySet()) {
            if (entry.getValue().isChecked()) {
                return entry.getKey();
            }
        }
        return null;
    }

    private AsyncTask<File, Integer, Void> mImportFilesTask = new  AsyncTask<File, Integer, Void>() {
        int filesRead = 0;
        int totalFiles = 0;

        @Override
        protected Void doInBackground(File... srcDir) {
            try {
                if (srcDir == null) { return null; }
                CountFilesWalker walker = new CountFilesWalker();
                walker.Count(new File(srcDir[0], "tokens"));
                walker.Count(new File(srcDir[0], "maps"));
                totalFiles = walker.getCount();

                File destDir = ImportDataDialog.this.getExternalFilesDir(null);

                RecursiveCopyWalker copyWalker = new RecursiveCopyWalker();
                copyWalker.Copy(new File(srcDir[0], "tokens"),
                        new File(destDir, "tokens"),
                        ImportDataDialog.this.overwriteTokens.isChecked());
                copyWalker.Copy(new File(srcDir[0], "maps"),
                        new File(destDir, "maps"),
                        ImportDataDialog.this.overwriteMaps.isChecked());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            filesRead += progress[0];
            spinner.setMax(totalFiles);
            spinner.setProgress(filesRead);

        }

        @Override
        protected void onPostExecute(Void _) {
            ImportDataDialog.this.finish();
        }

        /**
         * DirectoryWalker that recursively copies file from the provided source
         * to the provided destination, respecting settings with regard to
         * overwriting.
         * @author Tim
         *
         */
        class RecursiveCopyWalker extends DirectoryWalker<File> {
            File mSrc;
            File mDest;
            boolean mOverwrite;
            public List<File> Copy(File src, File dest, boolean overwrite) throws IOException {
                mSrc = src;
                mDest = dest;
                mOverwrite = overwrite;
                List<File> results = new ArrayList<File>();
                this.walk(src, results);
                return results;
            }

            @Override
            protected boolean handleDirectory(
                    File directory, int depth, Collection<File> results) {
                replacePrefix(directory, mSrc, mDest).mkdirs();
                return true;
            }

            @Override
            protected void handleFile(
                    File file, int depth, Collection<File> results) {
                File destFile = replacePrefix(file, mSrc, mDest);
                if (!destFile.exists() || mOverwrite) {
                    try {
                        FileUtils.copyFile(mSrc, mDest);
                    } catch (IOException e) {
                        e.printStackTrace();
                        results.add(file);
                    }
                }
                publishProgress(1);
            }

            private File replacePrefix(File path, File oldPrefix, File newPrefix) {
                return new File(path.toString().replace(oldPrefix.toString(), newPrefix.toString()));
            }
        }

        class CountFilesWalker extends DirectoryWalker<File> {
            int count = 0;
            public void Count(File src) throws IOException {
                this.walk(src, null);
            }

            @Override
            protected void handleFile(
                    File file, int depth, Collection<File> results) {
                count += 1;
            }

            public int getCount() {
                return count;
            }
        }
    };



}

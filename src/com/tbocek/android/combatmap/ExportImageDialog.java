package com.tbocek.android.combatmap;

import java.io.IOException;

import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.MapDrawer;
import com.tbocek.android.combatmap.model.MapDrawer.FogOfWarMode;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

/**
 * Provides a dialog for the user to export an image.
 * 
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
    EditText mEditExportName;
    Button mExportButton;

    private MapData mData;
    private int mExportWidth;
    private int mExportHeight;

    private class SetBooleanPreferenceHandler implements
            CompoundButton.OnCheckedChangeListener {
        String mPreference;

        public SetBooleanPreferenceHandler(String preference) {
            mPreference = preference;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            Editor editor = sharedPreferences.edit();
            editor.putBoolean(mPreference, isChecked);
            editor.commit();
        }
    }

    /**
     * Constructor.
     * 
     * @param context
     *            Application context to use.
     */
    public ExportImageDialog(Context context) {
        super(context);
        this.setTitle("Export Image");
        this.setContentView(R.layout.export_dialog);

        mRadioExportFullMap = (RadioButton) this
                .findViewById(R.id.radio_export_full_map);
        mRadioExportCurrentView = (RadioButton) this
                .findViewById(R.id.radio_export_current_view);
        mCheckGridLines = (CheckBox) this
                .findViewById(R.id.checkbox_export_grid_lines);
        mCheckGmNotes = (CheckBox) this
                .findViewById(R.id.checkbox_export_gm_notes);
        mCheckTokens = (CheckBox) this
                .findViewById(R.id.checkbox_export_tokens);
        mCheckAnnotations = (CheckBox) this
                .findViewById(R.id.checkbox_export_annotations);
        mCheckFogOfWar = (CheckBox) this
                .findViewById(R.id.checkbox_export_fog_of_war);
        mEditExportName = (EditText) this.findViewById(R.id.edit_export_name);
        mExportButton = (Button) this.findViewById(R.id.button_export);

        associateControl(mRadioExportFullMap, "export_full_map", true);
        associateControl(mRadioExportCurrentView, "export_current_view", false);
        associateControl(mCheckGridLines, "export_grid_lines", true);
        associateControl(mCheckGmNotes, "export_gm_notes", false);
        associateControl(mCheckTokens, "export_tokens", true);
        associateControl(mCheckAnnotations, "export_annotations", false);
        associateControl(mCheckFogOfWar, "export_fog_of_war", false);

        mExportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    export();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast toast = Toast.makeText(getContext(),
                            "Could not export.  Reason: " + e.toString(),
                            Toast.LENGTH_LONG);
                    toast.show();
                }
                dismiss();
            }
        });
    }

    private void associateControl(CompoundButton b, String pref,
            boolean defaultValue) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        b.setChecked(prefs.getBoolean(pref, defaultValue));
        b.setOnCheckedChangeListener(new SetBooleanPreferenceHandler(pref));
    }

    public void prepare(String name, MapData mapData, int width, int height) {
        this.mEditExportName.setText(name);
        mData = mapData;
        mExportWidth = width;
        mExportHeight = height;
    }

    private void export() throws IOException {
        int width;
        int height;

        RectF wholeMapRect = mData.getScreenSpaceBoundingRect(30);
        if (this.mRadioExportCurrentView.isChecked()) {
            width = mExportWidth;
            height = mExportHeight;
        } else {
            width = (int) wholeMapRect.width();
            height = (int) wholeMapRect.height();
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (!mRadioExportCurrentView.isChecked()) {
            mData.getWorldSpaceTransformer().moveOrigin(-wholeMapRect.left,
                    -wholeMapRect.top);
        }

        new MapDrawer()
                .drawGridLines(this.mCheckGridLines.isChecked())
                .drawGmNotes(this.mCheckGmNotes.isChecked())
                .drawTokens(this.mCheckTokens.isChecked())
                .areTokensManipulable(true)
                .drawAnnotations(this.mCheckAnnotations.isChecked())
                .gmNotesFogOfWar(FogOfWarMode.NOTHING)
                .backgroundFogOfWar(
                        this.mCheckFogOfWar.isChecked() ? FogOfWarMode.CLIP
                                : FogOfWarMode.NOTHING).draw(canvas, mData);

        new DataManager(getContext()).exportImage(mEditExportName.getText()
                .toString(), bitmap, Bitmap.CompressFormat.PNG);

        if (!mRadioExportCurrentView.isChecked()) {
            mData.getWorldSpaceTransformer().moveOrigin(wholeMapRect.left,
                    wholeMapRect.top);
        }
    }
}

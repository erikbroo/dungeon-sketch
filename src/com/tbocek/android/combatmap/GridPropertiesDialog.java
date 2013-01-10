package com.tbocek.android.combatmap;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tbocek.android.combatmap.model.GridColorScheme;
import com.tbocek.android.combatmap.model.HexGridStrategy;
import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.RectangularGridStrategy;
import com.tbocek.android.combatmap.view.ImageToggleButton;
import com.tbocek.android.combatmap.view.ToggleButtonGroup;

public class GridPropertiesDialog extends Dialog {

    private ImageButton mBackgroundColor;

    private MapData mData;
    private ImageButton mForegroundColor;
    private ToggleButtonGroup mGridTypeToggles = new ToggleButtonGroup();
    private ImageToggleButton mHexGridButton;
    private ArrayAdapter<CharSequence> mPresetAdapter;
    private PropertiesChangedListener mPropertyListener;
    private ImageToggleButton mRectGridButton;

    private Button mThemePresetButton;

    ColorPickerDialog picker;

    public GridPropertiesDialog(Context context) {
        super(context);
        this.setTitle("Grid Properties");
        this.setContentView(R.layout.grid_background_properties);
        this.mForegroundColor =
                (ImageButton) this.findViewById(R.id.button_foreground_color);
        this.mBackgroundColor =
                (ImageButton) this.findViewById(R.id.button_background_color);
        this.mHexGridButton =
                (ImageToggleButton) this
                        .findViewById(R.id.button_toggle_hex_grid);
        this.mRectGridButton =
                (ImageToggleButton) this
                        .findViewById(R.id.button_toggle_rect_grid);
        this.mGridTypeToggles.add(this.mHexGridButton);
        this.mGridTypeToggles.add(this.mRectGridButton);

        this.mHexGridButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GridPropertiesDialog.this.hexGridButtonClick();

            }

        });
        this.mRectGridButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GridPropertiesDialog.this.rectGridButtonClick();
            }

        });
        this.mForegroundColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GridPropertiesDialog.this.foregroundColorClick();
            }

        });
        this.mBackgroundColor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                GridPropertiesDialog.this.backgroundColorClick();
            }

        });
        this.mPresetAdapter =
                new GridPropertiesDialog.MapThemeArrayAdapter(this.getContext());
        this.mThemePresetButton =
                (Button) this.findViewById(R.id.button_theme_presets);

        this.mThemePresetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GridPropertiesDialog.this.presetButtonClick();
            }
        });

    }

    void backgroundColorClick() {
        this.picker =
                new ColorPickerDialog(this.getContext(), this.mData.getGrid()
                        .getColorScheme().getBackgroundColor());
        this.picker
                .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {

                    @Override
                    public void onColorChanged(int color) {
                        GridPropertiesDialog.this.mBackgroundColor
                                .setImageBitmap(GridPropertiesDialog.this
                                        .getPreviewBitmap(color));
                        GridPropertiesDialog.this.mData
                                .getGrid()
                                .setColorScheme(
                                        new GridColorScheme(color,
                                                GridPropertiesDialog.this.mData
                                                        .getGrid()
                                                        .getColorScheme()
                                                        .getLineColor(), false));
                        GridPropertiesDialog.this.picker.dismiss();
                        GridPropertiesDialog.this.propertiesChanged();
                    }
                });
        this.picker.show();
    }

    private void colorThemeSelected(String colorTheme) {
        GridColorScheme scheme = GridColorScheme.fromNamedScheme(colorTheme);
        this.mForegroundColor.setImageBitmap(this.getPreviewBitmap(scheme
                .getLineColor()));
        this.mBackgroundColor.setImageBitmap(this.getPreviewBitmap(scheme
                .getBackgroundColor()));
        this.mData.getGrid().setColorScheme(scheme);
        this.propertiesChanged();
    }

    void foregroundColorClick() {
        this.picker =
                new ColorPickerDialog(this.getContext(), this.mData.getGrid()
                        .getColorScheme().getLineColor());
        this.picker
                .setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {

                    @Override
                    public void onColorChanged(int color) {
                        GridPropertiesDialog.this.mForegroundColor
                                .setImageBitmap(GridPropertiesDialog.this
                                        .getPreviewBitmap(color));
                        GridPropertiesDialog.this.mData.getGrid()
                                .setColorScheme(
                                        new GridColorScheme(
                                                GridPropertiesDialog.this.mData
                                                        .getGrid()
                                                        .getColorScheme()
                                                        .getBackgroundColor(),
                                                color, false));
                        GridPropertiesDialog.this.picker.dismiss();
                        GridPropertiesDialog.this.propertiesChanged();
                    }
                });
        this.picker.show();
    }

    private Bitmap getPreviewBitmap(int color) {
        float density =
                this.getContext().getResources().getDisplayMetrics().density;
        int d = (int) (density * 31); // 30dip
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c =
                        (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY
                                : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    private void hexGridButtonClick() {
        this.mGridTypeToggles.untoggle();
        this.mHexGridButton.setToggled(true);
        this.mData.getGrid().setDrawStrategy(new HexGridStrategy());
        this.propertiesChanged();
    }

    private void presetButtonClick() {

        new AlertDialog.Builder(this.getContext())
                .setTitle("Grid Theme Presets")
                .setAdapter(this.mPresetAdapter,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                GridPropertiesDialog.this
                                        .colorThemeSelected(GridPropertiesDialog.this.mPresetAdapter
                                                .getItem(which).toString());
                                dialog.dismiss();
                                GridPropertiesDialog.this.propertiesChanged();
                            }
                        }).create().show();
    }

    private void propertiesChanged() {
        if (this.mPropertyListener != null) {
            this.mPropertyListener.onPropertiesChanged();
        }
    }

    private void rectGridButtonClick() {
        this.mGridTypeToggles.untoggle();
        this.mRectGridButton.setToggled(true);
        this.mData.getGrid().setDrawStrategy(new RectangularGridStrategy());
        this.propertiesChanged();
    }

    public void setMapData(MapData data) {
        this.mBackgroundColor.setImageBitmap(this.getPreviewBitmap(data
                .getGrid().getColorScheme().getBackgroundColor()));
        this.mForegroundColor.setImageBitmap(this.getPreviewBitmap(data
                .getGrid().getColorScheme().getLineColor()));
        this.mGridTypeToggles.untoggle();
        if (data.getGrid().getDrawStrategy() instanceof HexGridStrategy) {
            this.mHexGridButton.setToggled(true);
        } else {
            this.mRectGridButton.setToggled(true);
        }
        this.mData = data;
    }

    public void setOnPropertiesChangedListener(
            PropertiesChangedListener propertiesChangedListener) {
        this.mPropertyListener = propertiesChangedListener;

    }

    /**
     * Extends ArrayAdapter to be an adapter specific to map themes. Will theme
     * the list items to preview what the map will look like with those colors.
     * 
     * @author Tim
     * 
     */
    private class MapThemeArrayAdapter extends ArrayAdapter<CharSequence> {

        public MapThemeArrayAdapter(Context context) {
            super(context, R.layout.selection_dialog_text_view, context
                    .getResources().getTextArray(R.array.themeNames));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView v =
                    (TextView) super.getView(position, convertView, parent);
            GridColorScheme scheme =
                    GridColorScheme.fromNamedScheme(v.getText().toString());
            v.setTextColor(scheme.getLineColor());
            v.setBackgroundColor(scheme.getBackgroundColor());

            // With all these different colors, changing the padding works
            // wonders!
            // TODO: DO this in the XML!!!!
            float dp16 =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                            this.getContext().getResources()
                                    .getDisplayMetrics());
            v.setPadding(0, (int) dp16, 0, (int) dp16);
            v.setTextSize(2 * dp16);

            return v;
        }

    }

    public interface PropertiesChangedListener {
        void onPropertiesChanged();
    }

}

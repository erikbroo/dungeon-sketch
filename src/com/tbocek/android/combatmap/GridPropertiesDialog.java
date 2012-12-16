package com.tbocek.android.combatmap;

import net.margaritov.preference.colorpicker.ColorPickerDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

import com.tbocek.android.combatmap.model.GridColorScheme;
import com.tbocek.android.combatmap.model.HexGridStrategy;
import com.tbocek.android.combatmap.model.MapData;
import com.tbocek.android.combatmap.model.RectangularGridStrategy;
import com.tbocek.android.combatmap.tokenmanager.TokenManager;
import com.tbocek.android.combatmap.view.ImageToggleButton;
import com.tbocek.android.combatmap.view.ToggleButtonGroup;

public class GridPropertiesDialog extends Dialog{
	
	public interface PropertiesChangedListener {
		void onPropertiesChanged();
	}
	
	private ImageButton mForegroundColor;
	private ImageButton mBackgroundColor;
	private ImageToggleButton mHexGridButton;
	private ImageToggleButton mRectGridButton;
	private Button mThemePresetButton;
	private MapData mData;
	private ArrayAdapter<CharSequence> mPresetAdapter;
	
	private PropertiesChangedListener mPropertyListener;
	
	private ToggleButtonGroup mGridTypeToggles = new ToggleButtonGroup();
	
	public GridPropertiesDialog(Context context) {
		super(context);
		this.setTitle("Grid Properties");
		this.setContentView(R.layout.grid_background_properties);
		mForegroundColor = (ImageButton) this.findViewById(R.id.button_foreground_color);
		mBackgroundColor = (ImageButton) this.findViewById(R.id.button_background_color);
		mHexGridButton = (ImageToggleButton) this.findViewById(R.id.button_toggle_hex_grid);
		mRectGridButton = (ImageToggleButton) this.findViewById(R.id.button_toggle_rect_grid);
		mGridTypeToggles.add(mHexGridButton);
		mGridTypeToggles.add(mRectGridButton);
		
		mHexGridButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				hexGridButtonClick();
				
			}

		});
		mRectGridButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				rectGridButtonClick();
			}

		});
		mForegroundColor.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				foregroundColorClick();
			}
			
		});
		mBackgroundColor.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				backgroundColorClick();
			}
			
		});
		mPresetAdapter = ArrayAdapter.createFromResource(
				this.getContext(), R.array.themeNames, 
				R.layout.selection_dialog_text_view);
		mPresetAdapter.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		mThemePresetButton = (Button) this.findViewById(
				R.id.button_theme_presets);

		mThemePresetButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				presetButtonClick();
			}
		});
		
	}
	
	private void presetButtonClick() {

		  new AlertDialog.Builder(this.getContext())
		  .setTitle("Grid Theme Presets")
		  .setAdapter(mPresetAdapter, new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	colorThemeSelected(mPresetAdapter.getItem(which).toString());
		    	dialog.dismiss();
		    	propertiesChanged();
		    }
		  }).create().show();
	}
	
	private void hexGridButtonClick() {
		mGridTypeToggles.untoggle();
		mHexGridButton.setToggled(true);
		mData.getGrid().setDrawStrategy(new HexGridStrategy());
		propertiesChanged();
	}
	
	private void rectGridButtonClick() {
		mGridTypeToggles.untoggle();
		mRectGridButton.setToggled(true);
		mData.getGrid().setDrawStrategy(new RectangularGridStrategy());
		propertiesChanged();
	}
	
	private void colorThemeSelected(String colorTheme) {
		GridColorScheme scheme = GridColorScheme.fromNamedScheme(colorTheme);
		this.mForegroundColor.setImageBitmap(getPreviewBitmap(scheme.getLineColor()));
		this.mBackgroundColor.setImageBitmap(getPreviewBitmap(scheme.getBackgroundColor()));
		this.mData.getGrid().setColorScheme(scheme);
		propertiesChanged();
	}
	
	ColorPickerDialog picker;
	
	void foregroundColorClick() {
		picker = new ColorPickerDialog(getContext(), mData.getGrid().getColorScheme().getLineColor());
		picker.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
			
			@Override
			public void onColorChanged(int color) {
				mForegroundColor.setImageBitmap(getPreviewBitmap(color));
				mData.getGrid().setColorScheme(new GridColorScheme(mData.getGrid().getColorScheme().getBackgroundColor(), color, false));
				picker.dismiss();
				propertiesChanged();
			}
		});
		picker.show();
	}
	
	void backgroundColorClick() {
		picker = new ColorPickerDialog(getContext(), mData.getGrid().getColorScheme().getBackgroundColor());
		picker.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
			
			@Override
			public void onColorChanged(int color) {
				mBackgroundColor.setImageBitmap(getPreviewBitmap(color));
				mData.getGrid().setColorScheme( new GridColorScheme(color, mData.getGrid().getColorScheme().getLineColor(), false));
				picker.dismiss();
				propertiesChanged();
			}
		});
		picker.show();
	}

	public void setMapData(MapData data) {
		mBackgroundColor.setImageBitmap(getPreviewBitmap(
				data.getGrid().getColorScheme().getBackgroundColor()));
		mForegroundColor.setImageBitmap(getPreviewBitmap(
				data.getGrid().getColorScheme().getLineColor()));
		mGridTypeToggles.untoggle();
		if (data.getGrid().getDrawStrategy() instanceof HexGridStrategy) {
			mHexGridButton.setToggled(true);
		} else {
			mRectGridButton.setToggled(true);
		}
		mData = data;
	}
	
	private Bitmap getPreviewBitmap(int color) {
		float density = getContext().getResources().getDisplayMetrics().density;
		int d = (int) (density * 31); //30dip
		Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
		int w = bm.getWidth();
		int h = bm.getHeight();
		int c = color;
		for (int i = 0; i < w; i++) {
			for (int j = i; j < h; j++) {
				c = (i <= 1 || j <= 1 || i >= w-2 || j >= h-2) ? Color.GRAY : color;
				bm.setPixel(i, j, c);
				if (i != j) {
					bm.setPixel(j, i, c);
				}
			}
		}

		return bm;
	}
	
	private void propertiesChanged() {
		if (mPropertyListener != null) {
			mPropertyListener.onPropertiesChanged();
		}
	}

	public void setOnPropertiesChangedListener(
			PropertiesChangedListener propertiesChangedListener) {
		mPropertyListener = propertiesChangedListener;
		
	}

}

package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class SaveFileButton extends LinearLayout{
	ImageView preview;
	TextView text;
	
	public SaveFileButton(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.saved_map_file, this);
		preview = (ImageView) findViewById(R.id.saved_map_preview);
		text = (TextView) findViewById(R.id.saved_map_file_name);
		preview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				performClick();
			}
		});
	}
	
	public void setFileName(String name) {
		text.setText(name);
	}
	
	public void setPreviewImage(Bitmap image) {
		preview.setImageBitmap(image);
	}
	
	public String getFileName() {
		return text.getText().toString();
	}
	
	@Override
	public void setOnCreateContextMenuListener(View.OnCreateContextMenuListener l) {
		super.setOnCreateContextMenuListener(l);
		preview.setOnCreateContextMenuListener(l);
		text.setOnCreateContextMenuListener(l);
	}
}

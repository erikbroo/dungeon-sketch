package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.Button;
import android.widget.ImageView;

public class PencilButton extends ImageView {

	int color;
	
	public PencilButton(Context context, int color) {
		super(context);
		this.color = color;
		Drawable d = context.getResources().getDrawable(R.drawable.pencilbw);
		ColorFilter cf = new PorterDuffColorFilter(color, PorterDuff.Mode.OVERLAY);
		this.setImageDrawable(d);
		this.setColorFilter(cf);
	}
}

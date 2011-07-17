package com.tbocek.android.combatmap.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.ImageButton;

public class ImageToggleButton extends ImageButton {
	public boolean toggled = false;
	public ImageToggleButton(Context context) {
		super(context);
	}
	
	public boolean isToggled() {
		return toggled;
	}
	
	public void setToggled(boolean toggled) {
		this.toggled = toggled;
		invalidate();
	}
	
	@Override
	public void onDraw(Canvas c) {
		super.onDraw(c);
		
		Paint paint = new Paint();
		paint.setColor(Color.rgb(0, 127, 255));
		paint.setStrokeWidth(3);
		if (toggled) {
			c.drawLine(0, 0, 0, getHeight(), paint);
			c.drawLine(0, 0, getWidth(), 0, paint);
			c.drawLine(getWidth(), 0, getWidth(), getHeight(), paint);
			c.drawLine(0, getHeight(), getWidth(), getHeight(), paint);
		}
	}
}

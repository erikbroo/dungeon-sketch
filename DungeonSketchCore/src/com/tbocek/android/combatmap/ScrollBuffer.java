package com.tbocek.android.combatmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;

public class ScrollBuffer {
	private Bitmap primary;
	private Bitmap secondary;
	
	public void allocateBitmaps(int width, int height) {
		// TODO: Do we need to use ARGB_8888 instead?
		primary = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		secondary = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
	}
	
	public Canvas scroll(int deltaX, int deltaY) {
		Canvas c = new Canvas(secondary);
		Rect dst = new Rect(deltaX, deltaY, c.getWidth() + deltaX, c.getHeight() + deltaY);
		c.drawBitmap(primary, null, dst, null);

		swapBuffers();
		
		Region.Op yOp = Region.Op.UNION;
		if (deltaX > 0) {
			c.clipRect(new Rect(0, 0, deltaX, c.getHeight()), Region.Op.REPLACE);
		} else if (deltaX < 0) {
			c.clipRect(new Rect(c.getWidth() + deltaX, 0, c.getWidth(), c.getHeight()), Region.Op.REPLACE);
		} else {
			// No x clip, the Y clip needs a union
			yOp = Region.Op.REPLACE;
		}
		
		if (deltaY > 0) {
			c.clipRect(new Rect(0, 0, c.getWidth(), deltaY), yOp);
		} else if (deltaY < 0) {
			c.clipRect(new Rect(0, c.getHeight() + deltaY, c.getWidth(), c.getHeight()), yOp);
		}
		
		return c;
	}
	
	public Canvas startScrolling() {
		return new Canvas(primary);
	}
	
	private void swapBuffers() {
		Bitmap tmp = primary;
		primary = secondary;
		secondary = tmp;
	}
}

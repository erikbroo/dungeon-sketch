package com.tbocek.android.combatmap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;

public class ScrollBuffer {
	private Bitmap primary;
	private Bitmap secondary;
	
	private float deltaXAccumulator = 0;
	private float deltaYAccumulator = 0;
	
	public int mLastXScroll;
	public int mLastYScroll;
	
	public void allocateBitmaps(int width, int height) {
		// TODO: Do we need to use ARGB_8888 instead?
		primary = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		secondary = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		
	}
	
	public Canvas scroll(float deltaX, float deltaY) {
		deltaXAccumulator += deltaX;
		deltaYAccumulator += deltaY;
		
		mLastXScroll = (int) deltaX;
		mLastYScroll= (int) deltaY;
		
		if (mLastXScroll == 0 && mLastYScroll == 0) {
			return null;
		}
		
		deltaXAccumulator -= mLastXScroll;
		deltaYAccumulator -= mLastYScroll;
		
		
		Canvas c = new Canvas(secondary);
		Rect dst = new Rect(mLastXScroll, mLastYScroll, c.getWidth() + mLastXScroll, c.getHeight() + mLastYScroll);
		c.drawBitmap(primary, null, dst, null);

		swapBuffers();
		
		Region.Op yOp = Region.Op.UNION;
		if (deltaX > 0) {
			c.clipRect(new Rect(0, 0, mLastXScroll, c.getHeight()), Region.Op.REPLACE);
		} else if (deltaX < 0) {
			c.clipRect(new Rect(c.getWidth() + mLastXScroll, 0, c.getWidth(), c.getHeight()), Region.Op.REPLACE);
		} else {
			// No x clip, the Y clip needs a union
			yOp = Region.Op.REPLACE;
		}
		
		if (deltaY > 0) {
			c.clipRect(new Rect(0, 0, c.getWidth(), mLastYScroll), yOp);
		} else if (deltaY < 0) {
			c.clipRect(new Rect(0, c.getHeight() + mLastYScroll, c.getWidth(), c.getHeight()), yOp);
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

	public Bitmap getActiveBuffer() {
		return primary;
	}
	
	public int lastXScroll() {
		return mLastXScroll;
	}
	
	public int lastYScroll() {
		return mLastYScroll;
	}
}

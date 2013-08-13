package com.tbocek.android.combatmap;

import java.util.List;

import com.google.common.collect.Lists;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;

public class ScrollBuffer {
	public class DrawRequest {
		public Canvas canvas;
		public List<Rect> invalidRegions = Lists.newArrayList();
		public int deltaX;
		public int deltaY;
		
	}
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
	
	public DrawRequest scroll(float deltaX, float deltaY) {
		DrawRequest req = new DrawRequest();
		deltaXAccumulator += deltaX;
		deltaYAccumulator += deltaY;
		
		mLastXScroll = (int) deltaX;
		mLastYScroll = (int) deltaY;
		
		if (mLastXScroll == 0 && mLastYScroll == 0) {
			return null;
		}
		
		deltaXAccumulator -= mLastXScroll;
		deltaYAccumulator -= mLastYScroll;
		
		req.canvas = new Canvas(secondary);
		Rect dst = new Rect(mLastXScroll, mLastYScroll, req.canvas.getWidth() + mLastXScroll, req.canvas.getHeight() + mLastYScroll);
		req.canvas.drawBitmap(primary, null, dst, null);

		swapBuffers();
		
		if (mLastXScroll > 0) {
			req.invalidRegions.add(new Rect(0, 0, mLastXScroll +1 , req.canvas.getHeight()));
		} else if (mLastXScroll < 0) {
			req.invalidRegions.add(new Rect(req.canvas.getWidth() + mLastXScroll - 1, 0, req.canvas.getWidth(), req.canvas.getHeight()));
		}
		
		if (mLastYScroll > 0) {
			req.invalidRegions.add((new Rect(0, 0, req.canvas.getWidth(), mLastYScroll + 1)));
		} else if (mLastYScroll < 0) {
			req.invalidRegions.add(new Rect(0, req.canvas.getHeight() + mLastYScroll - 1, req.canvas.getWidth(), req.canvas.getHeight()));
		}
		
		req.deltaX = mLastXScroll;
		req.deltaY = mLastYScroll;
		
		return req;
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

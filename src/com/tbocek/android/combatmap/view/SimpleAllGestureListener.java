package com.tbocek.android.combatmap.view;

import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

public class SimpleAllGestureListener extends SimpleOnScaleGestureListener implements OnGestureListener, OnDoubleTapListener  {

	@Override
	public boolean onDown(MotionEvent arg0) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {	
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {	
	}

	@Override
	public boolean onSingleTapUp(MotionEvent ev) {
		return false;
	}

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		return true;
	}

}

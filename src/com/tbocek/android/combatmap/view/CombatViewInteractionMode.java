package com.tbocek.android.combatmap.view;


import com.tbocek.android.combatmap.graphicscore.PointF;

import android.graphics.Canvas;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class CombatViewInteractionMode extends SimpleAllGestureListener {

	protected CombatView view;

	public CombatViewInteractionMode(CombatView view) {
		this.view = view;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		view.getTransformer().zoom(detector.getScaleFactor(), new PointF(detector.getFocusX(), detector.getFocusY()));
	    view.invalidate();
	    return true;
	}

	public void onCreateContextMenu(ContextMenu menu) {
	}

	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}
	
	/**
	 * Allows the manipulation mode to draw custom user interface elements.
	 * @param c
	 */
	public void draw(Canvas c) {
		
	}
	
	public void onUp(MotionEvent event) {
		
	}
}
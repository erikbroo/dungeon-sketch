package com.tbocek.android.combatmap.view;


import android.view.ScaleGestureDetector;

public class CombatViewGestureListener extends SimpleAllGestureListener {

	protected CombatView view;

	public CombatViewGestureListener(CombatView view) {
		this.view = view;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		view.getTransformer().zoomLevel *= detector.getScaleFactor();
	    
	    // Don't let the object get too small or too large.
		view.getTransformer().zoomLevel = Math.max(0.1f, Math.min(view.getTransformer().zoomLevel, 5.0f));
	
	    view.invalidate();
	    return true;
	}
}
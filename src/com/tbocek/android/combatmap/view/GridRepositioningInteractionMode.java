package com.tbocek.android.combatmap.view;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class GridRepositioningInteractionMode extends CombatViewInteractionMode {

	public GridRepositioningInteractionMode(CombatView view) {
		super(view);
	}
	
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	view.mData.grid.gridSpaceToWorldSpaceTransformer().moveOrigin(
    			-view.getTransformer().screenSpaceToWorldSpace(distanceX),
    			-view.getTransformer().screenSpaceToWorldSpace(distanceY));
    	view.invalidate();
		return true;
	}
    
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		PointF invariantPointWorldSpace = view.getTransformer().screenSpaceToWorldSpace(detector.getFocusX(), detector.getFocusY());
		view.mData.grid.gridSpaceToWorldSpaceTransformer().zoom(detector.getScaleFactor(), invariantPointWorldSpace);
	    view.invalidate();
	    return true;
	}

}

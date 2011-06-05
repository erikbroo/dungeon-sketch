package com.tbocek.android.combatmap.view;


import android.graphics.PointF;
import android.view.MotionEvent;

public class EraserGestureListener extends CombatViewGestureListener {

	public EraserGestureListener(CombatView view) {
		super(view);
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		for (int i = 0; i < view.getLines().size(); ++i) {
			view.getLines().get(i).erase(
					view.getTransformer().screenSpaceToWorldSpace(new PointF(e2.getX(), e2.getY())),
					30 / view.getTransformer().zoomLevel);
		}
		view.invalidate();
		return true;
	}

}

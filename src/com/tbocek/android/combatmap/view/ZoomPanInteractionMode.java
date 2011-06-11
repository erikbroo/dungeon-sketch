package com.tbocek.android.combatmap.view;


import android.view.MotionEvent;

public class ZoomPanInteractionMode extends CombatViewInteractionMode {
	public ZoomPanInteractionMode(CombatView view) {
		super(view);
	}
	
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	view.getTransformer().moveOrigin(-distanceX, -distanceY);
    	view.invalidate();
		return true;
	}
    
    

}

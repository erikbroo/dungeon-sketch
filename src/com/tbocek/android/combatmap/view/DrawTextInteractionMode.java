package com.tbocek.android.combatmap.view;

import com.tbocek.android.combatmap.graphicscore.PointF;

import android.view.MotionEvent;

public class DrawTextInteractionMode extends BaseDrawInteractionMode {
	
	public DrawTextInteractionMode(CombatView view) {
		super(view);
	}
	
    @Override
    public boolean onSingleTapConfirmed(final MotionEvent e) {
    	PointF p = new PointF(e.getX(), e.getY());
    	
    	view.requestNewTextEntry( 
    			view.getWorldSpaceTransformer().screenSpaceToWorldSpace(p));
    	
        return true;
    }
}

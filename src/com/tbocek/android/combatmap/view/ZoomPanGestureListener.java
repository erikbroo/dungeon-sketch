package com.tbocek.android.combatmap.view;


import com.tbocek.android.combatmap.graphicscore.Token;

import android.graphics.PointF;
import android.view.MotionEvent;

public class ZoomPanGestureListener extends CombatViewGestureListener {
	public ZoomPanGestureListener(CombatView view) {
		super(view);
	}
	
	Token currentToken = null;
	
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	if (currentToken != null) {
    		currentToken.move(view.getTransformer().screenSpaceToWorldSpace(distanceX), 
    				          view.getTransformer().screenSpaceToWorldSpace(distanceY));
    	}
    	else {
    		view.getTransformer().originX -= distanceX;
    		view.getTransformer().originY -= distanceY;
    	}
    	view.invalidate();
		return true;
	}
    
    

}

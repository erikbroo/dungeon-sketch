package com.tbocek.android.combatmap.view;

import android.graphics.PointF;
import android.view.MotionEvent;

import com.tbocek.android.combatmap.graphicscore.Token;

public class TokenManipulationListener extends ZoomPanGestureListener {
	public TokenManipulationListener(CombatView view) {
		super(view);
		// TODO Auto-generated constructor stub
	}

	Token currentToken = null;
    @Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    	if (currentToken != null) {
    		currentToken.move(view.getTransformer().screenSpaceToWorldSpace(distanceX), 
    				          view.getTransformer().screenSpaceToWorldSpace(distanceY));
    	}
    	else {
    		return super.onScroll(e1, e2, distanceX, distanceY);
    	}
    	view.invalidate();
		return true;
	}
    public boolean onDown(MotionEvent e) {
    	currentToken = view.tokens.getTokenUnderPoint(new PointF(e.getX(), e.getY()), view.getTransformer());
    	return true;
    }
    
    public boolean onDoubleTap(MotionEvent e) {
    	if (currentToken != null)
    		currentToken.bloodied = !currentToken.bloodied;
    	view.invalidate();
    	return true;
    }
}
